package com.nativeflow.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.database")
public record DatabaseProperties(
        String host,
        int port,
        String database,
        String user,
        String password,
        String poolMode
) {
}
