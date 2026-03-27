package com.nativeflow.backend.diary.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public class DiaryEntryRow {

    private String id;
    private LocalDate entryDate;
    private String rawContent;
    private String feedbackHeadline;
    private String feedbackSummary;
    private String correctedContent;
    private String modelName;
    private boolean perfect;
    private OffsetDateTime lastCorrectedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(LocalDate entryDate) {
        this.entryDate = entryDate;
    }

    public String getRawContent() {
        return rawContent;
    }

    public void setRawContent(String rawContent) {
        this.rawContent = rawContent;
    }

    public String getFeedbackHeadline() {
        return feedbackHeadline;
    }

    public void setFeedbackHeadline(String feedbackHeadline) {
        this.feedbackHeadline = feedbackHeadline;
    }

    public String getFeedbackSummary() {
        return feedbackSummary;
    }

    public void setFeedbackSummary(String feedbackSummary) {
        this.feedbackSummary = feedbackSummary;
    }

    public String getCorrectedContent() {
        return correctedContent;
    }

    public void setCorrectedContent(String correctedContent) {
        this.correctedContent = correctedContent;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public boolean isPerfect() {
        return perfect;
    }

    public void setPerfect(boolean perfect) {
        this.perfect = perfect;
    }

    public OffsetDateTime getLastCorrectedAt() {
        return lastCorrectedAt;
    }

    public void setLastCorrectedAt(OffsetDateTime lastCorrectedAt) {
        this.lastCorrectedAt = lastCorrectedAt;
    }
}
