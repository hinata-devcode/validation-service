package com.venky.validationservice.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.Refill;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;

@Component
@Slf4j
public class RateLimitInterceptor implements HandlerInterceptor {

    @Autowired
    private ProxyManager<String> proxyManager;
    
    @Value("${rate-limit.capacity:100}")
    private int capacity;

    @Value("${rate-limit.refill-tokens:100}")
    private int refillTokens;

    @Value("${rate-limit.refill-duration-minutes:1}")
    private long refillDurationMinutes;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        
        String clientId = request.getHeader("X-User-Id");
        if (clientId == null || clientId.isEmpty()) {
            clientId = request.getRemoteAddr(); 
        }

        String bucketKey = "rate_limit:user:" + clientId;

       
        BucketConfiguration configuration = BucketConfiguration.builder()
                .addLimit(Bandwidth.classic(capacity, 
                          Refill.greedy(refillTokens, Duration.ofMinutes(refillDurationMinutes))))
                .build();
        
        Bucket bucket = proxyManager.builder().build(bucketKey, configuration);

        if (bucket.tryConsume(1)) {
        	log.info(">>>>>> ---- INTERCEPTOR FIRED FOR USER: " + clientId + " | TOKENS REMAINING: " + bucket.getAvailableTokens());
            return true; 
        }

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.getWriter().write("Too many requests. Please try again later.");
        return false;
    }
}