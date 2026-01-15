package com.demo.pojo;

import lombok.Getter;

@Getter
public enum RedisLuaScript {

    ARTICLE_LIKE("lua/article_like.lua"),
    CURRENT_CHANGE("lua/current_change.lua");

    private final String path;

    RedisLuaScript(String path) {
        this.path = path;
    }
}
