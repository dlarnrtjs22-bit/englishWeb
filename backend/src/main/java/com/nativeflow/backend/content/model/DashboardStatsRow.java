package com.nativeflow.backend.content.model;

public class DashboardStatsRow {

    private int dueCount;
    private int reviewedDistinctCount;
    private int reviewedTodayCount;
    private int streakDays;

    public int getDueCount() {
        return dueCount;
    }

    public void setDueCount(int dueCount) {
        this.dueCount = dueCount;
    }

    public int getReviewedDistinctCount() {
        return reviewedDistinctCount;
    }

    public void setReviewedDistinctCount(int reviewedDistinctCount) {
        this.reviewedDistinctCount = reviewedDistinctCount;
    }

    public int getReviewedTodayCount() {
        return reviewedTodayCount;
    }

    public void setReviewedTodayCount(int reviewedTodayCount) {
        this.reviewedTodayCount = reviewedTodayCount;
    }

    public int getStreakDays() {
        return streakDays;
    }

    public void setStreakDays(int streakDays) {
        this.streakDays = streakDays;
    }
}
