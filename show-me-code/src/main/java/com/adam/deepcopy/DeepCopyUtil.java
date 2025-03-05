package com.adam.deepcopy;


import java.lang.ref.WeakReference;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 该工具类提供了深拷贝功能，支持对普通对象、List、Map等类型的数据进行深拷贝。
 * 深拷贝意味着不仅会拷贝对象本身，还会拷贝对象中的所有引用字段，避免直接引用原对象。
 * 适用于需要创建对象独立副本的场景，避免修改原对象时影响到拷贝的副本。
 * <p>
 * 注意：此工具类在拷贝过程中会使用缓存来避免重复拷贝相同对象，并通过反射机制递归地处理所有字段。
 * </p>
 */
public class DeepCopyUtil {

    //减少反射的次数，优化速度
    private static final Map<Class<?>, Field[]> FIELD_CACHE = new ConcurrentHashMap<>();
    //防止死循环
    private static final Map<Integer, WeakReference<Object>> COPIED_OBJECTS = new ConcurrentHashMap<>();

    /**
     * 深拷贝 List 类型的数据。
     * <p>
     * 创建一个新的 List，并确保原 List 中的每个元素都进行深拷贝。如果 List 中的元素是对象类型，
     * 则这些对象会被复制，而不是直接引用原对象。这适用于需要保留原 List 数据不被修改的场景。
     * </p>
     *
     * @param originalList 被拷贝的 List，不能为空
     * @param clazz List 中元素的类型，用于创建新对象
     * @param <T> List 中元素的类型
     * @return 返回一个新的 List，包含深拷贝后的元素
     * @throws NullPointerException 如果原始 List 为 null
     * @throws IllegalArgumentException 如果 clazz 为 null 或不支持深拷贝
     */
    public static <T> List<T> deepCopy(List<T> originalList, Class<T> clazz) {
        if (originalList == null) return null;
        List<T> copyList = new ArrayList<>(originalList.size());
        for (T item : originalList) {
            copyList.add(deepCopy(item, clazz));
        }
        return copyList;
    }


