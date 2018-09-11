package com.example.hyunjujung.tbox.data_vo.broadcast;

import com.google.gson.annotations.SerializedName;

public class RealtimeListVO {
    @SerializedName("idx")
    private int idx;
    @SerializedName("realtimeTitle")
    private String realtimeTitle;
    @SerializedName("realtimeThumb")
    private String realtimeThumb;
    @SerializedName("realtimeCount")
    private int realtimeCount;
    @SerializedName("realtimePath")
    private int realtimePath;

    public RealtimeListVO() {
    }

    public RealtimeListVO(int idx, String realtimeTitle, String realtimeThumb, int realtimeCount, int realtimePath) {
        this.idx = idx;
        this.realtimeTitle = realtimeTitle;
        this.realtimeThumb = realtimeThumb;
        this.realtimeCount = realtimeCount;
        this.realtimePath = realtimePath;
    }

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public String getRealtimeTitle() {
        return realtimeTitle;
    }

    public void setRealtimeTitle(String realtimeTitle) {
        this.realtimeTitle = realtimeTitle;
    }

    public String getRealtimeThumb() {
        return realtimeThumb;
    }

    public void setRealtimeThumb(String realtimeThumb) {
        this.realtimeThumb = realtimeThumb;
    }

    public int getRealtimeCount() {
        return realtimeCount;
    }

    public void setRealtimeCount(int realtimeCount) {
        this.realtimeCount = realtimeCount;
    }

    public int getRealtimePath() {
        return realtimePath;
    }

    public void setRealtimePath(int realtimePath) {
        this.realtimePath = realtimePath;
    }
}
