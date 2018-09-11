package com.example.hyunjujung.tbox.data_vo.user;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class BookAndMember {
    @SerializedName("members")
    @Expose
    private ArrayList<MemberVO> memberArray;
    @SerializedName("userBook")
    @Expose
    private ArrayList<BookmarkVO> bookArray;

    public BookAndMember() {
    }

    public BookAndMember(ArrayList<MemberVO> memberArray, ArrayList<BookmarkVO> bookArray) {
        this.memberArray = memberArray;
        this.bookArray = bookArray;
    }

    public ArrayList<MemberVO> getMemberArray() {
        return memberArray;
    }

    public void setMemberArray(ArrayList<MemberVO> memberArray) {
        this.memberArray = memberArray;
    }

    public ArrayList<BookmarkVO> getBookArray() {
        return bookArray;
    }

    public void setBookArray(ArrayList<BookmarkVO> bookArray) {
        this.bookArray = bookArray;
    }
}
