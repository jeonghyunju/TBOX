package com.example.hyunjujung.tbox.data_vo.chart;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 *  [ 남녀, 연령별, 일별 통계 데이터를 담을 Array 클래스 ]
 *
 */

public class ChartArray {
    @SerializedName("genderJson")
    @Expose
    private ArrayList<GenderChartVO> genderArray;   // 남녀 통계
    @SerializedName("dayJson")
    @Expose
    private ArrayList<DayChartVO> dayArray; // 연령별 통계
    @SerializedName("monthJson")
    @Expose
    private ArrayList<MonthChartVO> monthArray; // 일별 통계

    public ChartArray() {
    }

    public ChartArray(ArrayList<GenderChartVO> genderArray, ArrayList<DayChartVO> dayArray, ArrayList<MonthChartVO> monthArray) {
        this.genderArray = genderArray;
        this.dayArray = dayArray;
        this.monthArray = monthArray;

    }

    public ArrayList<GenderChartVO> getGenderArray() {
        return genderArray;
    }

    public void setGenderArray(ArrayList<GenderChartVO> genderArray) {
        this.genderArray = genderArray;
    }

    public ArrayList<DayChartVO> getDayArray() {
        return dayArray;
    }

    public void setDayArray(ArrayList<DayChartVO> dayArray) {
        this.dayArray = dayArray;
    }

    public ArrayList<MonthChartVO> getMonthArray() {
        return monthArray;
    }

    public void setMonthArray(ArrayList<MonthChartVO> monthArray) {
        this.monthArray = monthArray;
    }
}
