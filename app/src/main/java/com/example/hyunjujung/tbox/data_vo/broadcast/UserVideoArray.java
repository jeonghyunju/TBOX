package com.example.hyunjujung.tbox.data_vo.broadcast;

import com.example.hyunjujung.tbox.data_vo.user.FavoriteVO;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 *  [ ME 화면의 내가 방송한 영상 목록과 추가한 영상 목록을 저장할 데이터 클래스 ]
 *
 */

public class UserVideoArray {
    @SerializedName("myVideo")
    @Expose
    private ArrayList<StreamLiveListVO> myVideoArray;   // 내가 방송한 영상 목록
    @SerializedName("favoriteVideo")
    @Expose
    private ArrayList<FavoriteVO> favoritArray; // 추가한 영상 목록

    public UserVideoArray() {
    }

    public UserVideoArray(ArrayList<StreamLiveListVO> myVideoArray, ArrayList<FavoriteVO> favoritArray) {
        this.myVideoArray = myVideoArray;
        this.favoritArray = favoritArray;
    }

    public ArrayList<StreamLiveListVO> getMyVideoArray() {
        return myVideoArray;
    }

    public void setMyVideoArray(ArrayList<StreamLiveListVO> myVideoArray) {
        this.myVideoArray = myVideoArray;
    }

    public ArrayList<FavoriteVO> getFavoritArray() {
        return favoritArray;
    }

    public void setFavoritArray(ArrayList<FavoriteVO> favoritArray) {
        this.favoritArray = favoritArray;
    }
}
