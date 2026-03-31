package com.nativeflow.backend.content.service;

import com.nativeflow.backend.chatgpt.dto.ChatGptDtos;
import com.nativeflow.backend.chatgpt.service.ChatGptProxyService;
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
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ContentService {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final ChatGptProxyService chatGptProxyService;
    private final ContentCommandMapper contentCommandMapper;
    private final ContentQueryMapper contentQueryMapper;

    public ContentService(
            ChatGptProxyService chatGptProxyService,
            ContentCommandMapper contentCommandMapper,
            ContentQueryMapper contentQueryMapper
    ) {
        this.chatGptProxyService = chatGptProxyService;
        this.contentCommandMapper = contentCommandMapper;
        this.contentQueryMapper = contentQueryMapper;
    }

    public ApiResponses.DashboardResponse getDashboard(String userId, String userName) {
        List<SeriesCardRow> subscribed = contentQueryMapper.findSubscribedSeriesCards(userId);
        List<SeriesCardRow> allSeries = contentQueryMapper.findSeriesCards(userId);
        DashboardStatsRow stats = contentQueryMapper.findDashboardStats(userId);

        List<ApiResponses.SeriesSummaryDto> activeSeries = subscribed.stream()
                .limit(2)
                .map(row -> toSeriesSummary(row, deriveSubtitle(row), null))
                .toList();

        int progressPercent = activeSeries.isEmpty()
                ? 0
                : (int) Math.round(activeSeries.stream()
                        .mapToInt(ApiResponses.SeriesSummaryDto::progress)
                        .average()
                        .orElse(0.0));

        String progressMessage = activeSeries.isEmpty()
                ? "먼저 오늘의 학습 시리즈를 살펴보세요."
                : progressPercent <= 0
                        ? "가볍게 한 문제부터 시작해보세요."
                        : progressPercent >= 100
                                ? "오늘 목표를 모두 채웠습니다."
                                : "이어서 학습할 준비가 되어 있습니다.";

        List<ApiResponses.SeriesSummaryDto> recommendedSeries = allSeries.stream()
                .filter(row -> !row.isSubscribed())
                .limit(3)
                .map(row -> toSeriesSummary(row, deriveSubtitle(row), "Business English".equals(row.getTitle()) ? "Premium" : null))
                .toList();

        return new ApiResponses.DashboardResponse(
                userName,
                progressPercent,
                activeSeries.isEmpty() ? "먼저 오늘의 시리즈를 하나 골라보세요." : "이어서 학습할 준비가 되어 있습니다.",
                new ApiResponses.ReviewSummaryDto(
                        stats.getDueCount(),
                        stats.getDueCount() > 0 ? "완료한 단어를 다시 복습할 수 있습니다." : "아직 복습할 완료 단어가 없습니다.",
                        stats.getDueCount() > 0
                                ? List.of("완료 단어 전체를 기준으로 복습이 시작됩니다.", "오늘복습완료 또는 다시복습하기로 진행합니다.")
                                : List.of("학습 화면에서 완료(복습) 처리한 단어가 복습 큐에 쌓입니다.", "가볍게 한 문제부터 시작해보세요.")
                ),
                activeSeries,
                recommendedSeries,
                List.of(
                        new ApiResponses.StatDto("Total Streak", stats.getStreakDays() + "일"),
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
        List<SeriesPackRow> packs = contentQueryMapper.findSeriesPacks(userId, seriesId);
        int totalItems = packs.stream().mapToInt(SeriesPackRow::getItemCount).sum();
        int completedItems = packs.stream().mapToInt(SeriesPackRow::getCompletedItemCount).sum();
        int overallProgress = totalItems == 0
                ? 0
                : (int) Math.round((completedItems * 100.0) / totalItems);
        boolean allUnitsCompleted = totalItems > 0 && completedItems >= totalItems;

        return new ApiResponses.SeriesDetailResponse(
                detail.getId(),
                detail.getTitle(),
                detail.getDescription(),
                detail.getCategoryLabel(),
                detail.getThumbnailUrl(),
                "NativeFlow Coach",
                inferLevel(detail.getCategoryLabel()),
                "2026.03.25",
                overallProgress,
                packs.isEmpty()
                        ? "아직 학습 유닛이 없습니다."
                        : allUnitsCompleted
                                ? "오늘 학습할 단어를 모두 마쳤습니다."
                                : "남은 유닛을 선택하면 바로 학습을 시작할 수 있습니다.",
                "표현을 이해하고 직접 써보는 흐름으로 구성했습니다.",
                buildTags(detail.getCategoryLabel()),
                packs.stream()
                        .map(pack -> new ApiResponses.SeriesPackDto(
                                pack.getId(),
                                "Unit " + pack.getOrderIndex(),
                                pack.getTitle(),
                                pack.getDescription(),
                                pack.getItemCount(),
                                pack.getItemCount() == 0
                                        ? 0
                                        : (int) Math.round((pack.getCompletedItemCount() * 100.0) / pack.getItemCount()),
                                pack.getRemainingItemCount() == 0 && pack.getItemCount() > 0,
                                false,
                                pack.getRemainingItemCount() == 0
                                        ? "오늘 완료"
                                        : pack.getCompletedItemCount() > 0
                                                ? "이어 학습"
                                                : "바로 시작 가능",
                                resolvePackEntryItemId(pack)
                        ))
                        .toList()
        );
    }

    public ApiResponses.LearningItemResponse getLearningItem(String userId, String itemId, String mode) {
        LearningItemRow row = contentQueryMapper.findLearningItem(userId, itemId, normalizeMode(mode));

        return new ApiResponses.LearningItemResponse(
                row.getId(),
                row.getSourceText(),
                row.getTargetText(),
                row.getNuanceNote(),
                row.getImagePath(),
                row.getExampleSentence(),
                row.getExampleTranslation(),
                "문장을 직접 만들어보며 표현을 익혀보세요.",
                new ApiResponses.LearningProgressDto(row.getCurrent(), row.getTotal())
        );
    }

    public ApiResponses.CheckAnswerResponse checkAnswer(String userId, String itemId, String typedAnswer, String mode) {
        String rawAnswer = typedAnswer == null ? "" : typedAnswer;
        String normalizedAnswer = normalizeAnswer(typedAnswer);
        String normalizedMode = normalizeMode(mode);
        List<String> acceptedAnswers = contentCommandMapper.findAcceptedAnswers(itemId).stream()
                .map(this::normalizeAnswer)
                .distinct()
                .toList();
        boolean isCorrect = acceptedAnswers.contains(normalizedAnswer);

        if (!"random".equals(normalizedMode)) {
            contentCommandMapper.insertUserAnswer(userId, itemId, rawAnswer, normalizedAnswer, isCorrect);
            syncSeriesProgress(userId, itemId);
        }

        LearningItemRow row = contentQueryMapper.findLearningItem(userId, itemId, normalizedMode);
        return new ApiResponses.CheckAnswerResponse(
                isCorrect,
                row.getTargetText(),
                acceptedAnswers,
                row.getExampleSentence(),
                row.getExampleTranslation()
        );
    }

    public ApiResponses.SentenceFeedbackResponse getSentenceFeedback(
            String userId,
            String itemId,
            String sentence,
            String mode
    ) {
        LearningItemRow row = contentQueryMapper.findLearningItem(userId, itemId, normalizeMode(mode));
        ChatGptDtos.SentenceFeedbackResult feedback = chatGptProxyService.generateSentenceFeedback(
                row.getSourceText(),
                row.getTargetText(),
                row.getNuanceNote(),
                row.getExampleSentence(),
                row.getExampleTranslation(),
                sentence.trim()
        );

        return new ApiResponses.SentenceFeedbackResponse(
                feedback.perfect(),
                feedback.headline(),
                feedback.message(),
                feedback.correctedSentence(),
                feedback.tips()
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

    public ApiResponses.RandomLearningItemResponse getRandomLearningItemByPack(String packId) {
        return new ApiResponses.RandomLearningItemResponse(
                contentQueryMapper.findRandomLearningItemIdByPackId(packId)
        );
    }

    public ApiResponses.RandomLearningQueueResponse getRandomLearningQueueByPack(String packId) {
        return new ApiResponses.RandomLearningQueueResponse(
                contentQueryMapper.findRandomLearningItemIdsByPackId(packId)
        );
    }

    public ApiResponses.RandomLearningQueueResponse getStudyLearningQueueByPack(String userId, String packId) {
        return new ApiResponses.RandomLearningQueueResponse(
                contentQueryMapper.findLearningItemIdsByPackId(userId, packId)
        );
    }

    public ApiResponses.ActionSuccessResponse resetLearningItem(String userId, String itemId) {
        contentCommandMapper.deleteReviewSchedule(userId, itemId);
        contentCommandMapper.deleteCompletedLearningItem(userId, itemId);
        contentCommandMapper.deleteUserAnswersForItem(userId, itemId);
        syncSeriesProgress(userId, itemId);
        return new ApiResponses.ActionSuccessResponse(true);
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
        List<ReviewQueueRow> reviewItems = contentQueryMapper.findDueReviewItems(userId);
        List<ReviewHistoryRow> historyRows = contentQueryMapper.findReviewHistory(userId);
        DashboardStatsRow stats = contentQueryMapper.findDashboardStats(userId);
        int totalReviewCount = stats.getDueCount();
        int remainingReviewCount = reviewItems.size();

        List<ApiResponses.ReviewItemDto> items = reviewItems.stream()
                .map(item -> new ApiResponses.ReviewItemDto(item.getItemId(), item.getSourceText(), item.getContextText(), 1))
                .toList();

        List<ApiResponses.ReviewGroupDto> groups = new ArrayList<>();
        reviewItems.stream()
                .map(ReviewQueueRow::getSeriesTitle)
                .distinct()
                .forEach(seriesTitle -> groups.add(new ApiResponses.ReviewGroupDto(
                        seriesTitle.toLowerCase().replace(" ", "-"),
                        seriesTitle,
                        seriesTitle + " 복습 큐",
                        reviewItems.stream()
                                .filter(item -> seriesTitle.equals(item.getSeriesTitle()))
                                .map(item -> new ApiResponses.ReviewItemDto(item.getItemId(), item.getSourceText(), item.getContextText(), 1))
                                .toList()
                )));

        return new ApiResponses.ReviewQueueResponse(
                items,
                groups,
                List.of(
                        new ApiResponses.ReviewSummaryCardDto("전체 복습 단어", totalReviewCount + "개", "완료 단어 전체 수", "library_books", "warning"),
                        new ApiResponses.ReviewSummaryCardDto("오늘 복습 완료", stats.getReviewedTodayCount() + "개", "오늘 복습 세션에서 처리한 수", "done_all", "neutral"),
                        new ApiResponses.ReviewSummaryCardDto("남은 복습", remainingReviewCount + "개", "지금 다시 볼 수 있는 단어 수", "replay", "mint")
                ),
                historyRows.stream().map(ReviewHistoryRow::getReviewCount).toList()
        );
    }

    public ApiResponses.ReviewScheduleResponse submitReview(String userId, String itemId, String result, String mode) {
        String normalizedResult = result.trim().toLowerCase();
        String normalizedMode = normalizeMode(mode);

        if ("random".equals(normalizedMode)) {
            return new ApiResponses.ReviewScheduleResponse(
                    true,
                    normalizedResult,
                    0,
                    0.0,
                    null,
                    contentQueryMapper.findRandomLearningItemId(itemId)
            );
        }

        if ("review".equals(normalizedMode)) {
            if ("repeat".equals(normalizedResult)) {
                contentCommandMapper.insertReviewLog(userId, itemId, "repeat", 0, 0.0);
                return new ApiResponses.ReviewScheduleResponse(true, normalizedResult, 0, 0.0, null, null);
            }

            if ("review_done".equals(normalizedResult)) {
                contentCommandMapper.touchCompletedLearningItem(userId, itemId);
                contentCommandMapper.insertReviewLog(userId, itemId, "review_done", 0, 0.0);
                syncSeriesProgress(userId, itemId);
                return new ApiResponses.ReviewScheduleResponse(true, normalizedResult, 0, 0.0, null, null);
            }
        }

        if ("study".equals(normalizedMode) && "complete".equals(normalizedResult)) {
            contentCommandMapper.deleteReviewSchedule(userId, itemId);
            contentCommandMapper.upsertCompletedLearningItem(userId, itemId);
            contentCommandMapper.insertReviewLog(userId, itemId, normalizedResult, 0, 0.0);
            syncSeriesProgress(userId, itemId);

            return new ApiResponses.ReviewScheduleResponse(
                    true,
                    normalizedResult,
                    0,
                    0.0,
                    null,
                    selectNextItemId(userId, itemId, normalizedMode, normalizedResult)
            );
        }

        int intervalDays = resolveIntervalDays(normalizedResult, normalizedMode);
        double easeFactor = resolveEaseFactor(normalizedResult, normalizedMode);
        int repetitionCount = resolveRepetitionCount(normalizedResult, normalizedMode);
        OffsetDateTime nextReviewAt = resolveNextReviewAt(normalizedResult, normalizedMode, intervalDays);

        contentCommandMapper.deleteCompletedLearningItem(userId, itemId);
        contentCommandMapper.upsertReviewSchedule(userId, itemId, normalizedResult, intervalDays, repetitionCount, easeFactor, nextReviewAt);
        contentCommandMapper.insertReviewLog(userId, itemId, normalizedResult, intervalDays, easeFactor);
        syncSeriesProgress(userId, itemId);

        return new ApiResponses.ReviewScheduleResponse(
                true,
                normalizedResult,
                intervalDays,
                easeFactor,
                nextReviewAt.toString(),
                selectNextItemId(userId, itemId, normalizedMode, normalizedResult)
        );
    }

    public void syncPublishedSeriesAccess(String userId) {
        List<String> publishedSeriesIds = contentCommandMapper.findPublishedSeriesIds();

        for (String seriesId : publishedSeriesIds) {
            contentCommandMapper.subscribeSeries(userId, seriesId);
            contentCommandMapper.ensureSeriesProgress(userId, seriesId);
            contentCommandMapper.updateSeriesProgressCounts(userId, seriesId);
        }
    }

    private void syncSeriesProgress(String userId, String itemId) {
        String seriesId = contentCommandMapper.findSeriesIdByLearningItem(itemId);

        if (seriesId != null) {
            contentCommandMapper.ensureSeriesProgress(userId, seriesId);
            contentCommandMapper.updateSeriesProgressCounts(userId, seriesId);
        }
    }

    private OffsetDateTime resolveNextReviewAt(String normalizedResult, int intervalDays) {
        LocalDate today = LocalDate.now(SEOUL);

        return switch (normalizedResult) {
            case "month" -> today.plusMonths(1).atStartOfDay(SEOUL).toOffsetDateTime();
            case "year" -> today.plusYears(1).atStartOfDay(SEOUL).toOffsetDateTime();
            default -> today.plusDays(intervalDays).atStartOfDay(SEOUL).toOffsetDateTime();
        };
    }

    private OffsetDateTime resolveNextReviewAt(String normalizedResult, String mode, int intervalDays) {
        if ("minute".equals(normalizedResult)) {
            return OffsetDateTime.now().plusMinutes(1);
        }

        return resolveNextReviewAt(normalizedResult, intervalDays);
    }

    private int resolveIntervalDays(String normalizedResult, String mode) {
        return switch (normalizedResult) {
            case "again" -> 1;
            case "minute" -> 0;
            case "hard" -> 2;
            case "good" -> 4;
            case "easy" -> 7;
            case "month" -> 30;
            case "year" -> 365;
            case "complete" -> 0;
            default -> 1;
        };
    }

    private double resolveEaseFactor(String normalizedResult, String mode) {
        return switch (normalizedResult) {
            case "again" -> 2.1;
            case "minute" -> 2.2;
            case "hard" -> 2.3;
            case "good" -> 2.5;
            case "easy" -> 2.7;
            case "month" -> 3.0;
            case "year" -> 3.2;
            case "complete" -> 0.0;
            default -> 2.5;
        };
    }

    private int resolveRepetitionCount(String normalizedResult, String mode) {
        return switch (normalizedResult) {
            case "again" -> 0;
            case "minute" -> 0;
            case "hard" -> 1;
            case "good" -> 2;
            case "easy" -> 3;
            case "month" -> 4;
            case "year" -> 5;
            case "complete" -> 0;
            default -> 1;
        };
    }

    private String selectNextItemId(String userId, String itemId, String mode, String result) {
        return switch (mode) {
            case "review" -> contentQueryMapper.findNextReviewItemId(userId, itemId);
            case "favorites" -> {
                String nextItemId = contentQueryMapper.findNextFavoriteItemId(userId, itemId);
                if (!"minute".equals(result) && itemId.equals(nextItemId)) {
                    yield null;
                }
                yield nextItemId;
            }
            default -> contentQueryMapper.findNextStudyLearningItemId(userId, itemId);
        };
    }

    private String resolvePackEntryItemId(SeriesPackRow pack) {
        if (pack.getFirstItemId() != null) {
            return pack.getFirstItemId();
        }

        if (pack.getRemainingItemCount() == 0 && pack.getItemCount() > 0) {
            return contentQueryMapper.findRandomLearningItemIdByPackId(pack.getId());
        }

        return null;
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
        return List.of(categoryLabel, "표현 중심", "반복 학습", "한국어 해설");
    }

    private String normalizeMode(String mode) {
        if (mode == null) {
            return "study";
        }

        return switch (mode.trim().toLowerCase()) {
            case "favorites" -> "favorites";
            case "review" -> "review";
            case "random" -> "random";
            default -> "study";
        };
    }


    private String normalizeAnswer(String value) {
        if (value == null) {
            return "";
        }

        return value.trim()
                .toLowerCase()
                .replaceAll("[.?!,;:]+$", "")
                .replaceAll("\\s+", " ");
    }
}
