package com.nativeflow.backend.diary.model;

public class DiaryFeedbackNoteRow {

    private String noteType;
    private String content;
    private int noteOrder;

    public String getNoteType() {
        return noteType;
    }

    public void setNoteType(String noteType) {
        this.noteType = noteType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getNoteOrder() {
        return noteOrder;
    }

    public void setNoteOrder(int noteOrder) {
        this.noteOrder = noteOrder;
    }
}
