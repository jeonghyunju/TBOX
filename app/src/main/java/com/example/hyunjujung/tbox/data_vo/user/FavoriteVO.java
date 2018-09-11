package com.example.hyunjujung.tbox.data_vo.user;

import com.google.gson.annotations.SerializedName;

public class FavoriteVO {
    @SerializedName("vodIdx")
    private int vodIdx;
    @SerializedName("userId")
    private String userId;
    @SerializedName("vodHostId")
    private String vodHostId;
    @SerializedName("vodTitle")
    private String vodTitle;
    @SerializedName("vodPath")
    private String vodPath;
    @SerializedName("startTime")
    private long startTime;

    public FavoriteVO() {
    }

    public FavoriteVO(int vodIdx, String userId, String vodHostId, String vodTitle, String vodPath, long startTime) {
        this.vodIdx = vodIdx;
        this.userId = userId;
        this.vodHostId = vodHostId;
        this.vodTitle = vodTitle;
        this.vodPath = vodPath;
        this.startTime = startTime;
    }

    public int getVodIdx() {
        return vodIdx;
    }

    public void setVodIdx(int vodIdx) {
        this.vodIdx = vodIdx;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getVodHostId() {
        return vodHostId;
    }

    public void setVodHostId(String vodHostId) {
        this.vodHostId = vodHostId;
    }

    public String getVodTitle() {
        return vodTitle;
    }

    public void setVodTitle(String vodTitle) {
        this.vodTitle = vodTitle;
    }

    public String getVodPath() {
        return vodPath;
    }

    public void setVodPath(String vodPath) {
        this.vodPath = vodPath;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
}
