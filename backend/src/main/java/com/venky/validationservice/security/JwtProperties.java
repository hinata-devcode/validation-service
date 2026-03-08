package com.venky.validationservice.security;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "security.jwt")
@ToString(exclude = {"secret"})
public class JwtProperties {
    
    private String secret;
    private long expirationMs;
    private boolean cookieSecure;
}
