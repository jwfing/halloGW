package me.jwfing.halloGW;

import com.codahale.metrics.ConsoleReporter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class HalloGwApplication {

	public static void main(String[] args) {
		ConsoleReporter reporter = ConsoleReporter.forRegistry(DelayedTaskController.metricRegistry).build();
		reporter.start(3, TimeUnit.MINUTES);
		reporter.report();

		SpringApplication.run(HalloGwApplication.class, args);
	}

	@RequestMapping("/serverTS")
	public Mono<String> serverTimestamp() {
		return Mono.just(new Date().toGMTString());
	}

	@Bean
	public RouteLocator myRoutes(RouteLocatorBuilder builder) {
		return builder.routes()
				.route(p -> p
						.path("/get")
						.filters(f -> {
							System.out.println("/get " + p.toString());
							return f.addRequestHeader("Hello", "World");})
						.uri("http://httpbin.org:80"))
				.route(p -> p
						.path("/delay/*")
						.filters(f -> {
							System.out.println("/delay/* " + p.toString());
							return f.addRequestHeader("Hello", "delay");})
						.uri("http://httpbin.org:80"))
				.build();
	}
}
