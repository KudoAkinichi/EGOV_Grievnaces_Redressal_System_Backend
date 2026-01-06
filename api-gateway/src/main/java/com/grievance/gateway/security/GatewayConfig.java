// api-gateway/src/main/java/com/grievance/gateway/security/GatewayConfig.java
package com.grievance.gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class GatewayConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();

        // ✅ Allow your frontend origin - SINGLE LIST
        corsConfig.setAllowedOrigins(Arrays.asList(
                "http://localhost:4200",
                "http://localhost:4201",
                "http://127.0.0.1:4200"
        ));

        // ✅ Allow all HTTP methods
        corsConfig.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        // ✅ Allow all headers - DO NOT USE addAllowedHeader AFTER setAllowedHeaders("*")
        corsConfig.setAllowedHeaders(Arrays.asList(
                "*",
                "Authorization",
                "Content-Type",
                "X-User-Id",
                "X-User-Role",
                "X-Department-ID"
        ));

        // ✅ Allow credentials
        corsConfig.setAllowCredentials(true);

        // ✅ Cache preflight
        corsConfig.setMaxAge(3600L);

        // ✅ Expose headers for frontend
        corsConfig.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-User-Id",
                "X-User-Role"
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}