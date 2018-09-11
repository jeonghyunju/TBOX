package com.example.hyunjujung.tbox.data_vo.chart;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 *  [ 연령별 통계 데이터 클래스 ]
 *
 */

public class DayChartVO {
    @SerializedName("10대")
    private int teenage;
    @SerializedName("20대")
    private int twenty;
    @SerializedName("30대")
    private int thirty;
    @SerializedName("40대")
    private int fourty;
    @SerializedName("50대")
    private int fifty;
    @SerializedName("60대")
    private int sixty;

    public DayChartVO() {
    }

    public DayChartVO(int teenage, int twenty, int thirty, int fourty, int fifty, int sixty) {
        this.teenage = teenage;
        this.twenty = twenty;
        this.thirty = thirty;
        this.fourty = fourty;
        this.fifty = fifty;
        this.sixty = sixty;
    }

    public int getTeenage() {
        return teenage;
    }

    public void setTeenage(int teenage) {
        this.teenage = teenage;
    }

    public int getTwenty() {
        return twenty;
    }

    public void setTwenty(int twenty) {
        this.twenty = twenty;
    }

    public int getThirty() {
        return thirty;
    }

    public void setThirty(int thirty) {
        this.thirty = thirty;
    }

    public int getFourty() {
        return fourty;
    }

    public void setFourty(int fourty) {
        this.fourty = fourty;
    }

    public int getFifty() {
        return fifty;
    }

    public void setFifty(int fifty) {
        this.fifty = fifty;
    }

    public int getSixty() {
        return sixty;
    }

    public void setSixty(int sixty) {
        this.sixty = sixty;
    }
}
