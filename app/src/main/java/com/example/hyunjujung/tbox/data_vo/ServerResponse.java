package com.example.hyunjujung.tbox.data_vo;

import com.google.gson.annotations.SerializedName;

/**
 *  [ 데이터베이스에 insert, update, delete 하거나 데이터베이스에 있는 정보를 확인할때 반환해주는 true, false 값 저장 ]
 *
 */

public class ServerResponse {
    @SerializedName("success")
    private int success;
    @SerializedName("message")
    private String message;
    @SerializedName("userProfile")
    private String userProfile;
    @SerializedName("userAge")
    private String userAge;
    @SerializedName("userGender")
    private String userGender;

    public int getSuccess() {
        return success;
    }
    public String getMessage() {
        return message;
    }
    public String getUserProfile() {
        return userProfile;
    }
    public String getUserAge() {
        return userAge;
    }
    public String getUserGender() {
        return userGender;
    }
}
