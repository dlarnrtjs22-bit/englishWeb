package com.nativeflow.backend.content.model;

public class LearningExpressionNoteRow {

    private String expressionText;
    private String partOfSpeechKo;
    private String meaningKo;
    private String nuanceKo;
    private int orderIndex;

    public String getExpressionText() {
        return expressionText;
    }

    public void setExpressionText(String expressionText) {
        this.expressionText = expressionText;
    }

    public String getPartOfSpeechKo() {
        return partOfSpeechKo;
    }

    public void setPartOfSpeechKo(String partOfSpeechKo) {
        this.partOfSpeechKo = partOfSpeechKo;
    }

    public String getMeaningKo() {
        return meaningKo;
    }

    public void setMeaningKo(String meaningKo) {
        this.meaningKo = meaningKo;
    }

    public String getNuanceKo() {
        return nuanceKo;
    }

    public void setNuanceKo(String nuanceKo) {
        this.nuanceKo = nuanceKo;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }
}
