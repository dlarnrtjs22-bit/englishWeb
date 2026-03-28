package com.nativeflow.backend.controller;

import com.nativeflow.backend.common.security.AuthenticatedUser;
import com.nativeflow.backend.common.security.CurrentUser;
import com.nativeflow.backend.content.dto.ContentActionDtos;
import com.nativeflow.backend.content.service.ContentService;
import com.nativeflow.backend.dto.ApiResponses;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class ContentController {

    private final ContentService contentService;

    public ContentController(ContentService contentService) {
        this.contentService = contentService;
    }

    @GetMapping("/dashboard")
    public ApiResponses.DashboardResponse dashboard(@CurrentUser AuthenticatedUser authenticatedUser) {
        ApiResponses.DashboardResponse response = contentService.getDashboard(authenticatedUser.userId(), authenticatedUser.name());
        int progressPercent = response.activeSeries().isEmpty()
                ? 0
                : (int) Math.round(response.activeSeries().stream()
                        .mapToInt(ApiResponses.SeriesSummaryDto::progress)
                        .average()
                        .orElse(0.0));

        String progressMessage = response.activeSeries().isEmpty()
                ? "먼저 오늘의 학습 시리즈를 살펴보세요."
                : progressPercent <= 0
                        ? "가볍게 한 문제부터 시작해보세요."
                        : progressPercent >= 100
                                ? "오늘 목표를 모두 채웠습니다."
                                : "이어서 학습할 준비가 되어 있습니다.";

        return new ApiResponses.DashboardResponse(
                response.userName(),
                progressPercent,
                progressMessage,
                response.reviewSummary(),
                response.activeSeries(),
                response.recommendedSeries(),
                response.stats()
        );
    }

    @GetMapping("/series")
    public List<ApiResponses.SeriesSummaryDto> series(@CurrentUser AuthenticatedUser authenticatedUser) {
        return contentService.getSeriesCards(authenticatedUser.userId());
    }

    @GetMapping("/series/{seriesId}")
    public ApiResponses.SeriesDetailResponse seriesDetail(
            @CurrentUser AuthenticatedUser authenticatedUser,
            @PathVariable String seriesId
    ) {
        return contentService.getSeriesDetail(authenticatedUser.userId(), seriesId);
    }

    @GetMapping("/learning-items/{itemId}")
    public ApiResponses.LearningItemResponse learningItem(
            @CurrentUser AuthenticatedUser authenticatedUser,
            @PathVariable String itemId,
            @RequestParam(required = false, defaultValue = "study") String mode
    ) {
        return contentService.getLearningItem(authenticatedUser.userId(), itemId, mode);
    }

    @PostMapping("/learning-items/{itemId}/check-answer")
    public ApiResponses.CheckAnswerResponse checkAnswer(
            @CurrentUser AuthenticatedUser authenticatedUser,
            @PathVariable String itemId,
            @Valid @RequestBody ContentActionDtos.CheckAnswerRequest request
    ) {
        return contentService.checkAnswer(authenticatedUser.userId(), itemId, request.typedAnswer(), request.mode());
    }

    @PostMapping("/learning-items/{itemId}/sentence-feedback")
    public ApiResponses.SentenceFeedbackResponse sentenceFeedback(
            @CurrentUser AuthenticatedUser authenticatedUser,
            @PathVariable String itemId,
            @Valid @RequestBody ContentActionDtos.SentenceFeedbackRequest request
    ) {
        return contentService.getSentenceFeedback(
                authenticatedUser.userId(),
                itemId,
                request.sentence(),
                request.mode()
        );
    }

    @PostMapping("/learning-items/{itemId}/favorite")
    public ApiResponses.FavoriteToggleResponse favorite(
            @CurrentUser AuthenticatedUser authenticatedUser,
            @PathVariable String itemId
    ) {
        return contentService.favoriteItem(authenticatedUser.userId(), itemId);
    }

    @DeleteMapping("/learning-items/{itemId}/favorite")
    public ApiResponses.FavoriteToggleResponse unfavorite(
            @CurrentUser AuthenticatedUser authenticatedUser,
            @PathVariable String itemId
    ) {
        return contentService.unfavoriteItem(authenticatedUser.userId(), itemId);
    }

    @PostMapping("/learning-items/{itemId}/review")
    public ApiResponses.ReviewScheduleResponse submitReview(
            @CurrentUser AuthenticatedUser authenticatedUser,
            @PathVariable String itemId,
            @Valid @RequestBody ContentActionDtos.ReviewRequest request
    ) {
        return contentService.submitReview(authenticatedUser.userId(), itemId, request.result(), request.mode());
    }

    @PostMapping("/learning-items/{itemId}/reset")
    public ApiResponses.ActionSuccessResponse resetLearningItem(
            @CurrentUser AuthenticatedUser authenticatedUser,
            @PathVariable String itemId
    ) {
        return contentService.resetLearningItem(authenticatedUser.userId(), itemId);
    }

    @GetMapping("/reviews/queue")
    public ApiResponses.ReviewQueueResponse reviewQueue(@CurrentUser AuthenticatedUser authenticatedUser) {
        return contentService.getReviewQueue(authenticatedUser.userId());
    }

    @GetMapping("/favorites")
    public ApiResponses.FavoritesResponse favorites(@CurrentUser AuthenticatedUser authenticatedUser) {
        return contentService.getFavorites(authenticatedUser.userId());
    }

    @GetMapping("/packs/{packId}/random-item")
    public ApiResponses.RandomLearningItemResponse randomLearningItem(@PathVariable String packId) {
        return contentService.getRandomLearningItemByPack(packId);
    }

    @GetMapping("/packs/{packId}/study-queue")
    public ApiResponses.RandomLearningQueueResponse studyLearningQueue(
            @CurrentUser AuthenticatedUser authenticatedUser,
            @PathVariable String packId
    ) {
        return contentService.getStudyLearningQueueByPack(authenticatedUser.userId(), packId);
    }

    @GetMapping("/packs/{packId}/random-queue")
    public ApiResponses.RandomLearningQueueResponse randomLearningQueue(@PathVariable String packId) {
        return contentService.getRandomLearningQueueByPack(packId);
    }
}
