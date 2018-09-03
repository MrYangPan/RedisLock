package com.redislock;

/**
 * Created by Mr.PanYang on 2018/9/3.
 */
public class RedisLockApp {
    public static void main(String[] args) {
        LockService lockService = new LockService();
        for (int i = 0; i < 50; i++) {
            new ThreadRedis(lockService).start();
        }
    }
}
