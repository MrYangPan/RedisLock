package com.redislock;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.UUID;

/**
 * Created by Mr.PanYang on 2018/9/3.
 * <p>
 * redis 分布式锁
 * <p>
 * 有两个超时时间：
 * 1.在获取锁之前的超时间，如果规定时间内没有获取到所，直接放弃
 * 2.获取锁之后的超时时间，获取锁之后，key 对应的有效期时间，规定时间内要失效，防止死锁
 */
public class LockRedis {

    //redis 线程池
    private JedisPool jedisPool;
    //同时在redis上创建相同的一个key，key名称
    private String redislocakKey = "redis_lock";

    public LockRedis(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    /**
     * @param acquireTimeout 获取锁之前的超时时间
     * @param timeOut        获取锁之后的超时时间
     * @Author: My.PanYang
     * @Description: 获取锁
     * @Date: 17:14 2018/9/3
     */
    public String getRedisLock(Long acquireTimeout, Long timeOut) {
        /*   1.创建锁
         *   2.定义redis对应key的value值（uuid）保证唯一，标识锁的id，为了释放锁的时候，保证安全问题
         *   3.使用循环机制，保证重复尝试进行获取锁（乐观锁）
         *   4.使用setNX命令插入key，如果返回1，获取锁成功
        * */

        Jedis conn = null;
        try {
            // 1.建立redis连接
            conn = jedisPool.getResource();
            // 2.随机生成一个value
            String identifierValue = UUID.randomUUID().toString();
            int expireLock = (int) (timeOut / 1000);//秒为单位
            // 定义在没有获取锁之前,锁的超时时间
            Long endTime = System.currentTimeMillis() + acquireTimeout;
            while (System.currentTimeMillis() < endTime) {
                // 6.使用setnx方法设置锁值
                if (conn.setnx(redislocakKey, identifierValue) == 1) {
                    // 判断返回结果如果为1,则可以成功获取锁,并且设置锁的超时时间
                    conn.expire(redislocakKey, expireLock);
                    return identifierValue;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
        return null;
    }

    /**
     * @Author: My.PanYang
     * @Description: 释放锁
     * @Date: 17:15 2018/9/3
     */
    public boolean unRedisLock(String identifier) {
        Jedis conn = null;
        boolean flag = false;
        try {
            // 1.建立redis连接
            conn = jedisPool.getResource();
            // 3.如果value与redis中一直直接删除，否则等待超时
            if (identifier.equals(conn.get(redislocakKey))) {
                conn.del(redislocakKey);
                System.out.println(identifier + "解锁成功......");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
        return flag;
    }
}
