package com.nativeflow.backend.chatgpt.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.chatgpt")
public class ChatGptProperties {

    private String chatgptBaseUrl = "https://chatgpt.com/backend-api";
    private String codexBaseUrl = "https://chatgpt.com/backend-api/codex";
    private String authUrl = "https://auth.openai.com/oauth/token";
    private String clientId = "app_EMoamEEZ73f0CkXaXp7hrann";
    private String defaultCodexModel = "gpt-5.3-codex-spark";
    private String defaultChatModel = "gpt-5.3-high";
    private String oauthTokenFile = "chatgptOauthKey.json";
    private String deviceId = "a5f7b2c1-3e4d-4f8a-9b6c-2d1e0f3a4b5c";
    private int connectTimeoutSeconds = 20;
    private int readTimeoutSeconds = 120;

    public String getChatgptBaseUrl() {
        return chatgptBaseUrl;
    }

    public void setChatgptBaseUrl(String chatgptBaseUrl) {
        this.chatgptBaseUrl = chatgptBaseUrl;
    }

    public String getCodexBaseUrl() {
        return codexBaseUrl;
    }

    public void setCodexBaseUrl(String codexBaseUrl) {
        this.codexBaseUrl = codexBaseUrl;
    }

    public String getAuthUrl() {
        return authUrl;
    }

    public void setAuthUrl(String authUrl) {
        this.authUrl = authUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getDefaultCodexModel() {
        return defaultCodexModel;
    }

    public void setDefaultCodexModel(String defaultCodexModel) {
        this.defaultCodexModel = defaultCodexModel;
    }

    public String getDefaultChatModel() {
        return defaultChatModel;
    }

    public void setDefaultChatModel(String defaultChatModel) {
        this.defaultChatModel = defaultChatModel;
    }

    public String getOauthTokenFile() {
        return oauthTokenFile;
    }

    public void setOauthTokenFile(String oauthTokenFile) {
        this.oauthTokenFile = oauthTokenFile;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public int getConnectTimeoutSeconds() {
        return connectTimeoutSeconds;
    }

    public void setConnectTimeoutSeconds(int connectTimeoutSeconds) {
        this.connectTimeoutSeconds = connectTimeoutSeconds;
    }

    public int getReadTimeoutSeconds() {
        return readTimeoutSeconds;
    }

    public void setReadTimeoutSeconds(int readTimeoutSeconds) {
        this.readTimeoutSeconds = readTimeoutSeconds;
    }
}
