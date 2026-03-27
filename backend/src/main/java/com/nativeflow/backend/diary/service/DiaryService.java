package com.nativeflow.backend.diary.service;

import com.nativeflow.backend.chatgpt.dto.ChatGptDtos;
import com.nativeflow.backend.chatgpt.service.ChatGptProxyService;
import com.nativeflow.backend.common.exception.ApiException;
import com.nativeflow.backend.common.exception.ErrorCode;
import com.nativeflow.backend.diary.dto.DiaryDtos;
import com.nativeflow.backend.diary.mapper.DiaryMapper;
import com.nativeflow.backend.diary.model.DiaryEntryRow;
import com.nativeflow.backend.diary.model.DiaryFeedbackLineRow;
import com.nativeflow.backend.diary.model.DiaryFeedbackNoteRow;
import com.nativeflow.backend.diary.model.DiaryHistoryRow;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DiaryService {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private final DiaryMapper diaryMapper;
    private final ChatGptProxyService chatGptProxyService;

    public DiaryService(DiaryMapper diaryMapper, ChatGptProxyService chatGptProxyService) {
        this.diaryMapper = diaryMapper;
        this.chatGptProxyService = chatGptProxyService;
    }

    public DiaryDtos.DiaryEntryResponse getDiaryEntry(String userId, String entryDate) {
        LocalDate parsedDate = parseEntryDate(entryDate);
        DiaryEntryRow entry = diaryMapper.findDiaryEntryByDate(userId, parsedDate);

        if (entry == null) {
            return new DiaryDtos.DiaryEntryResponse(parsedDate.format(DATE_FORMATTER), false, "", null);
        }

        return new DiaryDtos.DiaryEntryResponse(
                parsedDate.format(DATE_FORMATTER),
                true,
                entry.getRawContent(),
                toFeedbackResponse(entry)
        );
    }

    public DiaryDtos.DiaryCalendarResponse getCalendar(String userId, String fromDate, String toDate) {
        LocalDate from = parseEntryDate(fromDate);
        LocalDate to = parseEntryDate(toDate);
        if (from.isAfter(to)) {
            throw new ApiException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "날짜 범위가 올바르지 않습니다.");
        }

        List<String> writtenDates = diaryMapper.findWrittenDates(userId, from, to).stream()
                .map(date -> date.format(DATE_FORMATTER))
                .toList();
        return new DiaryDtos.DiaryCalendarResponse(writtenDates);
    }

    public DiaryDtos.DiaryHistoryResponse getHistory(String userId, int limit) {
        int effectiveLimit = Math.min(Math.max(limit, 1), 100);
        List<DiaryHistoryRow> rows = diaryMapper.findDiaryHistory(userId, effectiveLimit);

        return new DiaryDtos.DiaryHistoryResponse(
                rows.stream()
                        .map(row -> new DiaryDtos.DiaryHistoryItemResponse(
                                row.getEntryDate().format(DATE_FORMATTER),
                                safeText(row.getRawSnippet()),
                                safeText(row.getCorrectedSnippet()),
                                splitKeywords(row.getKeywords())
                        ))
                        .toList()
        );
    }

    public DiaryDtos.DiaryFeedbackResponse generateFeedback(
            String userId,
            DiaryDtos.DiaryFeedbackRequest request
    ) {
        LocalDate entryDate = parseEntryDate(request.entryDate());
        validateEntryDate(entryDate);
        ChatGptDtos.DiaryFeedbackResult feedback = chatGptProxyService.generateDiaryFeedback(request.rawContent().trim());
        return toFeedbackResponse(feedback);
    }

    @Transactional
    public DiaryDtos.DiaryEntryResponse saveDiaryEntry(
            String userId,
            String entryDate,
            DiaryDtos.SaveDiaryEntryRequest request
    ) {
        LocalDate parsedDate = parseEntryDate(entryDate);
        validateEntryDate(parsedDate);

        DiaryEntryRow existing = diaryMapper.findDiaryEntryByDate(userId, parsedDate);
        OffsetDateTime correctedAt = resolveCorrectedAt(request.feedback());
        String entryId;

        if (existing == null) {
            entryId = diaryMapper.insertDiaryEntry(
                    userId,
                    parsedDate,
                    request.rawContent().trim(),
                    request.feedback() != null ? safeText(request.feedback().headline()) : null,
                    request.feedback() != null ? safeText(request.feedback().summary()) : null,
                    request.feedback() != null ? safeText(request.feedback().correctedContent()) : null,
                    request.feedback() != null ? safeText(request.feedback().modelName()) : null,
                    request.feedback() != null && request.feedback().perfect(),
                    correctedAt
            );
        } else {
            entryId = existing.getId();
            diaryMapper.updateDiaryEntry(
                    entryId,
                    request.rawContent().trim(),
                    request.feedback() != null ? safeText(request.feedback().headline()) : null,
                    request.feedback() != null ? safeText(request.feedback().summary()) : null,
                    request.feedback() != null ? safeText(request.feedback().correctedContent()) : null,
                    request.feedback() != null ? safeText(request.feedback().modelName()) : null,
                    request.feedback() != null && request.feedback().perfect(),
                    correctedAt
            );
        }

        diaryMapper.deleteDiaryFeedbackLines(entryId);
        diaryMapper.deleteDiaryFeedbackNotes(entryId);

        if (request.feedback() != null) {
            for (int index = 0; index < request.feedback().lines().size(); index++) {
                DiaryDtos.DiaryFeedbackLinePayload line = request.feedback().lines().get(index);
                diaryMapper.insertDiaryFeedbackLine(
                        entryId,
                        index,
                        safeText(line.originalLine()),
                        line.correctedLine().trim(),
                        safeText(line.translationLine())
                );
            }

            insertNotes(entryId, "keyword", request.feedback().keywords());
            insertNotes(entryId, "tip", request.feedback().tips());
            insertNotes(entryId, "advice", request.feedback().advice());
        }

        return getDiaryEntry(userId, entryDate);
    }

    private void insertNotes(String entryId, String noteType, List<String> notes) {
        if (notes == null) {
            return;
        }

        for (int index = 0; index < notes.size(); index++) {
            String content = safeText(notes.get(index)).trim();
            if (!content.isEmpty()) {
                diaryMapper.insertDiaryFeedbackNote(entryId, noteType, index, content);
            }
        }
    }

    private DiaryDtos.DiaryFeedbackResponse toFeedbackResponse(DiaryEntryRow entry) {
        List<DiaryFeedbackLineRow> lines = diaryMapper.findDiaryFeedbackLines(entry.getId());
        List<DiaryFeedbackNoteRow> notes = diaryMapper.findDiaryFeedbackNotes(entry.getId());

        return new DiaryDtos.DiaryFeedbackResponse(
                entry.isPerfect(),
                safeText(entry.getFeedbackHeadline()),
                safeText(entry.getFeedbackSummary()),
                safeText(entry.getCorrectedContent()),
                safeText(entry.getModelName()),
                entry.getLastCorrectedAt() != null ? entry.getLastCorrectedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : null,
                lines.stream()
                        .map(line -> new DiaryDtos.DiaryFeedbackLineDto(
                                safeText(line.getOriginalLine()),
                                safeText(line.getCorrectedLine()),
                                safeText(line.getTranslationLine())
                        ))
                        .toList(),
                filterNotes(notes, "keyword"),
                filterNotes(notes, "tip"),
                filterNotes(notes, "advice")
        );
    }

    private DiaryDtos.DiaryFeedbackResponse toFeedbackResponse(ChatGptDtos.DiaryFeedbackResult feedback) {
        return new DiaryDtos.DiaryFeedbackResponse(
                feedback.perfect(),
                feedback.headline(),
                feedback.summary(),
                feedback.correctedContent(),
                feedback.modelName(),
                OffsetDateTime.now(SEOUL).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                feedback.lines().stream()
                        .map(line -> new DiaryDtos.DiaryFeedbackLineDto(
                                safeText(line.originalLine()),
                                safeText(line.correctedLine()),
                                safeText(line.translationLine())
                        ))
                        .toList(),
                feedback.keywords(),
                feedback.tips(),
                feedback.advice()
        );
    }

    private List<String> filterNotes(List<DiaryFeedbackNoteRow> notes, String type) {
        List<String> values = new ArrayList<>();
        for (DiaryFeedbackNoteRow note : notes) {
            if (type.equals(note.getNoteType())) {
                values.add(note.getContent());
            }
        }
        return values;
    }

    private List<String> splitKeywords(String keywords) {
        if (keywords == null || keywords.isBlank()) {
            return List.of();
        }

        String[] parts = keywords.split("\\|\\|");
        List<String> values = new ArrayList<>();
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                values.add(trimmed);
            }
        }
        return values;
    }

    private LocalDate parseEntryDate(String rawDate) {
        try {
            return LocalDate.parse(rawDate, DATE_FORMATTER);
        } catch (Exception exception) {
            throw new ApiException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "날짜 형식이 올바르지 않습니다.");
        }
    }

    private void validateEntryDate(LocalDate entryDate) {
        LocalDate today = LocalDate.now(SEOUL);
        if (entryDate.isAfter(today)) {
            throw new ApiException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "미래의 일기는 작성할 수 없습니다.");
        }
    }

    private OffsetDateTime resolveCorrectedAt(DiaryDtos.DiaryFeedbackPayload feedback) {
        if (feedback == null || feedback.correctedAt() == null || feedback.correctedAt().isBlank()) {
            return null;
        }

        try {
            return OffsetDateTime.parse(feedback.correctedAt(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        } catch (Exception exception) {
            return OffsetDateTime.now(SEOUL);
        }
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }
}
