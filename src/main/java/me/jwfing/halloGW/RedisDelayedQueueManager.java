package me.jwfing.halloGW;

import org.redisson.api.RBlockingQueue;
import org.redisson.api.RBucket;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisDelayedQueueManager {
    @Autowired
    RedissonClient redissonClient;

    private static String onlineBucketName = "onlineBucket-";
    private static String delayQueueName = "DelayMessage";
    private static String ONLIE_VALUE = "1";

    /**
     * 添加元素到延时队列，或者刷新在线状态的过期时间。
     *
     * @param t         队列成员
     * @param delay     延时时间
     * @param timeUnit  时间单位
     * @param <T>       泛型
     */
    public <T> void addOrRefreshOnlineTime(T t, long ttl, long delay, TimeUnit timeUnit) {
        RBucket<String> onlineBucket = redissonClient.getBucket(onlineBucketName + t, StringCodec.INSTANCE);
        if (!ONLIE_VALUE.equals(onlineBucket.getAndSet(ONLIE_VALUE, ttl, TimeUnit.SECONDS))) {
            // first occ
            System.out.println("first occ for user: " + t.toString());
            offerDelayedQueue(t, ttl, delay, timeUnit);
        } else {
            System.out.println("renew online token for user: " + t.toString());
        }
    }

    /**
     * 添加元素到延时队列
     *
     * @param t         队列成员
     * @param delay     延时时间
     * @param timeUnit  时间单位
     * @param <T>       泛型
     */
    public <T> void offerDelayedQueue(T t, long ttl, long delay, TimeUnit timeUnit) {
        RBlockingQueue<T> blockingFairQueue = redissonClient.getBlockingQueue(delayQueueName);
        RDelayedQueue<T> delayedQueue = redissonClient.getDelayedQueue(blockingFairQueue);
        delayedQueue.offer(t, delay, timeUnit);
        delayedQueue.destroy();
    }

    /**
     * 判断用户是否在线
     *
     * @param t
     * @param <T>
     * @return
     */
    public<T> boolean onlineCheck(T t) {
        RBucket<String> onlineBucket = redissonClient.getBucket(onlineBucketName + t, StringCodec.INSTANCE);
        return null != onlineBucket && ONLIE_VALUE.equals(onlineBucket.get());
    }

    /**
     * 获取元素并删除
     * @param delayedTaskListener 延时任务监听器
     * @param <T>                 泛型
     */
    public <T> void take(DelayedTaskListener delayedTaskListener) {
        RBlockingQueue<T> blockingFairQueue = redissonClient.getBlockingQueue(delayQueueName);
        while (true) {
            try {
                delayedTaskListener.invoke(blockingFairQueue.take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
