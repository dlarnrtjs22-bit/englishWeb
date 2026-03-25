package com.nativeflow.backend.learning.service;

import com.nativeflow.backend.common.exception.ApiException;
import com.nativeflow.backend.common.exception.ErrorCode;
import com.nativeflow.backend.content.mapper.ContentCommandMapper;
import com.nativeflow.backend.content.mapper.ContentQueryMapper;
import com.nativeflow.backend.content.model.LearningItemRow;
import com.nativeflow.backend.dto.ApiResponses;
import com.nativeflow.backend.learning.dto.LearningSessionDtos;
import com.nativeflow.backend.learning.mapper.LearningSessionMapper;
import com.nativeflow.backend.learning.model.LearningSessionCandidateRow;
import com.nativeflow.backend.learning.model.LearningSessionCurrentRow;
import com.nativeflow.backend.learning.model.LearningSessionEntity;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LearningSessionService {

    private static final int DEFAULT_STUDY_LIMIT = 10;
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final LearningSessionMapper learningSessionMapper;
    private final ContentQueryMapper contentQueryMapper;
    private final ContentCommandMapper contentCommandMapper;

    public LearningSessionService(
            LearningSessionMapper learningSessionMapper,
            ContentQueryMapper contentQueryMapper,
            ContentCommandMapper contentCommandMapper
    ) {
        this.learningSessionMapper = learningSessionMapper;
        this.contentQueryMapper = contentQueryMapper;
        this.contentCommandMapper = contentCommandMapper;
    }

    @Transactional
    public LearningSessionDtos.LearningSessionResponse startStudySession(String userId, String seriesId) {
        List<LearningSessionCandidateRow> candidates = learningSessionMapper.findStudyCandidates(userId, seriesId, DEFAULT_STUDY_LIMIT);
        return createSession(userId, "study", "series", candidates, "오늘 학습 완료", "오늘 학습할 새 표현을 모두 확인했습니다.");
    }

    @Transactional
    public LearningSessionDtos.LearningSessionResponse startReviewSession(String userId) {
        OffsetDateTime dueBefore = LocalDate.now(SEOUL).plusDays(1).atStartOfDay(SEOUL).toOffsetDateTime();
        List<LearningSessionCandidateRow> candidates = learningSessionMapper.findReviewCandidates(userId, dueBefore);
        return createSession(userId, "review", "due", candidates, "오늘 복습 완료", "오늘 복습할 카드가 모두 끝났습니다.");
    }

    @Transactional
    public LearningSessionDtos.LearningSessionResponse startFavoriteSession(String userId, String itemId) {
        List<LearningSessionCandidateRow> candidates = learningSessionMapper.findFavoriteCandidates(userId, itemId);
        return createSession(userId, "review", "favorites", candidates, "저장한 표현 복습 완료", "복습할 저장 표현을 모두 확인했습니다.");
    }

    public LearningSessionDtos.LearningSessionResponse getCurrent(String userId, String sessionId) {
        LearningSessionEntity session = requireSession(userId, sessionId);
        LearningSessionCurrentRow current = learningSessionMapper.findCurrentItem(sessionId);

        if (current == null) {
            if (!"completed".equals(session.getStatus())) {
                learningSessionMapper.markSessionCompleted(sessionId);
                session = requireSession(userId, sessionId);
            }

            return new LearningSessionDtos.LearningSessionResponse(
                    session.getId(),
                    session.getSessionType(),
                    session.getSessionSource(),
                    true,
                    new LearningSessionDtos.SessionProgressResponse(session.getTotalSteps(), session.getTotalSteps()),
                    null,
                    new LearningSessionDtos.SessionSummaryResponse(
                            session.getSessionType().equals("review") ? "오늘 복습 완료" : "오늘 학습 완료",
                            session.getSessionSource().equals("favorites")
                                    ? "저장한 표현 복습이 모두 끝났습니다."
                                    : session.getSessionType().equals("review")
                                            ? "복습할 카드가 더 이상 없습니다."
                                            : "오늘 학습할 새 표현을 모두 확인했습니다.",
                            session.getCompletedSteps(),
                            session.getTotalSteps()
                    )
            );
        }

        return toResponse(session, current, false);
    }

    @Transactional
    public ApiResponses.CheckAnswerResponse answer(String userId, String sessionId, String typedAnswer) {
        LearningSessionEntity session = requireSession(userId, sessionId);
        LearningSessionCurrentRow current = requireCurrentItem(session);
        String normalizedAnswer = normalizeAnswer(typedAnswer);
        List<String> acceptedAnswers = contentCommandMapper.findAcceptedAnswers(current.getLearningItemId()).stream()
                .map(this::normalizeAnswer)
                .distinct()
                .toList();
        boolean isCorrect = acceptedAnswers.contains(normalizedAnswer);

        learningSessionMapper.markAnswerResult(current.getSessionItemId(), isCorrect);
        contentCommandMapper.insertUserAnswer(userId, current.getLearningItemId(), typedAnswer, normalizedAnswer, isCorrect);

        LearningItemRow row = contentQueryMapper.findLearningItem(userId, current.getLearningItemId(), "study");
        return new ApiResponses.CheckAnswerResponse(
                isCorrect,
                row.getTargetText(),
                acceptedAnswers,
                row.getExampleSentence(),
                row.getExampleTranslation()
        );
    }

    @Transactional
    public ApiResponses.ReviewScheduleResponse rate(String userId, String sessionId, String result) {
        LearningSessionEntity session = requireSession(userId, sessionId);
        LearningSessionCurrentRow current = requireCurrentItem(session);
        String normalizedResult = result.trim().toLowerCase();

        if ("minute".equals(normalizedResult)) {
            int nextSequence = learningSessionMapper.findMaxSequence(sessionId) + 1;
            learningSessionMapper.requeueItem(current.getSessionItemId(), nextSequence, normalizedResult);
            learningSessionMapper.incrementSessionTotals(sessionId, 1, 0);
            return new ApiResponses.ReviewScheduleResponse(true, normalizedResult, 0, 2.2, null, current.getLearningItemId());
        }

        int intervalDays = switch (normalizedResult) {
            case "again" -> 1;
            case "hard" -> 2;
            case "good" -> 4;
            case "easy" -> 7;
            case "month" -> 30;
            case "year" -> 365;
            case "complete" -> 1;
            default -> 1;
        };
        double easeFactor = switch (normalizedResult) {
            case "again" -> 2.1;
            case "hard" -> 2.3;
            case "good" -> 2.5;
            case "easy" -> 2.7;
            case "month" -> 3.0;
            case "year" -> 3.2;
            case "complete" -> 2.5;
            default -> 2.5;
        };
        int repetitionCount = switch (normalizedResult) {
            case "again" -> 0;
            case "hard" -> 1;
            case "good" -> 2;
            case "easy" -> 3;
            case "month" -> 4;
            case "year" -> 5;
            case "complete" -> 1;
            default -> 1;
        };

        OffsetDateTime nextReviewAt = switch (normalizedResult) {
            case "month" -> LocalDate.now(SEOUL).plusMonths(1).atStartOfDay(SEOUL).toOffsetDateTime();
            case "year" -> LocalDate.now(SEOUL).plusYears(1).atStartOfDay(SEOUL).toOffsetDateTime();
            default -> LocalDate.now(SEOUL).plusDays(intervalDays).atStartOfDay(SEOUL).toOffsetDateTime();
        };

        contentCommandMapper.upsertReviewSchedule(userId, current.getLearningItemId(), normalizedResult, intervalDays, repetitionCount, easeFactor, nextReviewAt);
        contentCommandMapper.insertReviewLog(userId, current.getLearningItemId(), normalizedResult, intervalDays, easeFactor);
        learningSessionMapper.completeItem(current.getSessionItemId(), "done", normalizedResult);
        learningSessionMapper.incrementSessionTotals(sessionId, 0, 1);
        syncSeriesProgress(userId, current.getLearningItemId());

        return new ApiResponses.ReviewScheduleResponse(
                true,
                normalizedResult,
                intervalDays,
                easeFactor,
                nextReviewAt.toString(),
                nextLearningItemId(sessionId)
        );
    }

    private LearningSessionDtos.LearningSessionResponse createSession(
            String userId,
            String sessionType,
            String sessionSource,
            List<LearningSessionCandidateRow> candidates,
            String completedTitle,
            String completedMessage
    ) {
        String sessionId = learningSessionMapper.insertSession(userId, sessionType, sessionSource, candidates.size());

        for (int i = 0; i < candidates.size(); i += 1) {
            learningSessionMapper.insertSessionItem(sessionId, candidates.get(i).getLearningItemId(), i + 1);
        }

        if (candidates.isEmpty()) {
            learningSessionMapper.markSessionCompleted(sessionId);
            return new LearningSessionDtos.LearningSessionResponse(
                    sessionId,
                    sessionType,
                    sessionSource,
                    true,
                    new LearningSessionDtos.SessionProgressResponse(0, 0),
                    null,
                    new LearningSessionDtos.SessionSummaryResponse(completedTitle, completedMessage, 0, 0)
            );
        }

        return getCurrent(userId, sessionId);
    }

    private LearningSessionEntity requireSession(String userId, String sessionId) {
        LearningSessionEntity session = learningSessionMapper.findSession(sessionId, userId);

        if (session == null) {
            throw new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "학습 세션을 찾을 수 없습니다.");
        }

        return session;
    }

    private LearningSessionCurrentRow requireCurrentItem(LearningSessionEntity session) {
        LearningSessionCurrentRow current = learningSessionMapper.findCurrentItem(session.getId());

        if (current == null) {
            throw new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "현재 학습 카드가 없습니다.");
        }

        return current;
    }

    private LearningSessionDtos.LearningSessionResponse toResponse(
            LearningSessionEntity session,
            LearningSessionCurrentRow current,
            boolean completed
    ) {
        return new LearningSessionDtos.LearningSessionResponse(
                session.getId(),
                session.getSessionType(),
                session.getSessionSource(),
                completed,
                new LearningSessionDtos.SessionProgressResponse(session.getCompletedSteps() + 1, session.getTotalSteps()),
                new LearningSessionDtos.SessionCardResponse(
                        current.getLearningItemId(),
                        current.getSourceText(),
                        current.getTargetText(),
                        current.getNuanceNote(),
                        current.getExampleSentence(),
                        current.getExampleTranslation()
                ),
                null
        );
    }

    private String nextLearningItemId(String sessionId) {
        LearningSessionCurrentRow next = learningSessionMapper.findCurrentItem(sessionId);
        return next != null ? next.getLearningItemId() : null;
    }

    private void syncSeriesProgress(String userId, String itemId) {
        String seriesId = contentCommandMapper.findSeriesIdByLearningItem(itemId);

        if (seriesId != null) {
            contentCommandMapper.ensureSeriesProgress(userId, seriesId);
            contentCommandMapper.updateSeriesProgressCounts(userId, seriesId);
        }
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
