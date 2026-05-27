local stock_key = KEYS[1]
local users_key = KEYS[2]
local user_id = ARGV[1]

local stock = redis.call('GET', stock_key)
if not stock or tonumber(stock) <= 0 then
    return -1
end

if redis.call('SISMEMBER', users_key, user_id) == 1 then
    return -2
end

redis.call('DECR', stock_key)
redis.call('SADD', users_key, user_id)
return 1
