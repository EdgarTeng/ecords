package com.tenchael.cords.demo;

import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

public class CordsClient {

    public static void main(String[] args) throws IOException {
        Jedis jedis = new Jedis("localhost", 8989);
        jedis.set("hello", "world");
        jedis.set("abc", "123");
        jedis.set("apple", "fruit");


        String value = jedis.get("hello");
        System.out.println(value);

        value = jedis.get("www");
        System.out.println(value);

        Set<String> keys = jedis.keys("a*");
        System.out.println(Arrays.toString(keys.toArray()));

        System.out.println("quit!");
    }

}
