package com.zeroone.star.util;

import com.alibaba.fastjson.JSON;

/**
 * 统一 JSON 工具。
 */
public final class JsonUtil {

    private JsonUtil() {
    }

    public static String toJson(Object value) {
        return JSON.toJSONString(value);
    }
}
