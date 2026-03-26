package com.nativeflow.backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.nativeflow.backend.chatgpt.dto.ChatGptDtos;
import com.nativeflow.backend.chatgpt.service.ChatGptProxyService;
import com.nativeflow.backend.common.security.AuthenticatedUser;
import com.nativeflow.backend.common.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/api/v1/chatgpt")
public class ChatGptController {

    private final ChatGptProxyService chatGptProxyService;

    public ChatGptController(ChatGptProxyService chatGptProxyService) {
        this.chatGptProxyService = chatGptProxyService;
    }

    @GetMapping("/status")
    public ChatGptDtos.ChatGptStatusResponse status(@CurrentUser AuthenticatedUser authenticatedUser) {
        return chatGptProxyService.getStatus();
    }

    @GetMapping("/models")
    public JsonNode models(@CurrentUser AuthenticatedUser authenticatedUser) {
        return chatGptProxyService.fetchModels();
    }

    @GetMapping("/codex-models")
    public java.util.List<ChatGptDtos.CodexModelDto> codexModels(@CurrentUser AuthenticatedUser authenticatedUser) {
        return chatGptProxyService.getCodexModels();
    }

    @GetMapping("/conversations")
    public JsonNode conversations(
            @CurrentUser AuthenticatedUser authenticatedUser,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "28") int limit
    ) {
        return chatGptProxyService.fetchConversations(offset, limit);
    }

    @DeleteMapping("/conversations/{conversationId}")
    public JsonNode deleteConversation(
            @CurrentUser AuthenticatedUser authenticatedUser,
            @PathVariable String conversationId
    ) {
        return chatGptProxyService.deleteConversation(conversationId);
    }

    @PostMapping(value = "/messages/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<StreamingResponseBody> streamMessage(
            @CurrentUser AuthenticatedUser authenticatedUser,
            @Valid @RequestBody ChatGptDtos.ChatMessageRequest request
    ) {
        StreamingResponseBody stream = outputStream -> chatGptProxyService.streamChatMessage(request, outputStream);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(stream);
    }

    @PostMapping(value = "/codex/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<StreamingResponseBody> streamCodex(
            @CurrentUser AuthenticatedUser authenticatedUser,
            @Valid @RequestBody ChatGptDtos.CodexMessageRequest request
    ) {
        StreamingResponseBody stream = outputStream -> chatGptProxyService.streamCodexMessage(request, outputStream);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(stream);
    }
}
