package com.example.hyunjujung.tbox.data_vo.chart;

import com.google.gson.annotations.SerializedName;

/**
 *  [ 일별 통계 데이터 클래스 ]
 *
 */

public class MonthChartVO {
    @SerializedName("date")
    private int date;

    public MonthChartVO() {
    }

    public MonthChartVO(int date) {
        this.date = date;
    }

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
    }
}
