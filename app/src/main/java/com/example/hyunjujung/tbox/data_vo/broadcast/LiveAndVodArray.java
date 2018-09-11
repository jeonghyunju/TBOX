package com.example.hyunjujung.tbox.data_vo.broadcast;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 *  [ 로그인 후 넘어오는 메인 화면 (FragStream_main) 에서 라이브 방송 목록과 VOD 목록을 담을 데이터 클래스 ]
 *
 *  - 데이터베이스의 livestream_list 테이블에서 라이브 방송 목록과 VOD 목록을 모두 가져와서 이 클래스에 담는다
 */

public class LiveAndVodArray {
    @SerializedName("live_list")
    @Expose
    private ArrayList<StreamLiveListVO> livelistArray;  // 라이브 목록
    @SerializedName("vod_list")
    @Expose
    private ArrayList<StreamLiveListVO> vodlistArray;   // VOD 목록
    @SerializedName("realtime_list")
    @Expose
    private ArrayList<RealtimeListVO> realtimeArray;

    public LiveAndVodArray() {
    }

    public LiveAndVodArray(ArrayList<StreamLiveListVO> livelistArray, ArrayList<StreamLiveListVO> vodlistArray, ArrayList<RealtimeListVO> realtimeArray) {
        this.livelistArray = livelistArray;
        this.vodlistArray = vodlistArray;
        this.realtimeArray = realtimeArray;
    }

    public ArrayList<StreamLiveListVO> getLivelistArray() {
        return livelistArray;
    }

    public void setLivelistArray(ArrayList<StreamLiveListVO> livelistArray) {
        this.livelistArray = livelistArray;
    }

    public ArrayList<StreamLiveListVO> getVodlistArray() {
        return vodlistArray;
    }

    public void setVodlistArray(ArrayList<StreamLiveListVO> vodlistArray) {
        this.vodlistArray = vodlistArray;
    }

    public ArrayList<RealtimeListVO> getRealtimeArray() {
        return realtimeArray;
    }

    public void setRealtimeArray(ArrayList<RealtimeListVO> realtimeArray) {
        this.realtimeArray = realtimeArray;
    }
}
