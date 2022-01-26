package me.jwfing.halloGW;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisDelayQueueListener implements CommandLineRunner {
    private static final Logger logger = LogManager.getLogger(RedisDelayQueueListener.class);

    @Autowired
    RedisDelayedQueueManager redisDelayedQueueManager;
    @Autowired
    private RedissonClient redissonClient;

    @Override
    public void run(String... args) throws Exception {
        logger.info("===============延时队列监听器启动==============");
        //监听延迟队列
        DelayedTaskListener<String> delayedTaskListener = new DelayedTaskListener<String>() {
            @Override
            public void invoke(String delayMessage) {
                //这里调用你延迟之后的代码,在这里执行业务处理
                if (redisDelayedQueueManager.onlineCheck(delayMessage)) {
                    redisDelayedQueueManager.offerDelayedQueue(delayMessage, 125,151, TimeUnit.SECONDS);
                    System.out.println("re-queue user: " + delayMessage);
                } else {
                    System.out.println(">>>>>===== user expired: " + delayMessage);
                }
            }
        };
        redisDelayedQueueManager.take(delayedTaskListener);
    }
}
