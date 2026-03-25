package com.nativeflow.backend.content.model;

public class DashboardStatsRow {

    private int dueCount;
    private int reviewedDistinctCount;

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
}
