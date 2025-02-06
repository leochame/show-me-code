package com.adam.strategy.autofill.strategies;

import java.util.HashMap;
import java.util.Map;

public class DataDictionaryMap {
    // 模拟数据字典
    private static final Map<String, String> dictionary = new HashMap<>();

    static {
        dictionary.put("gender", "男");
        dictionary.put("status", "启用");
    }

    public static String getValue(String key) {
        return dictionary.get(key);
    }
}

