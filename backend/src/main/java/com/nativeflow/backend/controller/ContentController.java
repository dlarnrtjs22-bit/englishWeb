package com.nativeflow.backend.controller;

import com.nativeflow.backend.dto.ApiResponses;
import com.nativeflow.backend.service.MockApiService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ContentController {

    private final MockApiService mockApiService;

    public ContentController(MockApiService mockApiService) {
        this.mockApiService = mockApiService;
    }

    @GetMapping("/dashboard")
    public ApiResponses.DashboardResponse dashboard() {
        return mockApiService.getDashboard();
    }

    @GetMapping("/series")
    public List<ApiResponses.SeriesSummaryDto> series() {
        return mockApiService.getSeriesList();
    }

    @GetMapping("/series/{seriesId}")
    public ApiResponses.SeriesDetailResponse seriesDetail(@PathVariable String seriesId) {
        return mockApiService.getSeriesDetail(seriesId);
    }

    @GetMapping("/learning-items/{itemId}")
    public ApiResponses.LearningItemResponse learningItem(@PathVariable String itemId) {
        return mockApiService.getLearningItem(itemId);
    }

    @GetMapping("/reviews/queue")
    public ApiResponses.ReviewQueueResponse reviewQueue() {
        return mockApiService.getReviewQueue();
    }

    @GetMapping("/favorites")
    public ApiResponses.FavoritesResponse favorites() {
        return mockApiService.getFavorites();
    }

    @GetMapping("/settings")
    public ApiResponses.SettingsResponse settings() {
        return mockApiService.getSettings();
    }
}
