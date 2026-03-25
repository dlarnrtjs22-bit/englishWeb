package com.nativeflow.backend.learning.mapper;

import com.nativeflow.backend.learning.model.LearningSessionCandidateRow;
import com.nativeflow.backend.learning.model.LearningSessionCurrentRow;
import com.nativeflow.backend.learning.model.LearningSessionEntity;
import java.time.OffsetDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LearningSessionMapper {

    String insertSession(
            @Param("userId") String userId,
            @Param("sessionType") String sessionType,
            @Param("sessionSource") String sessionSource,
            @Param("totalSteps") int totalSteps
    );

    void insertSessionItem(
            @Param("sessionId") String sessionId,
            @Param("learningItemId") String learningItemId,
            @Param("sequenceNo") int sequenceNo
    );

    List<LearningSessionCandidateRow> findStudyCandidates(
            @Param("userId") String userId,
            @Param("seriesId") String seriesId,
            @Param("limit") int limit
    );

    List<LearningSessionCandidateRow> findReviewCandidates(
            @Param("userId") String userId,
            @Param("dueBefore") OffsetDateTime dueBefore
    );

    List<LearningSessionCandidateRow> findFavoriteCandidates(
            @Param("userId") String userId,
            @Param("itemId") String itemId
    );

    LearningSessionEntity findSession(
            @Param("sessionId") String sessionId,
            @Param("userId") String userId
    );

    LearningSessionCurrentRow findCurrentItem(@Param("sessionId") String sessionId);

    Integer findPendingCount(@Param("sessionId") String sessionId);

    Integer findMaxSequence(@Param("sessionId") String sessionId);

    void markAnswerResult(
            @Param("sessionItemId") String sessionItemId,
            @Param("isCorrect") boolean isCorrect
    );

    void requeueItem(
            @Param("sessionItemId") String sessionItemId,
            @Param("sequenceNo") int sequenceNo,
            @Param("lastRating") String lastRating
    );

    void completeItem(
            @Param("sessionItemId") String sessionItemId,
            @Param("queueStatus") String queueStatus,
            @Param("lastRating") String lastRating
    );

    void incrementSessionTotals(
            @Param("sessionId") String sessionId,
            @Param("totalStepsDelta") int totalStepsDelta,
            @Param("completedStepsDelta") int completedStepsDelta
    );

    void markSessionCompleted(@Param("sessionId") String sessionId);

    Integer countItemsByStatus(
            @Param("sessionId") String sessionId,
            @Param("queueStatus") String queueStatus
    );
}
