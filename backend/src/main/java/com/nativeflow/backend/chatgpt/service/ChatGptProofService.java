package com.nativeflow.backend.chatgpt.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nativeflow.backend.chatgpt.config.ChatGptProperties;
import com.nativeflow.backend.common.exception.ApiException;
import com.nativeflow.backend.common.exception.ErrorCode;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ChatGptProofService {

    public record ChatRequirements(String chatToken, String proofToken) {
    }

    private static final List<Integer> CPU_CORE_CANDIDATES = List.of(8, 16, 24, 32);

    private final ChatGptProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public ChatGptProofService(ChatGptProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(properties.getConnectTimeoutSeconds()))
                .build();
    }

    public ChatRequirements getChatRequirements(Map<String, String> baseHeaders) {
        String userAgent = baseHeaders.getOrDefault("user-agent", "Mozilla/5.0");
        List<Object> config = generateConfig(userAgent);
        String requirementsToken = getRequirementsToken(config);

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("p", requirementsToken);

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(
                        URI.create(properties.getChatgptBaseUrl() + "/sentinel/chat-requirements"))
                .timeout(Duration.ofSeconds(properties.getReadTimeoutSeconds()))
                .POST(HttpRequest.BodyPublishers.ofString(writeJson(requestBody)));

        baseHeaders.forEach(requestBuilder::header);

        try {
            HttpResponse<String> response = httpClient.send(
                    requestBuilder.build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );

            if (response.statusCode() != 200) {
                throw new ApiException(
                        ErrorCode.EXTERNAL_API_ERROR,
                        HttpStatus.BAD_GATEWAY,
                        "ChatGPT sentinel 토큰을 가져오지 못했습니다."
                );
            }

            JsonNode body = objectMapper.readTree(response.body());
            String chatToken = body.path("token").asText("");
            if (chatToken.isBlank()) {
                throw new ApiException(
                        ErrorCode.EXTERNAL_API_ERROR,
                        HttpStatus.BAD_GATEWAY,
                        "ChatGPT sentinel 응답에 token이 없습니다."
                );
            }

            JsonNode proofOfWork = body.path("proofofwork");
            String proofToken = null;
            if (proofOfWork.path("required").asBoolean(false)) {
                String seed = proofOfWork.path("seed").asText("");
                String difficulty = proofOfWork.path("difficulty").asText("");
                proofToken = getProofToken(seed, difficulty, config);
            }

            return new ChatRequirements(chatToken, proofToken);
        } catch (IOException exception) {
            throw new ApiException(
                    ErrorCode.EXTERNAL_API_ERROR,
                    HttpStatus.BAD_GATEWAY,
                    "ChatGPT sentinel 응답을 읽는 중 오류가 발생했습니다."
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ApiException(
                    ErrorCode.EXTERNAL_API_ERROR,
                    HttpStatus.BAD_GATEWAY,
                    "ChatGPT sentinel 요청이 중단되었습니다."
            );
        }
    }

    private List<Object> generateConfig(String userAgent) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        String parseTime = now.format(DateTimeFormatter.ofPattern(
                "EEE MMM dd uuuu HH:mm:ss 'GMT+0900 (Korean Standard Time)'",
                java.util.Locale.ENGLISH
        ));
        double perfNow = random.nextDouble() * 100000.0;

        List<Object> config = new ArrayList<>();
        config.add(1920 + 1080);
        config.add(parseTime);
        config.add(4294705152L);
        config.add(0);
        config.add(userAgent);
        config.add("");
        config.add("");
        config.add("ko-KR");
        config.add("ko-KR,en-US;q=0.9,en;q=0.8");
        config.add(0);
        config.add("webdriver?false");
        config.add("location");
        config.add("window");
        config.add(perfNow * 1000);
        config.add(UUID.randomUUID().toString());
        config.add("");
        config.add(CPU_CORE_CANDIDATES.get(random.nextInt(CPU_CORE_CANDIDATES.size())));
        config.add(System.currentTimeMillis() - perfNow);
        return config;
    }

    private String getRequirementsToken(List<Object> config) {
        String seed = Double.toString(ThreadLocalRandom.current().nextDouble());
        String answer = generateAnswer(seed, "0fffff", config).answer();
        return "gAAAAAC" + answer;
    }

    private String getProofToken(String seed, String difficulty, List<Object> config) {
        String answer = generateAnswer(seed, difficulty, config).answer();
        return "gAAAAAB" + answer;
    }

    private AnswerResult generateAnswer(String seed, String difficulty, List<Object> config) {
        int difficultyLength = difficulty.length() / 2;
        byte[] targetDifficulty = hexToBytes(difficulty);
        MessageDigest digest = getSha3Digest();

        String configPart1 = toJsonArray(config.subList(0, 3));
        configPart1 = configPart1.substring(0, configPart1.length() - 1) + ", ";

        String configPart2 = toJsonArray(config.subList(4, 9));
        configPart2 = ", " + configPart2.substring(1, configPart2.length() - 1) + ", ";

        String configPart3 = toJsonArray(config.subList(10, config.size()));
        configPart3 = ", " + configPart3.substring(1);

        for (int index = 0; index < 500000; index++) {
            String finalJson = configPart1 + index + configPart2 + (index >> 1) + configPart3;
            String encoded = Base64.getEncoder().encodeToString(finalJson.getBytes(StandardCharsets.UTF_8));
            digest.reset();
            byte[] hash = digest.digest((seed + encoded).getBytes(StandardCharsets.UTF_8));
            if (comparePrefix(hash, targetDifficulty, difficultyLength) <= 0) {
                return new AnswerResult(encoded, true);
            }
        }

        String fallback = "wQ8Lk5FbGpA2NcR9dShT6gYjU7VxZ4D"
                + Base64.getEncoder().encodeToString(("\"" + seed + "\"").getBytes(StandardCharsets.UTF_8));
        return new AnswerResult(fallback, false);
    }

    private String toJsonArray(List<Object> values) {
        StringBuilder builder = new StringBuilder("[");
        for (int index = 0; index < values.size(); index++) {
            if (index > 0) {
                builder.append(", ");
            }
            builder.append(writeJsonValue(values.get(index)));
        }
        builder.append("]");
        return builder.toString();
    }

    private String writeJsonValue(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (IOException exception) {
            throw new ApiException(
                    ErrorCode.EXTERNAL_API_ERROR,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "ChatGPT proof 토큰 직렬화에 실패했습니다."
            );
        }
    }

    private String writeJson(JsonNode node) {
        try {
            return objectMapper.writeValueAsString(node);
        } catch (IOException exception) {
            throw new ApiException(
                    ErrorCode.EXTERNAL_API_ERROR,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "ChatGPT sentinel 요청 직렬화에 실패했습니다."
            );
        }
    }

    private MessageDigest getSha3Digest() {
        try {
            return MessageDigest.getInstance("SHA3-512");
        } catch (NoSuchAlgorithmException exception) {
            throw new ApiException(
                    ErrorCode.EXTERNAL_API_ERROR,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "SHA3-512 알고리즘을 사용할 수 없습니다."
            );
        }
    }

    private int comparePrefix(byte[] actual, byte[] target, int length) {
        for (int index = 0; index < length; index++) {
            int actualByte = Byte.toUnsignedInt(actual[index]);
            int targetByte = Byte.toUnsignedInt(target[index]);
            if (actualByte != targetByte) {
                return Integer.compare(actualByte, targetByte);
            }
        }
        return 0;
    }

    private byte[] hexToBytes(String hex) {
        int length = hex.length();
        byte[] result = new byte[length / 2];
        for (int index = 0; index < length; index += 2) {
            result[index / 2] = (byte) Integer.parseInt(hex.substring(index, index + 2), 16);
        }
        return result;
    }

    private record AnswerResult(String answer, boolean solved) {
    }
}
