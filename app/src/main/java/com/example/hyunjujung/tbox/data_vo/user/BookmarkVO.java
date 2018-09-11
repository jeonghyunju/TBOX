package com.example.hyunjujung.tbox.data_vo.user;

import com.google.gson.annotations.SerializedName;

/**
 *  [ 사용자의 북마크 목록을 담을 데이터 클래스 ]
 *
 */

public class BookmarkVO {
    @SerializedName("BJ_Id")
    private String bookmarkPro;
    @SerializedName("BJ_Profile")
    private String bookmarkId;

    public BookmarkVO() {
    }

    public BookmarkVO(String bookmarkPro, String bookmarkId) {
        this.bookmarkPro = bookmarkPro;
        this.bookmarkId = bookmarkId;
    }

    public String getBookmarkPro() {
        return bookmarkPro;
    }

    public void setBookmarkPro(String bookmarkPro) {
        this.bookmarkPro = bookmarkPro;
    }

    public String getBookmarkId() {
        return bookmarkId;
    }

    public void setBookmarkId(String bookmarkId) {
        this.bookmarkId = bookmarkId;
    }
}
