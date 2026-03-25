package com.nativeflow.backend.content.service;

import com.nativeflow.backend.content.mapper.ContentCommandMapper;
import com.nativeflow.backend.content.mapper.ContentQueryMapper;
import com.nativeflow.backend.content.model.DashboardStatsRow;
import com.nativeflow.backend.content.model.FavoriteRow;
import com.nativeflow.backend.content.model.LearningItemRow;
import com.nativeflow.backend.content.model.ReviewHistoryRow;
import com.nativeflow.backend.content.model.ReviewQueueRow;
import com.nativeflow.backend.content.model.SeriesCardRow;
import com.nativeflow.backend.content.model.SeriesDetailRow;
import com.nativeflow.backend.content.model.SeriesPackRow;
import com.nativeflow.backend.dto.ApiResponses;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ContentService {

    private final ContentCommandMapper contentCommandMapper;
    private final ContentQueryMapper contentQueryMapper;

    public ContentService(
            ContentCommandMapper contentCommandMapper,
            ContentQueryMapper contentQueryMapper
    ) {
        this.contentCommandMapper = contentCommandMapper;
        this.contentQueryMapper = contentQueryMapper;
    }

    public ApiResponses.DashboardResponse getDashboard(String userId, String userName) {
        List<SeriesCardRow> subscribed = contentQueryMapper.findSubscribedSeriesCards(userId);
        List<SeriesCardRow> allSeries = contentQueryMapper.findSeriesCards(userId);
        DashboardStatsRow stats = contentQueryMapper.findDashboardStats(userId);

        List<ApiResponses.SeriesSummaryDto> recommended = allSeries.stream()
                .filter(row -> !row.isSubscribed())
                .limit(3)
                .map(row -> toSeriesSummary(row, deriveSubtitle(row), "Business English".equals(row.getTitle()) ? "Premium" : null))
                .toList();

        return new ApiResponses.DashboardResponse(
                userName,
                Math.min(100, stats.getDueCount() == 0 ? 0 : 65),
                subscribed.isEmpty() ? "먼저 시리즈를 구독해서 학습 흐름을 시작해보세요." : "오늘 학습 흐름을 이어갈 준비가 되어 있습니다.",
                new ApiResponses.ReviewSummaryDto(
                        stats.getDueCount(),
                        stats.getDueCount() > 0 ? "복습이 필요한 표현이 준비되어 있습니다." : "지금은 복습 대기 항목이 없습니다.",
                        stats.getDueCount() > 0
                                ? List.of("오늘 처리할 복습 항목", "학습 후 다시 큐에 반영됩니다.")
                                : List.of("복습 큐가 비어 있습니다.", "새 학습을 시작해 데이터를 쌓아보세요.")
                ),
                subscribed.stream().limit(2).map(row -> toSeriesSummary(row, deriveSubtitle(row), null)).toList(),
                recommended,
                List.of(
                        new ApiResponses.StatDto("Total Streak", stats.getDueCount() > 0 ? "1일" : "0일"),
                        new ApiResponses.StatDto("Vocabulary", stats.getReviewedDistinctCount() + "개")
                )
        );
    }

    public List<ApiResponses.SeriesSummaryDto> getSeriesCards(String userId) {
        return contentQueryMapper.findSeriesCards(userId).stream()
                .map(row -> toSeriesSummary(row, deriveSubtitle(row), null))
                .toList();
    }

    public ApiResponses.SeriesDetailResponse getSeriesDetail(String userId, String seriesId) {
        SeriesDetailRow detail = contentQueryMapper.findSeriesDetail(userId, seriesId);
        List<SeriesPackRow> packs = contentQueryMapper.findSeriesPacks(seriesId);

        return new ApiResponses.SeriesDetailResponse(
                detail.getId(),
                detail.getTitle(),
                detail.getDescription(),
                detail.getCategoryLabel(),
                detail.getThumbnailUrl(),
                "NativeFlow Coach",
                inferLevel(detail.getCategoryLabel()),
                "2026.03.24",
                detail.getProgress(),
                packs.isEmpty() ? "아직 등록된 학습 유닛이 없습니다." : "학습 유닛을 선택해 바로 시작할 수 있습니다.",
                "표현 중심 학습 흐름으로 구성된 시리즈입니다.",
                buildTags(detail.getCategoryLabel()),
                packs.stream()
                        .map(pack -> new ApiResponses.SeriesPackDto(
                                pack.getId(),
                                "Unit " + pack.getOrderIndex(),
                                pack.getTitle(),
                                pack.getDescription(),
                                pack.getItemCount(),
                                0,
                                false,
                                false,
                                "바로 시작 가능",
                                contentQueryMapper.findFirstLearningItemIdByPackId(pack.getId())
                        ))
                        .toList()
        );
    }

    public ApiResponses.LearningItemResponse getLearningItem(String itemId) {
        LearningItemRow row = contentQueryMapper.findLearningItem(itemId);

        return new ApiResponses.LearningItemResponse(
                row.getId(),
                row.getSourceText(),
                row.getTargetText(),
                row.getNuanceNote(),
                row.getExampleSentence(),
                "AI 첨삭은 다음 단계에서 실제 LLM 피드백과 연결됩니다.",
                new ApiResponses.LearningProgressDto(row.getCurrent(), row.getTotal())
        );
    }

    public ApiResponses.CheckAnswerResponse checkAnswer(String userId, String itemId, String typedAnswer) {
        String normalizedAnswer = normalizeAnswer(typedAnswer);
        List<String> acceptedAnswers = contentCommandMapper.findAcceptedAnswers(itemId).stream()
                .map(this::normalizeAnswer)
                .distinct()
                .toList();
        boolean isCorrect = acceptedAnswers.contains(normalizedAnswer);

        contentCommandMapper.insertUserAnswer(userId, itemId, typedAnswer, normalizedAnswer, isCorrect);
        syncSeriesProgress(userId, itemId);

        LearningItemRow row = contentQueryMapper.findLearningItem(itemId);
        return new ApiResponses.CheckAnswerResponse(
                isCorrect,
                row.getTargetText(),
                acceptedAnswers,
                row.getExampleSentence()
        );
    }

    public ApiResponses.FavoritesResponse getFavorites(String userId) {
        List<FavoriteRow> items = contentQueryMapper.findFavorites(userId);
        return new ApiResponses.FavoritesResponse(
                items.stream()
                        .map(item -> new ApiResponses.FavoriteItemDto(
                                item.getItemId(),
                                item.getSourceText(),
                                item.getTargetText(),
                                item.getSeriesTitle(),
                                item.getPackTitle()
                        ))
                        .toList()
        );
    }

    public ApiResponses.FavoriteToggleResponse favoriteItem(String userId, String itemId) {
        contentCommandMapper.favoriteItem(userId, itemId);
        return new ApiResponses.FavoriteToggleResponse(true, true);
    }

    public ApiResponses.FavoriteToggleResponse unfavoriteItem(String userId, String itemId) {
        contentCommandMapper.unfavoriteItem(userId, itemId);
        return new ApiResponses.FavoriteToggleResponse(true, false);
    }

    public ApiResponses.ReviewQueueResponse getReviewQueue(String userId) {
        List<ReviewQueueRow> dueItems = contentQueryMapper.findDueReviewItems(userId);
        List<ReviewHistoryRow> historyRows = contentQueryMapper.findReviewHistory(userId);
        int dueCount = dueItems.size();

        List<ApiResponses.ReviewItemDto> itemDtos = dueItems.stream()
                .map(item -> new ApiResponses.ReviewItemDto(item.getItemId(), item.getSourceText(), item.getContextText(), 1))
                .toList();

        List<ApiResponses.ReviewGroupDto> groups = new ArrayList<>();
        dueItems.stream()
                .map(ReviewQueueRow::getSeriesTitle)
                .distinct()
                .forEach(seriesTitle -> {
                    List<ApiResponses.ReviewItemDto> groupItems = dueItems.stream()
                            .filter(item -> seriesTitle.equals(item.getSeriesTitle()))
                            .map(item -> new ApiResponses.ReviewItemDto(item.getItemId(), item.getSourceText(), item.getContextText(), 1))
                            .toList();
                    groups.add(new ApiResponses.ReviewGroupDto(
                            seriesTitle.toLowerCase().replace(" ", "-"),
                            seriesTitle,
                            seriesTitle + " 복습 큐",
                            groupItems
                    ));
                });

        return new ApiResponses.ReviewQueueResponse(
                itemDtos,
                groups,
                List.of(
                        new ApiResponses.ReviewSummaryCardDto("오늘 복습 예정", dueCount + "개", "오늘 처리할 복습 수", "alarm", "warning"),
                        new ApiResponses.ReviewSummaryCardDto("이후 일정", Math.max(0, dueCount * 2) + "개", "다음 복습 예정 수", "calendar_month", "neutral"),
                        new ApiResponses.ReviewSummaryCardDto("학습 스트릭", dueCount > 0 ? "1일" : "0일", "연속 학습 흐름", "local_fire_department", "mint")
                ),
                historyRows.stream().map(ReviewHistoryRow::getReviewCount).toList()
        );
    }

    public ApiResponses.ReviewScheduleResponse submitReview(String userId, String itemId, String result) {
        int intervalDays = switch (result) {
            case "again" -> 1;
            case "hard" -> 2;
            case "good" -> 4;
            case "easy" -> 7;
            default -> 1;
        };
        double easeFactor = switch (result) {
            case "again" -> 2.1;
            case "hard" -> 2.3;
            case "good" -> 2.5;
            case "easy" -> 2.7;
            default -> 2.5;
        };
        int repetitionCount = switch (result) {
            case "again" -> 0;
            case "hard" -> 1;
            case "good" -> 2;
            case "easy" -> 3;
            default -> 1;
        };
        OffsetDateTime nextReviewAt = OffsetDateTime.now().plusDays(intervalDays);

        contentCommandMapper.upsertReviewSchedule(userId, itemId, result, intervalDays, repetitionCount, easeFactor, nextReviewAt);
        contentCommandMapper.insertReviewLog(userId, itemId, result, intervalDays, easeFactor);
        syncSeriesProgress(userId, itemId);

        return new ApiResponses.ReviewScheduleResponse(true, result, intervalDays, easeFactor, nextReviewAt.toString());
    }

    public void subscribeStarterSeries(String userId) {
        String starterSeriesId = contentCommandMapper.findSeriesIdBySlug("everyday-english");
        if (starterSeriesId != null) {
            contentCommandMapper.subscribeSeries(userId, starterSeriesId);
            contentCommandMapper.ensureSeriesProgress(userId, starterSeriesId);
            contentCommandMapper.updateSeriesProgressCounts(userId, starterSeriesId);
        }
    }

    private void syncSeriesProgress(String userId, String itemId) {
        String seriesId = contentCommandMapper.findSeriesIdByLearningItem(itemId);
        if (seriesId != null) {
            contentCommandMapper.ensureSeriesProgress(userId, seriesId);
            contentCommandMapper.updateSeriesProgressCounts(userId, seriesId);
        }
    }

    private ApiResponses.SeriesSummaryDto toSeriesSummary(SeriesCardRow row, String subtitle, String badge) {
        return new ApiResponses.SeriesSummaryDto(
                row.getId(),
                row.getTitle(),
                subtitle,
                row.getDescription(),
                row.getCategoryLabel(),
                row.getThumbnailUrl(),
                row.getProgress(),
                row.getPackCount(),
                row.isSubscribed(),
                badge
        );
    }

    private String deriveSubtitle(SeriesCardRow row) {
        return row.getCategoryLabel() + " 시리즈";
    }

    private String inferLevel(String categoryLabel) {
        return switch (categoryLabel) {
            case "학술 영어" -> "Advanced";
            case "비즈니스", "시사 영어" -> "Intermediate";
            default -> "Beginner";
        };
    }

    private List<String> buildTags(String categoryLabel) {
        return List.of(categoryLabel, "표현 중심", "구독 학습", "한국어 해설");
    }

    private String normalizeAnswer(String value) {
        return value == null ? "" : value.trim().toLowerCase().replaceAll("\\s+", " ");
    }
}
