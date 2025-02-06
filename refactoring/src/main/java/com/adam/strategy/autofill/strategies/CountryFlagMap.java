package com.adam.strategy.autofill.strategies;

import java.util.HashMap;
import java.util.Map;

public class CountryFlagMap {
    // 模拟国旗URL映射
    private static final Map<String, String> flagUrls = new HashMap<>();

    static {
        flagUrls.put("US", "https://example.com/flags/us.png");
        flagUrls.put("CN", "https://example.com/flags/cn.png");
    }

    public static String getFlagUrl(String countryCode) {
        return flagUrls.get(countryCode);
    }
}
