# Redis + Lua 点赞/收藏缓存设计说明文档

## 1. 背景与问题描述

在文章点赞 / 收藏功能中，系统需要同时满足以下目标：

- **高并发下的正确性**
- **避免缓存击穿、并发穿透**
- **减少 Redis IO 次数**
- **避免 DB 被放大访问**
- **保证读路径性能稳定**

最初实现中，存在以下问题：

1. **Redis 多次 IO**
   - `EXISTS` / `EXPIRE` / `SET` / `SADD` 分散执行
   - 单次请求触发多次网络往返
2. **并发穿透风险**
   - 多个请求同时发现缓存不存在
   - 同一时间触发多次 DB 查询
3. **冷启动不稳定**
   - 冷访问延迟高
   - 并发下 DB 压力放大

------

## 2. 核心设计目标

本设计的核心目标可以总结为一句话：

> **用 Redis Lua 的原子性，换取更少的 IO、更稳定的并发行为和可控的 DB 访问频率。**

具体目标包括：

- Redis **判断 + 占位 + 续期** 原子完成
- 冷加载 **最多只允许一个请求访问 DB**
- 热路径 **不访问 DB**
- 避免过度 batch、不过度设计

------

## 3. Redis Key 设计

以点赞功能为例：

| Key                       | 类型   | 说明                          |
| ------------------------- | ------ | ----------------------------- |
| `article:like:{id}`       | Set    | 点赞用户集合                  |
| `article:like:init:{id}`  | String | 初始化状态（READY / LOADING） |
| `article:like:count:{id}` | String | 点赞数量                      |

说明：

- **initKey 用于并发控制**
- **countKey 为展示用统计，不承担强一致性**

------

## 4. 整体流程设计（两段 Lua）

### 4.1 流程总览

```
请求到来
   ↓
Lua #1（ensure）
   ├─ READY    → 直接使用缓存
   ├─ LOADING  → 不访问 DB
   └─ NEED_LOAD → 只有一个请求进入 DB
                     ↓
                Java 查询 DB
                     ↓
               Lua #2（fill）
                     ↓
                 标记 READY
```

------

## 5. Lua 脚本设计

### 5.1 Lua #1：ensure（判断 + 占位）

**职责：**

- 判断缓存状态
- 防止并发穿透
- 不涉及 DB 数据

```
-- KEYS[1] = initKey
-- KEYS[2] = likeKey
-- KEYS[3] = countKey
-- ARGV[1] = hotTtl
-- ARGV[2] = loadingTtl

local state = redis.call("GET", KEYS[1])

if state == "READY" then
    local ttl = tonumber(ARGV[1])
    redis.call("EXPIRE", KEYS[1], ttl)
    redis.call("EXPIRE", KEYS[2], ttl)
    redis.call("EXPIRE", KEYS[3], ttl)
    return 1 -- READY
end

if state == "LOADING" then
    return 2 -- LOADING
end

redis.call("SET", KEYS[1], "LOADING", "EX", tonumber(ARGV[2]))
return 0 -- NEED_LOAD
```

**说明：**

- 原子判断，避免并发 DB 穿透
- `LOADING` TTL 较短，防止异常卡死

------

### 5.2 Lua #2：fill（回填数据）

**职责：**

- 回填 DB 查询结果
- 统一设置 TTL
- 标记 READY

```
-- KEYS[1] = likeKey
-- KEYS[2] = initKey
-- KEYS[3] = countKey
-- ARGV[1] = ttl
-- ARGV[2..n] = userIds

local ttl = tonumber(ARGV[1])
local n = #ARGV - 1

if n > 0 then
    redis.call("SADD", KEYS[1], unpack(ARGV, 2))
    redis.call("SET", KEYS[3], tostring(n))
else
    redis.call("SET", KEYS[3], "0")
end

redis.call("SET", KEYS[2], "READY")
redis.call("EXPIRE", KEYS[1], ttl)
redis.call("EXPIRE", KEYS[2], ttl)
redis.call("EXPIRE", KEYS[3], ttl)

return 1
```

------

## 6. Java 调用规范（关键点）

### 6.1 必须使用 `StringRedisTemplate`

原因：

- `RedisTemplate<Object, Object>` 使用 JSON 序列化
- 会将 List / Array 序列化为 `"[13,14]"`，破坏 Lua ARGV 语义
- Lua 只接受 **string / integer**

```
@Bean
public StringRedisTemplate luaStringRedisTemplate(
        RedisConnectionFactory connectionFactory) {
    return new StringRedisTemplate(connectionFactory);
}
```

------

### 6.2 Lua 参数构造规则（强约束）

**规则：**

- `ARGV` 必须是一维、扁平参数
- 每一个参数必须是 **String**
- 禁止传 List / Array / Object

```
List<String> args = new ArrayList<>();
args.add(String.valueOf(ttl));

for (Long uid : userIds) {
    args.add(String.valueOf(uid));
}

stringRedisTemplate.execute(
        fillScript,
        keys,
        args.toArray()
);
```

------

## 7. 为什么使用 Lua 不会降低并发性

- Redis 本身单线程
- Lua 将多条命令合并为一次执行
- 减少网络往返和命令调度成本

**结论：**

> 短 Lua 脚本会提升吞吐和稳定性，不会降低并发。

------

## 8. 性能表现总结

### 冷访问

- 第一次访问略慢（冷启动成本）
- 仅一次 DB 查询

### 热访问

- Redis 命中
- DB 零访问
- Redis IO 数量显著减少

### 实测结论

- 稳定后请求耗时 **略有下降**
- 在并发或 TTL 边界场景下 **稳定性明显提升**
- DB 访问被严格限制在可控范围内

------

## 9. 适用范围与边界

适合：

- 点赞 / 收藏 / 关注
- 热数据缓存
- 并发读多写少场景

不适合：

- 超大集合遍历
- 复杂业务逻辑写入 Lua
- 长时间运行脚本

------

## 10. 经验总结（关键结论）

1. **Lua 的价值不在“更快”，而在“更稳”**
2. **ARGV 必须是扁平字符串数组**
3. **Lua 负责原子性，Java 负责数据来源**
4. **缓存设计要区分冷路径与热路径**
5. **优化是为了防止系统在压力下失控，而不是为了炫技**

------

## 11. 最终评价

该方案在复杂度、性能、稳定性之间取得了合理平衡：

- 没有过度设计
- 没有无意义 batch
- 明确限制 DB 访问
- 可长期维护