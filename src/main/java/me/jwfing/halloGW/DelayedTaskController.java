package me.jwfing.halloGW;

import com.codahale.metrics.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(value = "/delayed", produces = "application/json;charset=utf-8")
public class DelayedTaskController {
    private static Random random = new Random(System.currentTimeMillis());

    public static MetricRegistry metricRegistry = new MetricRegistry();
    private static Meter onlineOpTimer = metricRegistry.meter("onlineTimer");
    private static Histogram histogram = metricRegistry.histogram("onlineHistogram");
    private static Timer timer = new Timer(onlineOpTimer, histogram, Clock.defaultClock());

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
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                Snapshot snapshot = histogram.getSnapshot();
                StringBuilder sb = new StringBuilder();
                sb.append("count = " + histogram.getCount());
                sb.append("\r\n");
                sb.append("min = " + snapshot.getMin());
                sb.append("\r\n");
                sb.append("max = " + snapshot.getMax());
                sb.append("\r\n");
                sb.append("mean = " + snapshot.getMean());
                sb.append("\r\n");
                sb.append("75% <= " + snapshot.get75thPercentile());
                sb.append("\r\n");
                sb.append("95% <= " + snapshot.get95thPercentile());
                sb.append("\r\n");
                sb.append("99% <= " + snapshot.get99thPercentile());
                return sb.toString();
            }
        });
    }

    @PostMapping("/online")
    public void addDelayedMessage(String value) {
        // value is user's id.
        Timer.Context ctx = timer.time();
        redisDelayedQueueManager.addOrRefreshOnlineTime(value, 125,
                Constaints.MAX_DELAY_INTERVAL, TimeUnit.SECONDS);
        ctx.stop();
    }
}