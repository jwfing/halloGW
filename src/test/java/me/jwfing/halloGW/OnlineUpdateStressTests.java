package me.jwfing.halloGW;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufMono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientResponse;

import java.util.ArrayList;
import java.util.function.BiFunction;

public class OnlineUpdateStressTests {
    @Test
    public void testUpdateOnlineStatus() throws Exception {
//        HttpClient client = reactor.netty.http.client.HttpClient.create().baseUrl("http://localhost:8080");
//        ArrayList userIds = new ArrayList(10000);
//        for (int i = 0;i < 10000; i++) {
//            userIds.add("user-" + i);
//        }
        final int userCnt = 2000;
        final int threadCnt = 10;
        final int loopCntPerThread = 5;
        final String appId = DelayedTaskController.getRandomString(16);
        Thread threads[] = new Thread[threadCnt];
        for (int threadIdx = 0; threadIdx < threadCnt; threadIdx++) {
            final int idx = threadIdx;
            threads[threadIdx] = new Thread(new Runnable() {
                @Override
                public void run() {
                    ArrayList userIds = new ArrayList(userCnt);
                    for (int i = 0;i < userCnt; i++) {
                        userIds.add(appId + ":user-" + idx + "-" + i);
                    }
                    HttpClient client = reactor.netty.http.client.HttpClient.create().baseUrl("http://117.50.162.77");
                    for (int loop = 0; loop < loopCntPerThread; loop++) {
                        long loopStart = System.currentTimeMillis();
                        for (int i = 0;i < userCnt; i++) {
                            client.post().uri("/delayed/online?value="+ userIds.get(i)).responseSingle(
                                    new BiFunction<HttpClientResponse, ByteBufMono, Mono<String>>() {
                                        @Override
                                        public Mono<String> apply(HttpClientResponse httpClientResponse, ByteBufMono byteBufMono) {
                                            return Mono.just(httpClientResponse.status().toString());
                                        }
                                    }).block();
                        }
                        long sleepInterval = loopStart + 120000 - System.currentTimeMillis();
                        if (sleepInterval > 0) {
                            try {
                                Thread.sleep(sleepInterval);
                            } catch (Exception e) {
                                e.printStackTrace();
                                break;
                            }
                        }
                    }
                }
            });
            threads[threadIdx].start();
        }
        for (int threadIdx = 0; threadIdx < threadCnt; threadIdx++) {
            threads[threadIdx].join();
        }
    }
}
