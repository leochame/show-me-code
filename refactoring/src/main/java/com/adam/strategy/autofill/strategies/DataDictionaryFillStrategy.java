package com.adam.strategy.autofill.strategies;

import java.lang.reflect.Field;

public class DataDictionaryFillStrategy implements FillStrategy {

    @Override
    public void fill(Object object, Field field, String key, String ref) {
        try {
            field.setAccessible(true);
            Object fieldValue = field.get(object);
            if (fieldValue != null) {
                String value = DataDictionaryMap.getValue(key);
                if (value != null) {
                    // 查找 ref 字段并填充
                    Class<?> clazz = object.getClass();
                    Field refField = clazz.getDeclaredField(ref);
                    refField.setAccessible(true);
                    refField.set(object, value);
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
