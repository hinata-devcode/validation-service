package com.venky.validationservice.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.Refill;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    @Autowired
    private ProxyManager<String> proxyManager;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        
        String clientId = request.getHeader("X-User-Id");
        if (clientId == null || clientId.isEmpty()) {
            clientId = request.getRemoteAddr(); 
        }

        String bucketKey = "rate_limit:user:" + clientId;

       
        BucketConfiguration configuration = BucketConfiguration.builder()
                .addLimit(Bandwidth.classic(20, Refill.greedy(20, Duration.ofMinutes(1))))
                .build();

        Bucket bucket = proxyManager.builder().build(bucketKey, configuration);

       
        if (bucket.tryConsume(1)) {
            return true; 
        }

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.getWriter().write("Too many requests. Please try again later.");
        return false;
    }
}