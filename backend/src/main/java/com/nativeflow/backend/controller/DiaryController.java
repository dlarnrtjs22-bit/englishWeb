package com.nativeflow.backend.controller;

import com.nativeflow.backend.common.security.AuthenticatedUser;
import com.nativeflow.backend.common.security.CurrentUser;
import com.nativeflow.backend.diary.dto.DiaryDtos;
import com.nativeflow.backend.diary.service.DiaryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/diary")
public class DiaryController {

    private final DiaryService diaryService;

    public DiaryController(DiaryService diaryService) {
        this.diaryService = diaryService;
    }

    @GetMapping("/entries/{entryDate}")
    public DiaryDtos.DiaryEntryResponse entry(
            @CurrentUser AuthenticatedUser authenticatedUser,
            @PathVariable String entryDate
    ) {
        return diaryService.getDiaryEntry(authenticatedUser.userId(), entryDate);
    }

    @PostMapping("/entries/feedback")
    public DiaryDtos.DiaryFeedbackResponse feedback(
            @CurrentUser AuthenticatedUser authenticatedUser,
            @Valid @RequestBody DiaryDtos.DiaryFeedbackRequest request
    ) {
        return diaryService.generateFeedback(authenticatedUser.userId(), request);
    }

    @PutMapping("/entries/{entryDate}")
    public DiaryDtos.DiaryEntryResponse save(
            @CurrentUser AuthenticatedUser authenticatedUser,
            @PathVariable String entryDate,
            @Valid @RequestBody DiaryDtos.SaveDiaryEntryRequest request
    ) {
        return diaryService.saveDiaryEntry(authenticatedUser.userId(), entryDate, request);
    }

    @GetMapping("/calendar")
    public DiaryDtos.DiaryCalendarResponse calendar(
            @CurrentUser AuthenticatedUser authenticatedUser,
            @RequestParam String from,
            @RequestParam String to
    ) {
        return diaryService.getCalendar(authenticatedUser.userId(), from, to);
    }

    @GetMapping("/history")
    public DiaryDtos.DiaryHistoryResponse history(
            @CurrentUser AuthenticatedUser authenticatedUser,
            @RequestParam(defaultValue = "50") int limit
    ) {
        return diaryService.getHistory(authenticatedUser.userId(), limit);
    }
}
