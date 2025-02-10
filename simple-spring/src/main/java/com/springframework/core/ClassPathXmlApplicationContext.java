package com.springframework.core;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ClassPathXmlApplicationContext是ApplicationContext接口的实现类。
 * 该类从类路径当中加载simple-spring.xml配置文件。
 * @className ClassPathXmlApplicationContext
 * @since 1.0
 **/
public class ClassPathXmlApplicationContext implements ApplicationContext{

    /**
     * 存储bean的Map集合
     */
    private Map<String,Object> beanMap = new HashMap<>();

    /**
     * 在该构造方法中，解析simple-spring.xml文件，创建所有的Bean实例，并将Bean实例存放到Map集合中。
     * @param resource 配置文件路径（要求在类路径当中）
     */
    public ClassPathXmlApplicationContext(String resource) {
        SAXReader reader = new SAXReader();
        Document document = null;
        try {
            document = reader.read(ClassLoader.getSystemClassLoader().getResourceAsStream(resource));
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }
        // 获取所有的bean标签
        List<Node> beanNodes = document.selectNodes("//bean");
        //实例化Bean
        createBean(beanNodes);
        //给Bean进行属性赋值
        propertyAssignment(beanNodes);
    }

    private void propertyAssignment(List<Node> beanNodes) {
        beanNodes.forEach(beanNode -> {
            Element beanElt = (Element) beanNode;
            // 获取bean的id
            String beanId = beanElt.attributeValue("id");
            // 获取所有property标签
            List<Element> propertyElts = beanElt.elements("property");
            // 遍历所有属性
            propertyElts.forEach(propertyElt -> {
                try {
                    // 获取属性名
                    String propertyName = propertyElt.attributeValue("name");
                    // 获取属性类型
                    Class<?> propertyType = beanMap.get(beanId).getClass().getDeclaredField(propertyName).getType();
                    // 获取set方法名
                    String setMethodName = "set" + propertyName.toUpperCase().charAt(0) + propertyName.substring(1);
                    // 获取set方法
                    Method setMethod = beanMap.get(beanId).getClass().getDeclaredMethod(setMethodName, propertyType);
                    // 获取属性的值，值可能是value，也可能是ref。
                    // 获取value
                    String propertyValue = propertyElt.attributeValue("value");
                    // 获取ref
                    String propertyRef = propertyElt.attributeValue("ref");
                    Object propertyVal = null;
                    if (propertyValue != null) {
                        // 该属性是简单属性
                        String propertyTypeSimpleName = propertyType.getSimpleName();
                        switch (propertyTypeSimpleName) {
                            case "byte": case "Byte":
                                propertyVal = Byte.valueOf(propertyValue);
                                break;
                            case "short": case "Short":
                                propertyVal = Short.valueOf(propertyValue);
                                break;
                            case "int": case "Integer":
                                propertyVal = Integer.valueOf(propertyValue);
                                break;
                            case "long": case "Long":
                                propertyVal = Long.valueOf(propertyValue);
                                break;
                            case "float": case "Float":
                                propertyVal = Float.valueOf(propertyValue);
                                break;
                            case "double": case "Double":
                                propertyVal = Double.valueOf(propertyValue);
                                break;
                            case "boolean": case "Boolean":
                                propertyVal = Boolean.valueOf(propertyValue);
                                break;
                            case "char": case "Character":
                                propertyVal = propertyValue.charAt(0);
                                break;
                            case "String":
                                propertyVal = propertyValue;
                                break;
                        }
                        setMethod.invoke(beanMap.get(beanId), propertyVal);
                    }
                    if (propertyRef != null) {
                        // 该属性不是简单属性
                        setMethod.invoke(beanMap.get(beanId), beanMap.get(propertyRef));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });

    }

    private void createBean(List<Node> beanNodes) {
        // 遍历集合
        beanNodes.forEach(beanNode -> {
            Element beanElt = (Element) beanNode;
            // 获取id
            String id = beanElt.attributeValue("id");
            // 获取className
            String className = beanElt.attributeValue("class");
            try {
                // 通过反射机制创建对象
                Class<?> clazz = Class.forName(className);
                Constructor<?> defaultConstructor = clazz.getDeclaredConstructor();
                Object bean = defaultConstructor.newInstance();
                // 存储到Map集合，给bean曝光
                beanMap.put(id, bean);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public Object getBean(String beanId) {
        return beanMap.get(beanId);
    }
}
