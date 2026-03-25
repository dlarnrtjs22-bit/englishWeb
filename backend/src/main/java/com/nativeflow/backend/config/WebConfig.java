package com.nativeflow.backend.config;

import com.nativeflow.backend.common.security.AuthInterceptor;
import com.nativeflow.backend.common.security.CurrentUserArgumentResolver;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final CurrentUserArgumentResolver currentUserArgumentResolver;

    public WebConfig(
            AuthInterceptor authInterceptor,
            CurrentUserArgumentResolver currentUserArgumentResolver
    ) {
        this.authInterceptor = authInterceptor;
        this.currentUserArgumentResolver = currentUserArgumentResolver;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173")
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
