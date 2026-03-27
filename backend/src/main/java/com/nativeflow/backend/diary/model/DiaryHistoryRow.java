package com.nativeflow.backend.diary.model;

import java.time.LocalDate;

public class DiaryHistoryRow {

    private String id;
    private LocalDate entryDate;
    private String rawSnippet;
    private String correctedSnippet;
    private String keywords;

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

    public String getRawSnippet() {
        return rawSnippet;
    }

    public void setRawSnippet(String rawSnippet) {
        this.rawSnippet = rawSnippet;
    }

    public String getCorrectedSnippet() {
        return correctedSnippet;
    }

    public void setCorrectedSnippet(String correctedSnippet) {
        this.correctedSnippet = correctedSnippet;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }
}
