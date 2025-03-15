package com.strategy.autofill.strategies;

import java.lang.reflect.Field;

public interface FillStrategy {
    void fill(Object object, Field field, String key, String ref);
}
