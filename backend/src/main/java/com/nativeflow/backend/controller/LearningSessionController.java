package com.nativeflow.backend.controller;

import com.nativeflow.backend.common.security.AuthenticatedUser;
import com.nativeflow.backend.common.security.CurrentUser;
import com.nativeflow.backend.dto.ApiResponses;
import com.nativeflow.backend.learning.dto.LearningSessionDtos;
import com.nativeflow.backend.learning.service.LearningSessionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/learning-sessions")
public class LearningSessionController {

    private final LearningSessionService learningSessionService;

    public LearningSessionController(LearningSessionService learningSessionService) {
        this.learningSessionService = learningSessionService;
    }

    @PostMapping("/study/start")
    public LearningSessionDtos.LearningSessionResponse startStudy(
            @CurrentUser AuthenticatedUser authenticatedUser,
            @RequestBody(required = false) LearningSessionDtos.StartStudySessionRequest request
    ) {
        return learningSessionService.startStudySession(authenticatedUser.userId(), request != null ? request.seriesId() : null);
    }

    @PostMapping("/review/start")
    public LearningSessionDtos.LearningSessionResponse startReview(@CurrentUser AuthenticatedUser authenticatedUser) {
        return learningSessionService.startReviewSession(authenticatedUser.userId());
    }

    @PostMapping("/favorites/start")
    public LearningSessionDtos.LearningSessionResponse startFavorites(
            @CurrentUser AuthenticatedUser authenticatedUser,
            @RequestBody(required = false) LearningSessionDtos.StartReviewSessionRequest request
    ) {
        return learningSessionService.startFavoriteSession(authenticatedUser.userId(), request != null ? request.itemId() : null);
    }

    @GetMapping("/{sessionId}/current")
    public LearningSessionDtos.LearningSessionResponse current(
            @CurrentUser AuthenticatedUser authenticatedUser,
            @PathVariable String sessionId
    ) {
        return learningSessionService.getCurrent(authenticatedUser.userId(), sessionId);
    }

    @PostMapping("/{sessionId}/answer")
    public ApiResponses.CheckAnswerResponse answer(
            @CurrentUser AuthenticatedUser authenticatedUser,
            @PathVariable String sessionId,
            @Valid @RequestBody LearningSessionDtos.SessionAnswerRequest request
    ) {
        return learningSessionService.answer(authenticatedUser.userId(), sessionId, request.typedAnswer());
    }

    @PostMapping("/{sessionId}/rate")
    public ApiResponses.ReviewScheduleResponse rate(
            @CurrentUser AuthenticatedUser authenticatedUser,
            @PathVariable String sessionId,
            @Valid @RequestBody LearningSessionDtos.SessionRateRequest request
    ) {
        return learningSessionService.rate(authenticatedUser.userId(), sessionId, request.result());
    }
}
