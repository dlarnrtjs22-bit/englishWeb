package com.nativeflow.backend.chatgpt.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nativeflow.backend.chatgpt.config.ChatGptProperties;
import com.nativeflow.backend.chatgpt.dto.ChatGptDtos;
import com.nativeflow.backend.common.exception.ApiException;
import com.nativeflow.backend.common.exception.ErrorCode;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ChatGptProxyService {

    private final ChatGptProperties properties;
    private final ChatGptTokenService tokenService;
    private final ChatGptProofService proofService;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final List<ChatGptDtos.CodexModelDto> codexModels;

    public ChatGptProxyService(
            ChatGptProperties properties,
            ChatGptTokenService tokenService,
            ChatGptProofService proofService,
            ObjectMapper objectMapper
    ) {
        this.properties = properties;
        this.tokenService = tokenService;
        this.proofService = proofService;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(properties.getConnectTimeoutSeconds()))
                .build();
        this.codexModels = List.of(
                new ChatGptDtos.CodexModelDto(
                        properties.getDefaultCodexModel(),
                        "GPT-5.3 Codex Spark",
                        "Ultra-fast coding model for lightweight assistant tasks.",
                        "low",
                        List.of(
                                new ChatGptDtos.SupportedEffortDto("low", "Fast responses with lighter reasoning"),
                                new ChatGptDtos.SupportedEffortDto("medium", "Balances speed and reasoning depth"),
                                new ChatGptDtos.SupportedEffortDto("high", "Greater reasoning depth for complex problems"),
                                new ChatGptDtos.SupportedEffortDto("xhigh", "Extra high reasoning depth")
                        )
                )
        );
    }

    public ChatGptDtos.ChatGptStatusResponse getStatus() {
        return tokenService.getStatus();
    }

    public JsonNode fetchModels() {
        return sendJsonRequest(
                "GET",
                properties.getChatgptBaseUrl() + "/models?history_and_training_disabled=false",
                tokenService.getBaseHeaders(),
                null
        );
    }

    public List<ChatGptDtos.CodexModelDto> getCodexModels() {
        return codexModels;
    }

    public JsonNode fetchConversations(int offset, int limit) {
        return sendJsonRequest(
                "GET",
                properties.getChatgptBaseUrl() + "/conversations?offset=" + offset + "&limit=" + limit + "&order=updated",
                tokenService.getBaseHeaders(),
                null
        );
    }

    public JsonNode deleteConversation(String conversationId) {
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("is_visible", false);
        return sendJsonRequest(
                "PATCH",
                properties.getChatgptBaseUrl() + "/conversation/" + conversationId,
                tokenService.getBaseHeaders(),
                requestBody
        );
    }

    public ChatGptDtos.SentenceFeedbackResult generateSentenceFeedback(
            String sourceText,
            String targetText,
            String nuanceNote,
            String exampleSentence,
            String exampleTranslation,
            String userSentence
    ) {
        Map<String, String> headers = new LinkedHashMap<>(tokenService.getBaseHeaders());
        headers.put("openai-beta", "responses-api-v1");

        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", properties.getDefaultCodexModel());
        body.put(
                "instructions",
                """
                You are an English sentence correction assistant for Korean learners.
                Review the learner sentence in the context of the study card.
                Return only a JSON object with these keys:
                perfect (boolean),
                headline (string, short Korean),
                message (string, 1-2 Korean sentences),
                correctedSentence (string, empty if the original is already excellent),
                tips (array of up to 3 short Korean strings).
                If the sentence is already natural and correct, set perfect to true.
                If the target expression is missing or unnatural, suggest one corrected sentence that uses it naturally.
                If the learner writes in Korean or mostly Korean, treat it as "I want to say this in English."
                In that case, do not treat the Korean sentence as already correct.
                Convert the Korean meaning into one natural English sentence.
                Try to use the target expression when it fits naturally.
                If the learner writes in Korean or mostly Korean, set perfect to false.
                If the learner sentence is already acceptable and natural English, do not force a rewrite just because another version sounds slightly different.
                Only correct when there is a clear grammar, spelling, tense, word choice, or naturalness issue.
                Do not return markdown. Do not wrap the JSON in code fences.
                """
        );
        body.set("input", buildSentenceFeedbackInput(
                sourceText,
                targetText,
                nuanceNote,
                exampleSentence,
                exampleTranslation,
                userSentence
        ));
        ObjectNode reasoning = objectMapper.createObjectNode();
        reasoning.put("effort", "low");
        reasoning.put("summary", "auto");
        body.set("reasoning", reasoning);
        body.put("stream", true);
        body.put("store", false);
        body.put("tool_choice", "none");
        body.put("parallel_tool_calls", false);
        body.set("tools", objectMapper.createArrayNode());

        String rawText = collectStreamedText(
                properties.getCodexBaseUrl() + "/responses",
                headers,
                body
        );

        return parseSentenceFeedback(rawText, userSentence);
    }

    public ChatGptDtos.DiaryFeedbackResult generateDiaryFeedback(String rawContent) {
        Map<String, String> headers = new LinkedHashMap<>(tokenService.getBaseHeaders());
        headers.put("openai-beta", "responses-api-v1");

        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", properties.getDefaultCodexModel());
        body.put(
                "instructions",
                """
                You are an English diary correction assistant for Korean learners.
                Review the diary and return only one JSON object.
                Required keys:
                perfect (boolean)
                headline (string, short Korean)
                summary (string, 1-2 Korean sentences)
                correctedContent (string, full corrected diary)
                lines (array of objects with originalLine, correctedLine, translationLine)
                keywords (array of strings formatted like "English phrase : Korean meaning")
                tips (array of short Korean tips)
                advice (array of short Korean advice)
                Rules:
                - First decide whether the diary is already grammatically correct, spelled correctly, and natural for everyday English.
                - If the diary contains Korean or is mostly Korean, interpret it as the learner expressing ideas they do not yet know how to write in English.
                - In that case, rewrite it into natural everyday English instead of treating it as already correct.
                - If the diary contains Korean or is mostly Korean, set perfect to false.
                - If the diary is already correct and natural, do not rewrite it just for style.
                - If the diary is already good, set perfect to true.
                - When perfect is true:
                  - headline should be a praise-style Korean sentence.
                  - summary should briefly explain why the writing is natural and good.
                  - correctedContent should stay the same as the original meaning, and lines should not introduce unnecessary changes.
                  - tips and advice should encourage the learner instead of forcing corrections.
                - Only correct when there is a clear grammar, spelling, tense, word choice, or naturalness issue.
                - Do not mark a sentence as wrong if it is already acceptable and natural.
                - Do not make cosmetic rewrites just because another version sounds slightly different.
                - Split the diary into natural corrected lines.
                - Every correctedLine must have a matching Korean translationLine directly explaining that line.
                - For keywords, always include the Korean meaning in the same string.
                  Example: "work from home : 재택근무를 하다"
                - If a line is already good, it is acceptable for correctedLine to stay almost identical to the original line.
                - Keep the output clean and practical.
                - Do not add markdown, numbering, or code fences.
                """
        );
        body.set("input", buildDiaryFeedbackInput(rawContent));
        ObjectNode reasoning = objectMapper.createObjectNode();
        reasoning.put("effort", "low");
        reasoning.put("summary", "auto");
        body.set("reasoning", reasoning);
        body.put("stream", true);
        body.put("store", false);
        body.put("tool_choice", "none");
        body.put("parallel_tool_calls", false);
        body.set("tools", objectMapper.createArrayNode());

        String rawText = collectStreamedText(
                properties.getCodexBaseUrl() + "/responses",
                headers,
                body
        );

        return parseDiaryFeedback(rawText);
    }

    public void streamChatMessage(ChatGptDtos.ChatMessageRequest request, OutputStream outputStream) {
        Map<String, String> baseHeaders = tokenService.getBaseHeaders();
        ChatGptProofService.ChatRequirements requirements = proofService.getChatRequirements(baseHeaders);

        Map<String, String> headers = new LinkedHashMap<>(baseHeaders);
        headers.put("accept", "text/event-stream");
        headers.put("openai-sentinel-chat-requirements-token", requirements.chatToken());
        if (StringUtils.hasText(requirements.proofToken())) {
            headers.put("openai-sentinel-proof-token", requirements.proofToken());
        }

        String model = StringUtils.hasText(request.model())
                ? request.model().trim()
                : properties.getDefaultChatModel();
        String parentId = StringUtils.hasText(request.parentMessageId())
                ? request.parentMessageId().trim()
                : UUID.randomUUID().toString();

        ObjectNode body = objectMapper.createObjectNode();
        body.put("action", "next");
        body.set("messages", buildChatMessages(request.message()));
        body.put("parent_message_id", parentId);
        body.put("model", model);
        body.put("timezone_offset_min", -540);
        body.put("timezone", "Asia/Seoul");
        body.put("history_and_training_disabled", false);
        ObjectNode conversationMode = objectMapper.createObjectNode();
        conversationMode.put("kind", "primary_assistant");
        body.set("conversation_mode", conversationMode);
        body.put("force_paragen", false);
        body.put("force_paragen_model_slug", "");
        body.put("force_rate_limit", false);
        body.put("force_use_sse", true);
        body.put("reset_rate_limits", false);
        body.set("suggestions", objectMapper.createArrayNode());
        body.set("supported_encodings", objectMapper.createArrayNode());
        body.set("system_hints", objectMapper.createArrayNode());
        body.put("websocket_request_id", UUID.randomUUID().toString());
        body.set("client_contextual_info", buildClientContext());
        if (StringUtils.hasText(request.conversationId())) {
            body.put("conversation_id", request.conversationId().trim());
        }

        streamRequest(
                properties.getChatgptBaseUrl() + "/conversation",
                headers,
                body,
                outputStream
        );
    }

    public void streamCodexMessage(ChatGptDtos.CodexMessageRequest request, OutputStream outputStream) {
        Map<String, String> headers = new LinkedHashMap<>(tokenService.getBaseHeaders());
        headers.put("accept", "text/event-stream");
        headers.put("openai-beta", "responses-api-v1");

        String model = StringUtils.hasText(request.model())
                ? request.model().trim()
                : properties.getDefaultCodexModel();
        String effectiveEffort = resolveReasoningEffort(model, request.reasoningEffort());

        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", model);
        body.put(
                "instructions",
                StringUtils.hasText(request.instructions())
                        ? request.instructions().trim()
                        : "You are a helpful coding assistant. Respond clearly and concisely."
        );
        body.set("input", buildCodexInput(request.conversationHistory()));
        ObjectNode reasoning = objectMapper.createObjectNode();
        reasoning.put("effort", effectiveEffort);
        reasoning.put("summary", "auto");
        body.set("reasoning", reasoning);
        body.put("stream", true);
        body.put("store", false);
        body.put("tool_choice", "none");
        body.put("parallel_tool_calls", false);
        body.set("tools", objectMapper.createArrayNode());
        ArrayNode include = objectMapper.createArrayNode();
        include.add("reasoning.encrypted_content");
        body.set("include", include);

        streamRequest(
                properties.getCodexBaseUrl() + "/responses",
                headers,
                body,
                outputStream
        );
    }

    private JsonNode sendJsonRequest(String method, String url, Map<String, String> headers, JsonNode requestBody) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(properties.getReadTimeoutSeconds()));

        headers.forEach(builder::header);

        if (requestBody == null) {
            switch (method) {
                case "GET" -> builder.GET();
                case "DELETE" -> builder.DELETE();
                default -> builder.method(method, HttpRequest.BodyPublishers.noBody());
            }
        } else {
            builder.method(method, HttpRequest.BodyPublishers.ofString(writeJson(requestBody)));
        }

        try {
            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                throw new ApiException(
                        ErrorCode.EXTERNAL_API_ERROR,
                        HttpStatus.BAD_GATEWAY,
                        "ChatGPT API 요청에 실패했습니다: " + response.body()
                );
            }
            return objectMapper.readTree(response.body());
        } catch (IOException exception) {
            throw new ApiException(
                    ErrorCode.EXTERNAL_API_ERROR,
                    HttpStatus.BAD_GATEWAY,
                    "ChatGPT API 응답을 읽는 중 오류가 발생했습니다."
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ApiException(
                    ErrorCode.EXTERNAL_API_ERROR,
                    HttpStatus.BAD_GATEWAY,
                    "ChatGPT API 요청이 중단되었습니다."
            );
        }
    }

    private String collectStreamedText(String url, Map<String, String> headers, JsonNode requestBody) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(properties.getReadTimeoutSeconds()))
                .POST(HttpRequest.BodyPublishers.ofString(writeJson(requestBody)));

        headers.forEach(builder::header);

        try {
            HttpResponse<java.io.InputStream> response = httpClient.send(
                    builder.build(),
                    HttpResponse.BodyHandlers.ofInputStream()
            );

            if (response.statusCode() != 200) {
                String errorBody = new String(response.body().readAllBytes(), StandardCharsets.UTF_8);
                throw new ApiException(
                        ErrorCode.EXTERNAL_API_ERROR,
                        HttpStatus.BAD_GATEWAY,
                        "ChatGPT API 요청에 실패했습니다: " + errorBody
                );
            }

            StringBuilder builderText = new StringBuilder();
            String doneText = "";

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.startsWith("data: ")) {
                        continue;
                    }

                    String payload = line.substring(6).trim();
                    if (payload.isEmpty() || "[DONE]".equals(payload)) {
                        continue;
                    }

                    JsonNode event = objectMapper.readTree(payload);
                    String type = event.path("type").asText("");
                    if ("response.output_text.delta".equals(type)) {
                        builderText.append(event.path("delta").asText(""));
                    } else if ("response.output_text.done".equals(type)) {
                        doneText = event.path("text").asText(doneText);
                    }
                }
            }

            return builderText.length() > 0 ? builderText.toString().trim() : doneText.trim();
        } catch (IOException exception) {
            throw new ApiException(
                    ErrorCode.EXTERNAL_API_ERROR,
                    HttpStatus.BAD_GATEWAY,
                    "ChatGPT SSE 응답을 읽는 중 오류가 발생했습니다."
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ApiException(
                    ErrorCode.EXTERNAL_API_ERROR,
                    HttpStatus.BAD_GATEWAY,
                    "ChatGPT SSE 요청이 중단되었습니다."
            );
        }
    }

    private void streamRequest(String url, Map<String, String> headers, JsonNode requestBody, OutputStream outputStream) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(properties.getReadTimeoutSeconds()))
                .POST(HttpRequest.BodyPublishers.ofString(writeJson(requestBody)));

        headers.forEach(builder::header);

        try {
            HttpResponse<java.io.InputStream> response = httpClient.send(
                    builder.build(),
                    HttpResponse.BodyHandlers.ofInputStream()
            );

            if (response.statusCode() != 200) {
                String errorBody = new String(response.body().readAllBytes(), StandardCharsets.UTF_8);
                throw new ApiException(
                        ErrorCode.EXTERNAL_API_ERROR,
                        HttpStatus.BAD_GATEWAY,
                        "ChatGPT 스트리밍 요청에 실패했습니다: " + errorBody
                );
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    outputStream.write((line + "\n").getBytes(StandardCharsets.UTF_8));
                    outputStream.flush();
                }
            }
        } catch (IOException exception) {
            throw new ApiException(
                    ErrorCode.EXTERNAL_API_ERROR,
                    HttpStatus.BAD_GATEWAY,
                    "ChatGPT 스트리밍 중 오류가 발생했습니다."
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ApiException(
                    ErrorCode.EXTERNAL_API_ERROR,
                    HttpStatus.BAD_GATEWAY,
                    "ChatGPT 스트리밍 요청이 중단되었습니다."
            );
        }
    }

    private ArrayNode buildChatMessages(String message) {
        ArrayNode messages = objectMapper.createArrayNode();
        ObjectNode messageNode = objectMapper.createObjectNode();
        messageNode.put("id", UUID.randomUUID().toString());
        ObjectNode author = objectMapper.createObjectNode();
        author.put("role", "user");
        messageNode.set("author", author);
        ObjectNode content = objectMapper.createObjectNode();
        content.put("content_type", "text");
        ArrayNode parts = objectMapper.createArrayNode();
        parts.add(message);
        content.set("parts", parts);
        messageNode.set("content", content);
        messageNode.set("metadata", objectMapper.createObjectNode());
        messages.add(messageNode);
        return messages;
    }

    private ObjectNode buildClientContext() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        ObjectNode context = objectMapper.createObjectNode();
        context.put("is_dark_mode", true);
        context.put("time_since_loaded", random.nextInt(50, 501));
        context.put("page_height", random.nextInt(500, 1001));
        context.put("page_width", random.nextInt(1000, 2001));
        context.put("pixel_ratio", 1.5);
        context.put("screen_height", random.nextInt(800, 1201));
        context.put("screen_width", random.nextInt(1200, 2201));
        return context;
    }

    private ArrayNode buildCodexInput(List<ChatGptDtos.ConversationTurnRequest> history) {
        ArrayNode input = objectMapper.createArrayNode();
        for (ChatGptDtos.ConversationTurnRequest message : history) {
            boolean isUser = "user".equalsIgnoreCase(message.role());
            ObjectNode node = objectMapper.createObjectNode();
            node.put("role", isUser ? "user" : "assistant");
            ArrayNode content = objectMapper.createArrayNode();
            ObjectNode contentNode = objectMapper.createObjectNode();
            contentNode.put("type", isUser ? "input_text" : "output_text");
            contentNode.put("text", message.content());
            content.add(contentNode);
            node.set("content", content);
            input.add(node);
        }
        return input;
    }

    private ArrayNode buildSentenceFeedbackInput(
            String sourceText,
            String targetText,
            String nuanceNote,
            String exampleSentence,
            String exampleTranslation,
            String userSentence
    ) {
        ArrayNode input = objectMapper.createArrayNode();
        ObjectNode message = objectMapper.createObjectNode();
        message.put("role", "user");
        ArrayNode content = objectMapper.createArrayNode();
        ObjectNode contentNode = objectMapper.createObjectNode();
        contentNode.put(
                "type",
                "input_text"
        );
        contentNode.put(
                "text",
                """
                [학습 카드 정보]
                한국어 의미: %s
                목표 표현: %s
                뉘앙스: %s
                예문: %s
                예문 해석: %s

                [학습자가 쓴 문장]
                %s
                """.formatted(
                        sourceText,
                        targetText,
                        nuanceNote,
                        exampleSentence,
                        exampleTranslation,
                        userSentence
                )
        );
        content.add(contentNode);
        message.set("content", content);
        input.add(message);
        return input;
    }

    private ArrayNode buildDiaryFeedbackInput(String rawContent) {
        ArrayNode input = objectMapper.createArrayNode();
        ObjectNode message = objectMapper.createObjectNode();
        message.put("role", "user");
        ArrayNode content = objectMapper.createArrayNode();
        ObjectNode contentNode = objectMapper.createObjectNode();
        contentNode.put("type", "input_text");
        contentNode.put("text", rawContent);
        content.add(contentNode);
        message.set("content", content);
        input.add(message);
        return input;
    }

    private String resolveReasoningEffort(String model, String requestedEffort) {
        if (StringUtils.hasText(requestedEffort)) {
            return requestedEffort.trim();
        }

        return codexModels.stream()
                .filter(candidate -> candidate.slug().equalsIgnoreCase(model))
                .findFirst()
                .map(ChatGptDtos.CodexModelDto::defaultEffort)
                .orElse("medium");
    }

    private String extractOutputText(JsonNode response) {
        StringBuilder builder = new StringBuilder();
        JsonNode output = response.path("output");

        if (output.isArray()) {
            for (JsonNode item : output) {
                if (!"message".equals(item.path("type").asText())) {
                    continue;
                }

                JsonNode content = item.path("content");
                if (!content.isArray()) {
                    continue;
                }

                for (JsonNode part : content) {
                    if ("output_text".equals(part.path("type").asText())) {
                        builder.append(part.path("text").asText(""));
                    }
                }
            }
        }

        return builder.toString().trim();
    }

    private ChatGptDtos.SentenceFeedbackResult parseSentenceFeedback(String rawText, String userSentence) {
        String trimmed = rawText == null ? "" : rawText.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replace("```json", "").replace("```", "").trim();
        }

        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end > start) {
            String candidate = trimmed.substring(start, end + 1);
            try {
                JsonNode json = objectMapper.readTree(candidate);
                List<String> tips = new java.util.ArrayList<>();
                JsonNode tipsNode = json.path("tips");
                if (tipsNode.isArray()) {
                    for (JsonNode tip : tipsNode) {
                        String value = tip.asText("").trim();
                        if (!value.isEmpty()) {
                            tips.add(value);
                        }
                    }
                }

                boolean perfect = json.path("perfect").asBoolean(false);
                String correctedSentence = json.path("correctedSentence").asText("");
                if (perfect && correctedSentence.isBlank()) {
                    correctedSentence = userSentence;
                }

                return new ChatGptDtos.SentenceFeedbackResult(
                        perfect,
                        json.path("headline").asText(perfect ? "완벽해요" : "이렇게 다듬어보세요"),
                        json.path("message").asText(perfect ? "문장이 자연스럽고 의미도 잘 전달돼요." : "문장을 조금 더 자연스럽게 다듬어봤어요."),
                        correctedSentence,
                        tips
                );
            } catch (IOException ignored) {
                // Fall through to text fallback.
            }
        }

        return new ChatGptDtos.SentenceFeedbackResult(
                false,
                "AI 피드백",
                trimmed.isBlank() ? "문장을 다시 한 번 확인해보세요." : trimmed,
                userSentence,
                List.of()
        );
    }

    private ChatGptDtos.DiaryFeedbackResult parseDiaryFeedback(String rawText) {
        String trimmed = rawText == null ? "" : rawText.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replace("```json", "").replace("```", "").trim();
        }

        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end > start) {
            String candidate = trimmed.substring(start, end + 1);
            try {
                JsonNode json = objectMapper.readTree(candidate);
                List<ChatGptDtos.DiaryFeedbackLineResult> lines = new java.util.ArrayList<>();
                if (json.path("lines").isArray()) {
                    for (JsonNode line : json.path("lines")) {
                        String correctedLine = line.path("correctedLine").asText("").trim();
                        if (correctedLine.isEmpty()) {
                            continue;
                        }
                        lines.add(new ChatGptDtos.DiaryFeedbackLineResult(
                                line.path("originalLine").asText(""),
                                correctedLine,
                                line.path("translationLine").asText("")
                        ));
                    }
                }
                String correctedContent = json.path("correctedContent").asText("").trim();
                if (correctedContent.isEmpty() && !lines.isEmpty()) {
                    correctedContent = lines.stream()
                            .map(ChatGptDtos.DiaryFeedbackLineResult::correctedLine)
                            .filter(text -> !text.isBlank())
                            .collect(java.util.stream.Collectors.joining("\n"));
                }

                return new ChatGptDtos.DiaryFeedbackResult(
                        json.path("perfect").asBoolean(false),
                        json.path("headline").asText("AI 첨삭 결과"),
                        json.path("summary").asText("문장을 더 자연스럽게 다듬었습니다."),
                        correctedContent,
                        properties.getDefaultCodexModel(),
                        lines,
                        readTextList(json.path("keywords")),
                        readTextList(json.path("tips")),
                        readTextList(json.path("advice"))
                );
            } catch (IOException ignored) {
                // fall through
            }
        }

        return new ChatGptDtos.DiaryFeedbackResult(
                false,
                "AI 첨삭 결과",
                trimmed.isBlank() ? "일기 내용을 다시 한 번 확인해주세요." : trimmed,
                "",
                properties.getDefaultCodexModel(),
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );
    }

    private List<String> readTextList(JsonNode node) {
        List<String> values = new java.util.ArrayList<>();
        if (node.isArray()) {
            for (JsonNode item : node) {
                String value = item.asText("").trim();
                if (!value.isEmpty()) {
                    values.add(value);
                }
            }
        }
        return values;
    }

    private String writeJson(JsonNode node) {
        try {
            return objectMapper.writeValueAsString(node);
        } catch (IOException exception) {
            throw new ApiException(
                    ErrorCode.EXTERNAL_API_ERROR,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "ChatGPT 요청 직렬화에 실패했습니다."
            );
        }
    }
}
