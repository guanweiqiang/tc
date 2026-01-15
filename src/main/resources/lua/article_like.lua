--============================
--Like toggle script
--============================
--KEYS[1] = article:like:{id}
--KEYS[2] = article:like:add:{id}:v{current}
--KEYS[3] = article:like:del:{id}:v{current}
--KEYS[4] = article:like:update

--ARGV[1] = userId
--ARGV[2] = articleId
--============================

redis.call("SADD", KEYS[4], ARGV[2])
if redis.call("SISMEMBER", KEYS[1], ARGV[1]) == 1 then
    redis.call("SREM", KEYS[1], ARGV[1])
    if redis.call("SISMEMBER", KEYS[2], ARGV[1]) == 1 then
        redis.call("SREM", KEYS[2], ARGV[1])
    else
        redis.call("SADD", KEYS[3], ARGV[1])
    end
    return false
else
    redis.call("SADD", KEYS[1], ARGV[1])
    if redis.call("SISMEMBER", KEYS[3], ARGV[1]) == 1 then
        redis.call("SREM", KEYS[3], ARGV[1])
    else
        redis.call("SADD", KEYS[2], ARGV[1])
    end
    return true
end