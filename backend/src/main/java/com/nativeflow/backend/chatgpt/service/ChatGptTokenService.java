package com.nativeflow.backend.chatgpt.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nativeflow.backend.chatgpt.config.ChatGptProperties;
import com.nativeflow.backend.chatgpt.dto.ChatGptDtos;
import com.nativeflow.backend.common.exception.ApiException;
import com.nativeflow.backend.common.exception.ErrorCode;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ChatGptTokenService {

    private static final Duration EXPIRY_SAFETY_WINDOW = Duration.ofMinutes(5);

    private final ChatGptProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final ReentrantLock tokenLock = new ReentrantLock();

    private volatile ObjectNode cachedTokenData;
    private volatile Path cachedTokenFile;

    public ChatGptTokenService(ChatGptProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(properties.getConnectTimeoutSeconds()))
                .build();
    }

    public ChatGptDtos.ChatGptStatusResponse getStatus() {
        JsonNode data = loadTokenData();
        JsonNode tokens = data.path("tokens");
        return new ChatGptDtos.ChatGptStatusResponse(
                tokens.hasNonNull("access_token") && tokens.hasNonNull("refresh_token"),
                data.path("auth_mode").asText("chatgpt"),
                tokens.path("account_id").asText(""),
                resolveTokenFile().toAbsolutePath().normalize().toString(),
                data.path("last_refresh").asText("")
        );
    }

    public Map<String, String> getBaseHeaders() {
        JsonNode data = loadTokenData();
        String accessToken = getAccessToken();
        String accountId = data.path("tokens").path("account_id").asText("");

        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("accept", "*/*");
        headers.put("accept-encoding", "identity");
        headers.put("accept-language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7");
        headers.put("authorization", "Bearer " + accessToken);
        headers.put("content-type", "application/json");
        headers.put("oai-device-id", properties.getDeviceId());
        headers.put("oai-language", "ko-KR");
        headers.put("origin", "https://chatgpt.com");
        headers.put("referer", "https://chatgpt.com/");
        headers.put("sec-ch-ua", "\"Chromium\";v=\"131\", \"Not_A Brand\";v=\"24\"");
        headers.put("sec-ch-ua-mobile", "?0");
        headers.put("sec-ch-ua-platform", "\"Windows\"");
        headers.put("sec-fetch-dest", "empty");
        headers.put("sec-fetch-mode", "cors");
        headers.put("sec-fetch-site", "same-origin");
        headers.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36");
        headers.put("chatgpt-account-id", accountId);
        return headers;
    }

    public JsonNode loadTokenData() {
        ObjectNode snapshot = cachedTokenData;
        if (snapshot != null) {
            return snapshot.deepCopy();
        }

        tokenLock.lock();
        try {
            if (cachedTokenData == null) {
                Path tokenFile = resolveTokenFile();
                if (!Files.exists(tokenFile)) {
                    throw new ApiException(
                            ErrorCode.CONFIGURATION_ERROR,
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "chatgptOauthKey.json 파일을 찾을 수 없습니다."
                    );
                }

                try {
                    JsonNode root = objectMapper.readTree(Files.readString(tokenFile, StandardCharsets.UTF_8));
                    if (!(root instanceof ObjectNode objectNode)) {
                        throw new ApiException(
                                ErrorCode.CONFIGURATION_ERROR,
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "chatgptOauthKey.json 형식이 올바르지 않습니다."
                        );
                    }
                    cachedTokenData = objectNode;
                    cachedTokenFile = tokenFile;
                } catch (IOException exception) {
                    throw new ApiException(
                            ErrorCode.CONFIGURATION_ERROR,
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "chatgptOauthKey.json 파일을 읽을 수 없습니다."
                    );
                }
            }
            return cachedTokenData.deepCopy();
        } finally {
            tokenLock.unlock();
        }
    }

    private String getAccessToken() {
        tokenLock.lock();
        try {
            if (cachedTokenData == null) {
                loadTokenData();
            }

            String accessToken = cachedTokenData.path("tokens").path("access_token").asText("");
            if (StringUtils.hasText(accessToken) && !isTokenExpired(accessToken)) {
                return accessToken;
            }

            String refreshToken = cachedTokenData.path("tokens").path("refresh_token").asText("");
            if (!StringUtils.hasText(refreshToken)) {
                throw new ApiException(
                        ErrorCode.CONFIGURATION_ERROR,
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "ChatGPT refresh token이 없습니다."
                );
            }

            ObjectNode refreshed = refreshAccessToken(refreshToken);
            ObjectNode tokens = (ObjectNode) cachedTokenData.with("tokens");
            tokens.put("access_token", refreshed.path("access_token").asText(""));
            if (refreshed.hasNonNull("id_token")) {
                tokens.put("id_token", refreshed.path("id_token").asText());
            }
            if (refreshed.hasNonNull("refresh_token")) {
                tokens.put("refresh_token", refreshed.path("refresh_token").asText());
            }
            cachedTokenData.put("last_refresh", Instant.now().toString());
            saveTokenData(cachedTokenData);
            return tokens.path("access_token").asText("");
        } finally {
            tokenLock.unlock();
        }
    }

    private ObjectNode refreshAccessToken(String refreshToken) {
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put(
                "redirect_uri",
                "com.openai.chat://auth0.openai.com/ios/com.openai.chat/callback"
        );
        requestBody.put("grant_type", "refresh_token");
        requestBody.put("client_id", properties.getClientId());
        requestBody.put("refresh_token", refreshToken);

        HttpRequest request = HttpRequest.newBuilder(URI.create(properties.getAuthUrl()))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(properties.getReadTimeoutSeconds()))
                .POST(HttpRequest.BodyPublishers.ofString(writeJson(requestBody)))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                throw new ApiException(
                        ErrorCode.EXTERNAL_API_ERROR,
                        HttpStatus.BAD_GATEWAY,
                        "ChatGPT OAuth 토큰 갱신에 실패했습니다."
                );
            }
            JsonNode body = objectMapper.readTree(response.body());
            if (body instanceof ObjectNode objectNode) {
                return objectNode;
            }
            throw new ApiException(
                    ErrorCode.EXTERNAL_API_ERROR,
                    HttpStatus.BAD_GATEWAY,
                    "ChatGPT OAuth 응답 형식이 올바르지 않습니다."
            );
        } catch (IOException exception) {
            throw new ApiException(
                    ErrorCode.EXTERNAL_API_ERROR,
                    HttpStatus.BAD_GATEWAY,
                    "ChatGPT OAuth 토큰 요청 중 오류가 발생했습니다."
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ApiException(
                    ErrorCode.EXTERNAL_API_ERROR,
                    HttpStatus.BAD_GATEWAY,
                    "ChatGPT OAuth 토큰 요청이 중단되었습니다."
            );
        }
    }

    private boolean isTokenExpired(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return true;
            }

            byte[] decodedPayload = Base64.getUrlDecoder().decode(parts[1]);
            JsonNode payload = objectMapper.readTree(decodedPayload);
            long expiresAt = payload.path("exp").asLong(0L);
            if (expiresAt <= 0L) {
                return true;
            }

            Instant safeExpiry = Instant.ofEpochSecond(expiresAt).minus(EXPIRY_SAFETY_WINDOW);
            return Instant.now().isAfter(safeExpiry);
        } catch (Exception exception) {
            return true;
        }
    }

    private void saveTokenData(ObjectNode data) {
        try {
            Files.writeString(
                    resolveTokenFile(),
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data),
                    StandardCharsets.UTF_8
            );
        } catch (IOException exception) {
            throw new ApiException(
                    ErrorCode.CONFIGURATION_ERROR,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "갱신된 ChatGPT 토큰 파일을 저장할 수 없습니다."
            );
        }
    }

    private Path resolveTokenFile() {
        if (cachedTokenFile != null) {
            return cachedTokenFile;
        }

        String envPath = System.getenv("CHATGPT_OAUTH_TOKEN_FILE");
        Path workingDirectory = Path.of("").toAbsolutePath().normalize();
        List<Path> candidates = new ArrayList<>();
        candidates.add(resolveCandidate(envPath, workingDirectory));
        candidates.add(resolveCandidate(properties.getOauthTokenFile(), workingDirectory));
        candidates.add(workingDirectory.resolve("chatgptOauthKey.json").normalize());
        candidates.add(workingDirectory.resolve("..").resolve("chatgptOauthKey.json").normalize());

        for (Path candidate : candidates) {
            if (candidate != null && Files.exists(candidate)) {
                return candidate;
            }
        }

        return candidates.stream()
                .filter(path -> path != null)
                .findFirst()
                .orElse(workingDirectory.resolve("chatgptOauthKey.json").normalize());
    }

    private Path resolveCandidate(String rawPath, Path workingDirectory) {
        if (!StringUtils.hasText(rawPath)) {
            return null;
        }

        Path candidate = Path.of(rawPath.trim());
        if (!candidate.isAbsolute()) {
            candidate = workingDirectory.resolve(candidate);
        }
        return candidate.normalize();
    }

    private String writeJson(JsonNode node) {
        try {
            return objectMapper.writeValueAsString(node);
        } catch (IOException exception) {
            throw new ApiException(
                    ErrorCode.EXTERNAL_API_ERROR,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "ChatGPT 요청 본문 직렬화에 실패했습니다."
            );
        }
    }
}
