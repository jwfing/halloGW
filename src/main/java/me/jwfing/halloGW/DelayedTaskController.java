package me.jwfing.halloGW;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(value = "/delayed", produces = "application/json;charset=utf-8")
@Timed
public class DelayedTaskController {
    private static Random random = new Random(System.currentTimeMillis());
    private static MeterRegistry registry = new SimpleMeterRegistry();
    private static Timer timer = registry.timer("onlineTimer");

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

    @GetMapping("/metrics")
    public Mono<?> outputMetrics() {
        return Mono.fromCallable(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                StringBuilder sb = new StringBuilder();
                for (Meter m: registry.getMeters()) {
                    for (Measurement mm: m.measure()) {
                        sb.append(mm.toString());
                        sb.append("\r\n");
                    }
                }
                return sb.toString();
            }
        });
    }

    @PostMapping("/online")
    public void addDelayedMessage(String value) {
        // value is user's id.
        long beginTs = System.currentTimeMillis();
        redisDelayedQueueManager.addOrRefreshOnlineTime(value, 125,
                Constaints.MAX_DELAY_INTERVAL, TimeUnit.SECONDS);
        timer.record(System.currentTimeMillis() - beginTs, TimeUnit.MILLISECONDS);
    }
}