package com.nativeflow.backend.config;

import com.nativeflow.backend.common.security.AuthInterceptor;
import com.nativeflow.backend.common.security.CurrentUserArgumentResolver;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final CurrentUserArgumentResolver currentUserArgumentResolver;
    private final List<String> corsAllowedOriginPatterns;

    public WebConfig(
            AuthInterceptor authInterceptor,
            CurrentUserArgumentResolver currentUserArgumentResolver,
            @Value("${app.cors.allowed-origin-patterns:http://localhost:5173}") String corsAllowedOriginPatterns
    ) {
        this.authInterceptor = authInterceptor;
        this.currentUserArgumentResolver = currentUserArgumentResolver;
        this.corsAllowedOriginPatterns = Arrays.stream(
                        StringUtils.commaDelimitedListToStringArray(corsAllowedOriginPatterns)
                )
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns(this.corsAllowedOriginPatterns.toArray(String[]::new))
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns(
                        "/api/v1/auth/logout",
                        "/api/v1/auth/me",
                        "/api/v1/me/**",
                        "/api/v1/settings",
                        "/api/v1/dashboard",
                        "/api/v1/packs/**",
                        "/api/v1/series",
                        "/api/v1/series/*",
                        "/api/v1/chatgpt/**",
                        "/api/v1/diary/**",
                        "/api/v1/learning-items/**",
                        "/api/v1/learning-sessions/**",
                        "/api/v1/reviews/**",
                        "/api/v1/favorites"
                );
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserArgumentResolver);
    }
}
