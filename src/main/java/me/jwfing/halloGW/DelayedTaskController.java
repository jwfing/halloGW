package me.jwfing.halloGW;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/delayed")
public class DelayedTaskController {
    private static Random random = new Random(System.currentTimeMillis());

    @Autowired
    private RedisDelayedQueueManager redisDelayedQueueManager;

    public static String getRandomString(int length) {
        String letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder randomString = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            randomString.append(letters.charAt(random.nextInt(letters.length())));
        }

        return randomString.toString();
    }

    @RequestMapping("/online")
    public void addDelayedMessage(String value) {
        // value is user's id.
        redisDelayedQueueManager.addOrRefreshOnlineTime(value, 125,
                Constaints.MAX_DELAY_INTERVAL, TimeUnit.SECONDS);
    }
}