package com.demo.config;

import com.demo.pojo.RedisLuaScript;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
public class RedisLuaScriptManager {

    private final Map<RedisLuaScript, RedisScript<?>> scriptCache = new EnumMap<>(RedisLuaScript.class);


    @SuppressWarnings("unchecked")
    public <T> RedisScript<T> get(RedisLuaScript luaScript, Class<T> resultType) {
        return (RedisScript<T>) scriptCache.computeIfAbsent(luaScript, redisLusScript -> {
            DefaultRedisScript<T> script = new DefaultRedisScript<>();
            script.setLocation(new ClassPathResource(luaScript.getPath()));
            script.setResultType(resultType);
            return script;
        });

    }
}
