package com.shopicorn.gateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class GatewayServiceApplication {

	@Value("${CALCULATOR_SERVICE_URL}")
	private String calculatorServiceUrl = "http://localhost:3003";

	@Value("${PRODUCT_SERVICE_URL}")
	private String productServiceUrl = "http://localhost:3001";

	@Autowired
	private KeyResolver hostNameResolver;

	@Autowired
	@Qualifier("redisRateLimiter")
	private RateLimiter<?> rateLimiter;

	public static void main(String[] args) {
		SpringApplication.run(GatewayServiceApplication.class, args);
	}

	@Bean
	public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
		return builder.routes().route("calculator", r -> r.path("/mwst/**")
				.filters(f -> f.rewritePath("/mwst/(?<price>.*)", "/calculator/mwst/${price}").requestRateLimiter(c -> {
					c.setKeyResolver(hostNameResolver);
					c.setRateLimiter(rateLimiter);
				})).uri(calculatorServiceUrl))
				.route("product_list",
						r -> r.path("/products").filters(f -> f.rewritePath("/products", "/product/products"))
								.uri(productServiceUrl))
				.route("product_info",
						r -> r.path("/products/**")
								.filters(f -> f.rewritePath("/products/(?<id>.*)", "/product/products/${id}"))
								.uri(productServiceUrl))
				.build();

	}
}
