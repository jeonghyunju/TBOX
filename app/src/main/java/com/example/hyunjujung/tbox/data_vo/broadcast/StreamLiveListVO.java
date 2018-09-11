package com.example.hyunjujung.tbox.data_vo.broadcast;

import com.google.gson.annotations.SerializedName;

/**
 *  [ HOME 화면, BJ 타임라인 화면의 VOD 한 개의 데이터의 담을 데이터 클래스 ]
 *
 */

public class StreamLiveListVO {
    @SerializedName("live_idx")
    private int live_idx;   // 데이터베이스의 인덱스
    @SerializedName("host_ID")
    private String host_ID; // 영상주인 아이디
    @SerializedName("host_Profile")
    private String host_Profile;    // 영상주인 프로필
    @SerializedName("live_title")
    private String live_title;  // 영상 제목
    @SerializedName("live_set")
    private String live_set;    // 공개/비공개 여부
    @SerializedName("live_password")
    private String live_password;   // 비공개일때 설정한 비밀번호
    @SerializedName("live_count")
    private int live_count; // 시청자수
    @SerializedName("live_path")
    private String live_path;   // 서버의 영상 경로
    @SerializedName("vod_tag")
    private int vod_tag;    // 0: 라이브 방송, 1: VOD
    @SerializedName("vod_thumbnail")
    private String vod_thumbnail;   // 영상 썸네일
    @SerializedName("startTime")
    private long live_startTime;    // 영상 시작 시간
    @SerializedName("duration")
    private long duration;   // 영상 지속 시간

    public StreamLiveListVO() {
    }

    public StreamLiveListVO(int live_idx, String host_ID, String host_Profile, String live_title, String live_set, String live_password, int live_count, String live_path, int vod_tag, String vod_thumbnail, long live_startTime, int duration) {
        this.live_idx = live_idx;
        this.host_ID = host_ID;
        this.host_Profile = host_Profile;
        this.live_title = live_title;
        this.live_set = live_set;
        this.live_password = live_password;
        this.live_count = live_count;
        this.live_path = live_path;
        this.vod_tag = vod_tag;
        this.vod_thumbnail = vod_thumbnail;
        this.live_startTime = live_startTime;
        this.duration = duration;
    }

    public int getLive_idx() {
        return live_idx;
    }

    public void setLive_idx(int live_idx) {
        this.live_idx = live_idx;
    }

    public String getHost_ID() {
        return host_ID;
    }

    public void setHost_ID(String host_ID) {
        this.host_ID = host_ID;
    }

    public String getHost_Profile() {
        return host_Profile;
    }

    public void setHost_Profile(String host_Profile) {
        this.host_Profile = host_Profile;
    }

    public String getLive_title() {
        return live_title;
    }

    public void setLive_title(String live_title) {
        this.live_title = live_title;
    }

    public String getLive_set() {
        return live_set;
    }

    public void setLive_set(String live_set) {
        this.live_set = live_set;
    }

    public String getLive_password() {
        return live_password;
    }

    public void setLive_password(String live_password) {
        this.live_password = live_password;
    }

    public int getLive_count() {
        return live_count;
    }

    public void setLive_count(int live_count) {
        this.live_count = live_count;
    }

    public String getLive_path() {
        return live_path;
    }

    public void setLive_path(String live_path) {
        this.live_path = live_path;
    }

    public int getVod_tag() {
        return vod_tag;
    }

    public void setVod_tag(int vod_tag) {
        this.vod_tag = vod_tag;
    }

    public String getVod_thumbnail() {
        return vod_thumbnail;
    }

    public void setVod_thumbnail(String vod_thumbnail) {
        this.vod_thumbnail = vod_thumbnail;
    }

    public long getLive_startTime() {
        return live_startTime;
    }

    public void setLive_startTime(long live_startTime) {
        this.live_startTime = live_startTime;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