    /**
     * 深拷贝单个对象。
     * <p>
     * 创建一个新的对象，并递归地复制该对象的所有字段。为了避免对象间的循环引用，该方法会检查并缓存已拷贝的对象。
     * 如果对象已经被拷贝过，会直接返回该对象的拷贝。
     * </p>
     *
     * @param original 被拷贝的对象，不能为空
     * @param clazz 对象的类型，用于创建新对象
     * @param <T> 对象的类型
     * @return 返回深拷贝后的对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T deepCopy(T original, Class<T> clazz) {
        if (original == null) {
            return null;
        }
        if(COPIED_OBJECTS.get(System.identityHashCode(original))==null){
            return deepCopyObject(original, clazz);
        }else{
            return (T) COPIED_OBJECTS.get(System.identityHashCode(original)).get();
        }

    }

    /**
     * 递归处理对象的深拷贝。
     * <p>
     * 该方法会根据字段的类型递归地创建对象并赋值。对于基础数据类型或 String 类型的字段，会直接复制值。
     * 对于其他类型，会递归调用深拷贝方法。
     * </p>
     *
     * @param original 原始对象
     * @param clazz 对象的类型
     * @param <T> 对象的类型
     * @return 返回深拷贝后的对象
     */
    private static <T> T deepCopyObject(T original, Class<T> clazz) {
        try {
            // 创建一个新的实例
            T copy = clazz.getDeclaredConstructor().newInstance();

            // 使用弱引用来存储拷贝对象
            COPIED_OBJECTS.put(System.identityHashCode(original), new WeakReference<>(copy));

            // 获取字段信息
            Field[] fields = getFields(clazz);
            for (Field field : fields) {
                if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
                    continue;
                }
                field.setAccessible(true);
                Object fieldValue = field.get(original);

                if (fieldValue == null) {
                    // 空值直接赋值
                    field.set(copy, null);
                } else {
                    // 处理字段值
                    field.set(copy, handleFieldType(field, fieldValue));
                }
            }
            return copy;
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new DeepCopyException("深拷贝失败，无法创建对象或访问字段", e);
        }
    }

    /**
     * 根据字段的类型处理字段值。
     * <p>
     * 该方法会根据字段的类型决定如何处理字段值。对于基本数据类型、日期、BigDecimal、List 和 Map 类型，分别处理。
     * 对于其他类型的字段，会递归调用深拷贝方法。
     * </p>
     *
     * @param field 字段信息
     * @param fieldValue 字段的值
     * @return 返回处理后的字段值
     */
    @SuppressWarnings("unchecked")
    private static Object handleFieldType(Field field, Object fieldValue) {
        if (fieldValue instanceof Date) {
            // 拷贝 Date
            return new Date(((Date) fieldValue).getTime());
        } else if (fieldValue instanceof BigDecimal) {
            // 拷贝 BigDecimal
            return new BigDecimal(fieldValue.toString());
        } else if (fieldValue instanceof List) {
            // 递归处理 List
            return deepCopyList(field, (List<?>) fieldValue);
        } else if (fieldValue instanceof Map) {
            // 递归处理 Map
            return deepCopyMap(field, (Map<?, ?>) fieldValue);
        } else if (fieldValue != null && field.getType().isPrimitive()||field.getType().equals(String.class)) {
            // 原始类型直接赋值
            return fieldValue;
        } else {
            // 对其他非原始类型字段递归深拷贝
            return deepCopy(fieldValue, (Class<? super Object>) field.getType());
        }
    }


    /**
     * 处理 List 类型字段的深拷贝。
     * <p>
     * 如果 List 中的元素类型能够确定，会递归深拷贝每个元素。否则会退回到 Object 类型进行深拷贝。
     * </p>
     *
     * @param field 字段信息
     * @param originalList 被拷贝的 List
     * @param <T> List 元素的类型
     * @return 返回深拷贝后的 List
     */
    @SuppressWarnings("unchecked")
    private static <T> List<T> deepCopyList(Field field, List<?> originalList) {
        // 获取 List 中实际元素的类型
        Type listType = field.getGenericType();
        if (listType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) listType;
            // 获取泛型类型参数
            Type actualType = parameterizedType.getActualTypeArguments()[0];

            List<T> copyList = new ArrayList<>(originalList.size());
            for (Object item : originalList) {
                copyList.add(deepCopy((T) item, (Class<T>) actualType));
                // 使用深拷贝处理每个元素
            }
            return copyList;
        } else {
            // 如果无法获取 List 泛型类型，输出警告并使用 Object 类型处理
            System.out.println("警告：无法获取 List 的泛型类型，默认使用 Object 类型进行深拷贝");
            List<T> copyList = new ArrayList<>(originalList.size());
            for (Object item : originalList) {
                // 默认使用 Object 类型进行深拷贝
                copyList.add((T) deepCopy(item, Object.class));
            }
            return copyList;
        }
    }

    /**
     * 深拷贝 Map 类型。
     * 递归深拷贝 Map 中的键和值。
     *
     * @param field 字段信息
     * @param originalMap 原始 Map
     * @param <K> Map 键的类型
     * @param <V> Map 值的类型
     * @return 返回深拷贝后的 Map
     */
    private static <K, V> Map<K, V> deepCopyMap(Field field, Map<K, V> originalMap) {
        Map<K, V> copyMap = new HashMap<>(originalMap.size());
        // 获取 Map 的值类型
        Type mapType = field.getGenericType();
        if (mapType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) mapType;
            // 获取 Map 的键类型
            Type keyType = parameterizedType.getActualTypeArguments()[0];
            // 获取 Map 的值类型
            Type valueType = parameterizedType.getActualTypeArguments()[1];
            // 使用 Class<K> 和 Class<V> 类型推导键和值类型
            Class<K> keyClass = (Class<K>) keyType;
            Class<V> valueClass = (Class<V>) valueType;

            for (Map.Entry<K, V> entry : originalMap.entrySet()) {
                // 深拷贝 Map 的值
                copyMap.put(entry.getKey(), deepCopy(entry.getValue(), valueClass));
            }
            return copyMap;
        } else {
            // 如果无法获取 Map 泛型类型，输出警告并使用 Object 类型处理
            System.out.println("警告：无法获取 Map 的泛型类型，默认使用 Object 类型进行深拷贝");
            for (Map.Entry<K, V> entry : originalMap.entrySet()) {
                // 默认使用 Object 类型进行深拷贝
                copyMap.put(entry.getKey(), (V) deepCopy(entry.getValue(), Object.class));
            }
            return copyMap;
        }
    }
    /**
     * 获取指定类的所有字段（包括私有字段），并使用缓存机制来优化性能。
     * <p>
     * 该方法会先检查缓存中是否已经存储了该类的字段信息，如果已经存在，则直接返回缓存的字段数组；
     * 如果不存在，则通过反射获取字段信息，并将其缓存起来，以供后续使用。此方法避免了每次调用时都进行反射操作，
     * 提高了性能，尤其是在需要频繁访问字段的情况下。
     * </p>
     * <p>
     * 注意：JDK 1.8 中的 `ConcurrentHashMap` 在某些情况下可能会遇到锁升级为重量级锁的问题，
     * 该方法通过先检查缓存是否已有值来避免每次都调用 `computeIfAbsent` 直接触发同步机制，从而提高效率。
     * </p>
     *
     * @param clazz 要获取字段信息的类
     * @return 返回该类的所有字段，包括私有字段
     */
    private static Field[] getFields(Class<?> clazz) {
        if (FIELD_CACHE.get(clazz) != null) {
            return FIELD_CACHE.get(clazz);
        } else {
            return FIELD_CACHE.computeIfAbsent(clazz, Class::getDeclaredFields);
        }
    }


    /**
     * 自定义异常类，用于在深拷贝过程中出现错误时提供更具体的错误信息。
     * <p>
     * 该异常类可以帮助开发者更清晰地定位深拷贝过程中出现的问题，
     * 比如对象创建失败、字段访问失败等。它扩展了 RuntimeException，
     * 因此在深拷贝过程中抛出时，不需要强制捕获或声明。
     * </p>
     */
    public static class DeepCopyException extends RuntimeException {

        /**
         * 构造一个带有详细错误消息和根本原因的异常实例。
         * <p>
         * 该构造函数可用于捕捉深拷贝过程中的具体错误，并附带详细的错误消息，
         * 方便开发者在调试时查看。
         * </p>
         *
         * @param message 错误消息，描述异常的具体原因
         * @param cause 异常的根本原因，通常为抛出该异常的引发异常
         */
        public DeepCopyException(String message, Throwable cause) {
            super(message, cause);  // 调用父类构造函数，传递消息和异常原因
        }

        /**
         * 构造一个仅包含错误消息的异常实例。
         * <p>
         * 如果没有根本原因（cause），可以使用此构造函数仅传递错误消息。
         * </p>
         *
         * @param message 错误消息，描述异常的具体原因
         */
        public DeepCopyException(String message) {
            super(message);  // 调用父类构造函数，仅传递消息
        }
    }

}

