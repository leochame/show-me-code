package com.reflect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionInvokeExample {

    public static void main(String[] args) {
        try {
            // 1. 实例方法调用：Calculator.add(int a, int b)
            Class<?> calculatorClass = Class.forName("com.reflect.Calculator");
            Calculator calculator = (Calculator) calculatorClass.getDeclaredConstructor().newInstance();

            // 获取add方法，参数类型为int.class, int.class
            Method addMethod = calculatorClass.getMethod("add", int.class, int.class);
            
            // 调用实例方法，需传入对象实例和参数
            Object resultAdd = addMethod.invoke(calculator, 2, 3);
            System.out.println("2 + 3 = " + resultAdd); // 输出：2 + 3 = 5

            // 2. 静态方法调用：Calculator.multiply(int a, int b)
            Method multiplyMethod = calculatorClass.getMethod("multiply", int.class, int.class);
            
            // 调用静态方法，传入null作为对象实例
            Object resultMultiply = multiplyMethod.invoke(null, 4, 5);
            System.out.println("4 * 5 = " + resultMultiply); // 输出：4 * 5 = 20

            // 3. 调用私有方法（假设Calculator有一个私有方法）
            // 注意：需使用getDeclaredMethod并设置setAccessible(true)
            Method privateMethod = calculatorClass.getDeclaredMethod("privateMethod");
            privateMethod.setAccessible(true); // 覆盖访问权限
            Object resultPrivate = privateMethod.invoke(calculator);
            System.out.println("私有方法返回值: " + resultPrivate);

        } catch (ClassNotFoundException e) {
            System.err.println("类未找到: " + e.getMessage());
        } catch (NoSuchMethodException e) {
            System.err.println("方法不存在: " + e.getMessage());
        } catch (IllegalAccessException e) {
            System.err.println("访问权限异常: " + e.getMessage());
        } catch (InvocationTargetException e) {
            System.err.println("方法执行异常: " + e.getTargetException().getMessage());
        } catch (InstantiationException e) {
            System.err.println("实例化异常: " + e.getMessage());
        }
    }
}

