package com.demo.pojo;

import lombok.Getter;

@Getter
public enum RedisLuaScript {

    ARTICLE_LIKE("lua/article_like.lua"),
    CURRENT_CHANGE("lua/current_change.lua"),
    ARTICLE_FAVORITE("lua/article_favorite.lua"),
    ENSURE_CACHE("lua/ensure_cache.lua"),
    COMMIT_CACHE("lua/commit_cache.lua");

    private final String path;

    RedisLuaScript(String path) {
        this.path = path;
    }
}
