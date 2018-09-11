package com.example.hyunjujung.tbox.data_vo.user;

import com.google.gson.annotations.SerializedName;

public class MemberVO {
    @SerializedName("userNo")
    private String userNo;
    @SerializedName("userID")
    private String userId;
    @SerializedName("userProfile")
    private String userProfile;
    @SerializedName("userAge")
    private String userAge;
    @SerializedName("userGender")
    private String userGender;
    @SerializedName("followCount")
    private int followCount;
    @SerializedName("followingCount")
    private int followingCount;
    @SerializedName("vodCount")
    private int vodCount;
    @SerializedName("fcmToken")
    private String fcmToken;

    public MemberVO() {
    }

    public MemberVO(String userNo, String userId, String userProfile, String userAge, String userGender, int followCount, int followingCount, int vodCount, String fcmToken) {
        this.userNo = userNo;
        this.userId = userId;
        this.userProfile = userProfile;
        this.userAge = userAge;
        this.userGender = userGender;
        this.followCount = followCount;
        this.followingCount = followingCount;
        this.vodCount = vodCount;
        this.fcmToken = fcmToken;
    }

    public String getUserNo() {
        return userNo;
    }

    public void setUserNo(String userNo) {
        this.userNo = userNo;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(String userProfile) {
        this.userProfile = userProfile;
    }

    public String getUserAge() {
        return userAge;
    }

    public void setUserAge(String userAge) {
        this.userAge = userAge;
    }

    public String getUserGender() {
        return userGender;
    }

    public void setUserGender(String userGender) {
        this.userGender = userGender;
    }

    public int getFollowCount() {
        return followCount;
    }

    public void setFollowCount(int followCount) {
        this.followCount = followCount;
    }

    public int getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(int followingCount) {
        this.followingCount = followingCount;
    }

    public int getVodCount() {
        return vodCount;
    }

    public void setVodCount(int vodCount) {
        this.vodCount = vodCount;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}
