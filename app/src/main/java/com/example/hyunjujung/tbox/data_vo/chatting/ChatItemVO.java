package com.example.hyunjujung.tbox.data_vo.chatting;

import com.google.gson.annotations.SerializedName;

public class ChatItemVO {
    @SerializedName("aeroPK")
    private String aeroPK;
    @SerializedName("sendUser")
    private String sendUser;
    @SerializedName("sendProfile")
    private String sendProfile;
    @SerializedName("mDate")
    private String mDate;
    @SerializedName("message")
    private String message;
    @SerializedName("syncTime")
    private int syncTime;

    public ChatItemVO() {
    }

    public ChatItemVO(String aeroPK, String sendUser, String sendProfile, String mDate, String message, int syncTime) {
        this.aeroPK = aeroPK;
        this.sendUser = sendUser;
        this.sendProfile = sendProfile;
        this.mDate = mDate;
        this.message = message;
        this.syncTime = syncTime;
    }

    public String getAeroPK() {
        return aeroPK;
    }

    public void setAeroPK(String aeroPK) {
        this.aeroPK = aeroPK;
    }

    public String getSendUser() {
        return sendUser;
    }

    public void setSendUser(String sendUser) {
        this.sendUser = sendUser;
    }

    public String getSendProfile() {
        return sendProfile;
    }

    public void setSendProfile(String sendProfile) {
        this.sendProfile = sendProfile;
    }

    public String getmDate() {
        return mDate;
    }

    public void setmDate(String mDate) {
        this.mDate = mDate;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getSyncTime() {
        return syncTime;
    }

    public void setSyncTime(int syncTime) {
        this.syncTime = syncTime;
    }
}
