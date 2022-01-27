这是一个 Spring boot API 网关的实验程序。

## 接口与功能

本网关对外提供的接口有如下几类：

- I，获取服务端时间戳
  - GET /serverTS
- II，转发本地请求到第三方（httpbin.org），主要有延迟和非延迟两种请求：
  - GET /get 会被转发给 httpbin.org，httpbin 会立刻给出应答。
  - GET /delay/xxx 也会被转发给 httpbin，不过 httpbin 会按照 xx 部分指定的时间延迟返回。
- III，客户端在线状态检测接口（使用了 Redis 延迟队列）
  - POST /delayed/online 请求参数 value 代表了用户 id，要求客户端以固定间隔调用该接口，以表示客户端在线（间隔不得超过 120 秒）。
  - GET /delayed/metrics 这里可以看到上面状态上报请求的处理时间和耗时的统计结果。

我们通过第 II 类接口测试网关的吞吐量，
通过第 III 类接口测试 Redis 延迟队列在在线状态监测中的性能。

## how to build

使用 mvn 编译该项目：
```shell
mvnw clean package
```

直接调用 java 命令行启动该项目：
```shell
java -jar ./target/halloGW-{version}.jar
```

> 注意：本地需要先启动 redis server（监听端口 6379）。如果你 redis server 的配置与默认值不一样，可修改代码。

## how to run stress test

使用 mvn 命名可直接启动第 III 类接口的压力测试任务：
```shell
mvnw test
```

默认每一次运行代表一个应用，包含 1w 用户，分 10 个线程来间隔不断上报所有玩家状态，你也可以直接修改测试代码来调整：
- 在线状态 server
- 测试用户规模
- 并发线程数
- 测试持续时间

测试过程中可以通过 http://localhost:8080/delayed/metrics 查看请求数和延迟的统计直方图。
