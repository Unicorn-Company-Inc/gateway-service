package com.shopicorn.gateway;

import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.factory.RequestRateLimiterGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Routes {

	@Value("${CALCULATOR_SERVICE_URL}")
	private String calculatorServiceUrl = "http://localhost:3003";

	@Value("${PRODUCT_SERVICE_URL}")
	private String productServiceUrl = "http://localhost:3001";

	@Autowired
	private KeyResolver hostNameResolver;

	@Autowired
	@Qualifier("redisRateLimiter")
	private RateLimiter<?> rateLimiter;

	@Bean
	public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
		Consumer<RequestRateLimiterGatewayFilterFactory.Config> redisRateLimiterConfig = c -> {
			c.setKeyResolver(hostNameResolver);
			c.setRateLimiter(rateLimiter);
		};
		return builder.routes()
				.route("calculator",
						r -> r.path("/mwst/**")
						.filters(f -> f.rewritePath("/mwst/(?<price>.*)", "/calculator/mwst/${price}")
						.requestRateLimiter(redisRateLimiterConfig))
						.uri(calculatorServiceUrl))
				.route("product_list", r -> r.path("/products").filters(
						f -> f.rewritePath("/products", "/product/products")
						.requestRateLimiter(redisRateLimiterConfig))
						.uri(productServiceUrl))
				.route("product_info",
						r -> r.path("/products/**")
						.filters(f -> f.rewritePath("/products/(?<id>.*)", "/product/products/${id}")
						.requestRateLimiter(redisRateLimiterConfig))
						.uri(productServiceUrl))
				.build();

	}
}
