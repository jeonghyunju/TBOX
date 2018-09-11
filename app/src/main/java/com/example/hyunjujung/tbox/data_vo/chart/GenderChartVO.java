package com.example.hyunjujung.tbox.data_vo.chart;

import com.google.gson.annotations.SerializedName;

/**
 *  [ 남녀 통계 데이터 클래스 ]
 *
 */

public class GenderChartVO {
    @SerializedName("Female")
    private int female;
    @SerializedName("Male")
    private int male;

    public GenderChartVO() {
    }

    public GenderChartVO(int female, int male) {
        this.female = female;
        this.male = male;
    }

    public int getFemale() {
        return female;
    }

    public void setFemale(int female) {
        this.female = female;
    }

    public int getMale() {
        return male;
    }

    public void setMale(int male) {
        this.male = male;
    }
}
