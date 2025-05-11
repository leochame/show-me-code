package com.adam.singleton;

import java.io.Serializable;

//public final class Singleton implements Serializable {
public final class Singleton{
    private static volatile  Singleton instance = null;

    private Singleton(){}


    public static Singleton getInstance(){
        if(instance != null){
            return instance;
        }
        synchronized (Singleton.class) {
            if(instance != null) return instance;
            instance = new Singleton();
            return instance;
        }
    }

//    /**
//     * 防止序列化的时候出错；
//     */
//    @Serial
//    private Object readResolve() {
//        return instance;
//    }
}
