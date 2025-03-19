package com.adam.business_code;

import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        User user1 = new User("a",1);
        User user2 = new User("a", 1);

        Map<User,Integer> map = new HashMap<>();
        map.put(user1,0);
        map.put(user2,2);

        for (User user : map.keySet()) {
            System.out.println(user.getUserName());
        }

    }
}
