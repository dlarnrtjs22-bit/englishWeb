package com.nativeflow.backend.content.mapper;

import java.time.OffsetDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ContentCommandMapper {

    String findSeriesIdBySlug(@Param("slug") String slug);

    List<String> findPublishedSeriesIds();

    void subscribeSeries(@Param("userId") String userId, @Param("seriesId") String seriesId);

    void ensureSeriesProgress(@Param("userId") String userId, @Param("seriesId") String seriesId);

    List<String> findAcceptedAnswers(@Param("itemId") String itemId);

    String findTargetText(@Param("itemId") String itemId);

    void insertUserAnswer(
            @Param("userId") String userId,
            @Param("itemId") String itemId,
            @Param("typedAnswer") String typedAnswer,
            @Param("normalizedAnswer") String normalizedAnswer,
            @Param("isCorrect") boolean isCorrect
    );

    void favoriteItem(@Param("userId") String userId, @Param("itemId") String itemId);

    void unfavoriteItem(@Param("userId") String userId, @Param("itemId") String itemId);

    void excludeItem(@Param("userId") String userId, @Param("itemId") String itemId);

    void unexcludeItem(@Param("userId") String userId, @Param("itemId") String itemId);

    void upsertReviewSchedule(
            @Param("userId") String userId,
            @Param("itemId") String itemId,
            @Param("lastResult") String lastResult,
            @Param("intervalDays") int intervalDays,
            @Param("repetitionCount") int repetitionCount,
            @Param("easeFactor") double easeFactor,
            @Param("nextReviewAt") OffsetDateTime nextReviewAt
    );

    void insertReviewLog(
            @Param("userId") String userId,
            @Param("itemId") String itemId,
            @Param("result") String result,
            @Param("newIntervalDays") int newIntervalDays,
            @Param("newEaseFactor") double newEaseFactor
    );

    void updateSeriesProgressCounts(@Param("userId") String userId, @Param("seriesId") String seriesId);

    String findSeriesIdByLearningItem(@Param("itemId") String itemId);
}
