package com.redislock;

/**
 * Created by Mr.PanYang on 2018/9/3.
 */
public class ThreadRedis extends Thread {

    private LockService lockService;

    public ThreadRedis(LockService lockService) {
        this.lockService = lockService;
    }

    @Override
    public void run() {
        lockService.seckill();
    }
}
