local cur = redis.call("GET", KEYS[1])

cur = tonumber(cur)
local next = 1 - cur
redis.call("SET", KEYS[1], next)
return cur
