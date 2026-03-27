package com.nativeflow.backend.diary.mapper;

import com.nativeflow.backend.diary.model.DiaryEntryRow;
import com.nativeflow.backend.diary.model.DiaryFeedbackLineRow;
import com.nativeflow.backend.diary.model.DiaryFeedbackNoteRow;
import com.nativeflow.backend.diary.model.DiaryHistoryRow;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DiaryMapper {

    DiaryEntryRow findDiaryEntryByDate(@Param("userId") String userId, @Param("entryDate") LocalDate entryDate);

    List<DiaryFeedbackLineRow> findDiaryFeedbackLines(@Param("entryId") String entryId);

    List<DiaryFeedbackNoteRow> findDiaryFeedbackNotes(@Param("entryId") String entryId);

    List<LocalDate> findWrittenDates(
            @Param("userId") String userId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    List<DiaryHistoryRow> findDiaryHistory(@Param("userId") String userId, @Param("limit") int limit);

    String insertDiaryEntry(
            @Param("userId") String userId,
            @Param("entryDate") LocalDate entryDate,
            @Param("rawContent") String rawContent,
            @Param("feedbackHeadline") String feedbackHeadline,
            @Param("feedbackSummary") String feedbackSummary,
            @Param("correctedContent") String correctedContent,
            @Param("modelName") String modelName,
            @Param("isPerfect") boolean isPerfect,
            @Param("lastCorrectedAt") OffsetDateTime lastCorrectedAt
    );

    void updateDiaryEntry(
            @Param("entryId") String entryId,
            @Param("rawContent") String rawContent,
            @Param("feedbackHeadline") String feedbackHeadline,
            @Param("feedbackSummary") String feedbackSummary,
            @Param("correctedContent") String correctedContent,
            @Param("modelName") String modelName,
            @Param("isPerfect") boolean isPerfect,
            @Param("lastCorrectedAt") OffsetDateTime lastCorrectedAt
    );

    void deleteDiaryFeedbackLines(@Param("entryId") String entryId);

    void deleteDiaryFeedbackNotes(@Param("entryId") String entryId);

    void insertDiaryFeedbackLine(
            @Param("entryId") String entryId,
            @Param("lineOrder") int lineOrder,
            @Param("originalLine") String originalLine,
            @Param("correctedLine") String correctedLine,
            @Param("translationLine") String translationLine
    );

    void insertDiaryFeedbackNote(
            @Param("entryId") String entryId,
            @Param("noteType") String noteType,
            @Param("noteOrder") int noteOrder,
            @Param("content") String content
    );
}
