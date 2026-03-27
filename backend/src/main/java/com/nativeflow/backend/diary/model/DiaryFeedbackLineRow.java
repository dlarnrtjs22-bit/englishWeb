package com.nativeflow.backend.diary.model;

public class DiaryFeedbackLineRow {

    private String originalLine;
    private String correctedLine;
    private String translationLine;
    private int lineOrder;

    public String getOriginalLine() {
        return originalLine;
    }

    public void setOriginalLine(String originalLine) {
        this.originalLine = originalLine;
    }

    public String getCorrectedLine() {
        return correctedLine;
    }

    public void setCorrectedLine(String correctedLine) {
        this.correctedLine = correctedLine;
    }

    public String getTranslationLine() {
        return translationLine;
    }

    public void setTranslationLine(String translationLine) {
        this.translationLine = translationLine;
    }

    public int getLineOrder() {
        return lineOrder;
    }

    public void setLineOrder(int lineOrder) {
        this.lineOrder = lineOrder;
    }
}
