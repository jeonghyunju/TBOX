package com.example.hyunjujung.tbox.data_vo.user;

import com.example.hyunjujung.tbox.data_vo.broadcast.StreamLiveListVO;
import com.example.hyunjujung.tbox.data_vo.user.MemberVO;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 *  [ 사용자의 타임라인에서 표시할 정보를 담을 데이터 클래스 ]
 *
 *  - 표시할 정보 : 게시글수, 팔로잉수, 팔로워수, VOD 목록, 추가한 VOD 목록
 *  - 게시글수, 팔로잉수, 팔로워수 -> members 테이블
 *  - VOD 목록 -> livestream_list 테이블
 *  - 추가한 VOD 목록 -> userVod_list 테이블
 *
 */

public class MemberAndVod {
    @SerializedName("memberOne")
    @Expose
    private ArrayList<MemberVO> memberArray;
    @SerializedName("vod_list")
    @Expose
    private ArrayList<StreamLiveListVO> vodArray;
    public MemberAndVod() {
    }

    public MemberAndVod(ArrayList<MemberVO> memberArray, ArrayList<StreamLiveListVO> vodArray) {
        this.memberArray = memberArray;
        this.vodArray = vodArray;
    }

    public ArrayList<MemberVO> getMemberArray() {
        return memberArray;
    }

    public void setMemberArray(ArrayList<MemberVO> memberArray) {
        this.memberArray = memberArray;
    }

    public ArrayList<StreamLiveListVO> getVodArray() {
        return vodArray;
    }

    public void setVodArray(ArrayList<StreamLiveListVO> vodArray) {
        this.vodArray = vodArray;
    }

}
