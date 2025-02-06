package com.adam.strategy.autofill.processor;

import com.adam.strategy.autofill.annoation.DataDictionary;
import com.adam.strategy.autofill.annoation.FlagURL;
import com.adam.strategy.autofill.strategies.DataDictionaryFillStrategy;
import com.adam.strategy.autofill.strategies.FillStrategy;
import com.adam.strategy.autofill.strategies.FlagURLFillStrategy;

import java.lang.reflect.Field;

public class AutoFillProcessor {

    private static final FillStrategy dataDictionaryStrategy = new DataDictionaryFillStrategy();
    private static final FillStrategy flagUrlStrategy = new FlagURLFillStrategy();

    public static void autoFill(Object object) {
        Class<?> clazz = object.getClass();

        // 遍历对象的字段
        for (Field field : clazz.getDeclaredFields()) {
            // 处理 DataDictionary 注解
            if (field.isAnnotationPresent(DataDictionary.class)) {
                DataDictionary annotation = field.getAnnotation(DataDictionary.class);
                String key = annotation.key();
                String ref = annotation.ref();
                // 使用数据字典填充策略
                dataDictionaryStrategy.fill(object, field, key, ref);
            }

            // 处理 FlagURL 注解
            if (field.isAnnotationPresent(FlagURL.class)) {
                FlagURL annotation = field.getAnnotation(FlagURL.class);
                String key = annotation.key();
                String ref = annotation.ref();
                // 使用国旗URL填充策略
                flagUrlStrategy.fill(object, field, key, ref);
            }
        }
    }
}
