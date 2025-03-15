package com.strategy.autofill.domain;

import com.strategy.autofill.processor.AutoFillProcessor;

public class Main {
    public static void main(String[] args) {
        User user = new User();
        user.setGender("gender");  // 设置 gender 字段为 "gender"，将填充 genderDescription 字段
        user.setCountryCode("US");  // 设置 countryCode 字段为 "US"，将填充 flagUrl 字段

        System.out.println("Before fill:");
        System.out.println("Gender: " + user.getGender());
        System.out.println("Gender Description: " + user.getGenderDescription());
        System.out.println("Country Code: " + user.getCountryCode());
        System.out.println("Flag URL: " + user.getFlagUrl());

        // 自动填充数据
        AutoFillProcessor.autoFill(user);

        System.out.println("\nAfter fill:");
        System.out.println("Gender: " + user.getGender());
        System.out.println("Gender Description: " + user.getGenderDescription());  // 应填充为 "男"
        System.out.println("Country Code: " + user.getCountryCode());
        System.out.println("Flag URL: " + user.getFlagUrl());  // 应填充为 "https://example.com/flags/us.png"
    }
}
