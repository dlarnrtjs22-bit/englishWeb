package com.nativeflow.backend.learning.model;

public class LearningSessionCurrentRow {

    private String sessionItemId;
    private String learningItemId;
    private String sourceText;
    private String targetText;
    private String nuanceNote;
    private String exampleSentence;
    private String exampleTranslation;
    private Boolean answerChecked;
    private Boolean lastAnswerCorrect;

    public String getSessionItemId() {
        return sessionItemId;
    }

    public void setSessionItemId(String sessionItemId) {
        this.sessionItemId = sessionItemId;
    }

    public String getLearningItemId() {
        return learningItemId;
    }

    public void setLearningItemId(String learningItemId) {
        this.learningItemId = learningItemId;
    }

    public String getSourceText() {
        return sourceText;
    }

    public void setSourceText(String sourceText) {
        this.sourceText = sourceText;
    }

    public String getTargetText() {
        return targetText;
    }

    public void setTargetText(String targetText) {
        this.targetText = targetText;
    }

    public String getNuanceNote() {
        return nuanceNote;
    }

    public void setNuanceNote(String nuanceNote) {
        this.nuanceNote = nuanceNote;
    }

    public String getExampleSentence() {
        return exampleSentence;
    }

    public void setExampleSentence(String exampleSentence) {
        this.exampleSentence = exampleSentence;
    }

    public String getExampleTranslation() {
        return exampleTranslation;
    }

    public void setExampleTranslation(String exampleTranslation) {
        this.exampleTranslation = exampleTranslation;
    }

    public Boolean getAnswerChecked() {
        return answerChecked;
    }

    public void setAnswerChecked(Boolean answerChecked) {
        this.answerChecked = answerChecked;
    }

    public Boolean getLastAnswerCorrect() {
        return lastAnswerCorrect;
    }

    public void setLastAnswerCorrect(Boolean lastAnswerCorrect) {
        this.lastAnswerCorrect = lastAnswerCorrect;
    }
}
