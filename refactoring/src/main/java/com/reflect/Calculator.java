package com.reflect;

class Calculator {
    public int add(int a, int b) {
        return a + b;
    }

    public static int multiply(int a, int b) {
        return a * b;
    }

    @SuppressWarnings("unused")
    private String privateMethod() {
        return "私有方法被调用!";
    }
}