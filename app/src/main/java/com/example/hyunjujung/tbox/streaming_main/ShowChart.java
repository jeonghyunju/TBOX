package com.example.hyunjujung.tbox.streaming_main;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

import com.example.hyunjujung.tbox.R;
import com.example.hyunjujung.tbox.data_vo.chart.ChartArray;
import com.example.hyunjujung.tbox.data_vo.chart.DayChartVO;
import com.example.hyunjujung.tbox.data_vo.chart.GenderChartVO;
import com.example.hyunjujung.tbox.data_vo.chart.MonthChartVO;
import com.example.hyunjujung.tbox.retrofit.Retrofit_ApiConfig;
import com.example.hyunjujung.tbox.retrofit.Retrofit_Creator;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.listener.ViewportChangeListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.ColumnChartView;
import lecho.lib.hellocharts.view.PieChartView;
import lecho.lib.hellocharts.view.PreviewColumnChartView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 *  [ 시청자 통계 화면 ]
 *
 *  - FragSettings_main 화면에서 시청자 통계 누르면 넘어오는 화면
 *  - 오늘 날짜에 해당하는 남녀 통계와 일별 통계를 볼 수 있다
 *  - 월별 통계는 해당 월의 1일부터 오늘 날짜까지의 통계를 보여준다
 *  - 가장 상단의 날짜 툴바를 클릭하면 지정된 날짜의 남녀, 일별 통계를 볼 수 있다 (월별 통계는 바뀌지 않음)
 *
 */

public class ShowChart extends AppCompatActivity {
    /* 상단 날짜, total 값 TextView */
    TextView date_tv, year_tv, month_tv, dayTotalCount;

    boolean hasLabels = false;
    boolean hasLabelOutside = false;
    boolean hasLabelForSelected = false;
    boolean hasAxes = true;
    boolean hasAxesName = true;

    /* 남녀 통계 차트 */
    PieChartView pieChart;
    PieChartData pieChartData;

    /* 연령별 통계 차트 */
    ColumnChartView dayColumnChart;
    ColumnChartData dayChartData;

    /* 일별 통계 차트 */
    ColumnChartView monthChartView;
    ColumnChartData monthChartData;
    PreviewColumnChartView monthChart_preview;
    ColumnChartData monthPreviewData;

    /* DatePicker 초기에 넣어줄 캘린더 */
    Calendar calendar = Calendar.getInstance();

    /* DatePicker 에서 입력받은 날짜 저장할 변수 */
    int dp_year, dp_month, dp_day = 0;

    /* 시청자 통계 데이터 Array
     *  - dayChartArray : 연령별 통계 Array
     *  - genderChartArray : 남녀 통계 Array
     *  - monthChartArray : 일별 통계 Array */
    ArrayList<DayChartVO> dayChartArray = new ArrayList<>();
    ArrayList<GenderChartVO> genderChartArray = new ArrayList<>();
    ArrayList<MonthChartVO> monthChartArray = new ArrayList<>();

    List<SliceValue> pieValues = new ArrayList<>();

    DecimalFormat totalDecimal = new DecimalFormat("#,###");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_chart);

        date_tv = (TextView)findViewById(R.id.date_tv);
        year_tv = (TextView)findViewById(R.id.year_tv);
        month_tv = (TextView)findViewById(R.id.month_tv);
        dayTotalCount = (TextView)findViewById(R.id.dayTotalCount);

        /* 남녀 통계 차트 */
        pieChart = (PieChartView)findViewById(R.id.pieChart);

        /* 연령별 통계 차트 */
        dayColumnChart = (ColumnChartView)findViewById(R.id.dayColumnChart);

        /* 일별 통계 차트 */
        monthChartView = (ColumnChartView)findViewById(R.id.monthChartView);
        monthChart_preview = (PreviewColumnChartView)findViewById(R.id.monthChart_preview);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(genderChartArray.size() > 0) {
            genderChartArray.clear();
        }

        /* 상단 날짜 툴바에 오늘 날짜 넣어주기 */
        date_tv.setText(calendar.get(Calendar.DATE) + "");
        year_tv.setText(calendar.get(Calendar.YEAR) + "");

        /* 날짜가 10 이하일때 : 앞에 0 붙여주기 (05 로 표시되도록) */
        if(calendar.get(Calendar.MONTH) + 1 < 10) {
            month_tv.setText("0" + (calendar.get(Calendar.MONTH) + 1));
        }else {
            month_tv.setText((calendar.get(Calendar.MONTH) + 1) + "");
        }

        /* 오늘 날짜에 해당하는 시청자 통계 가져오기 */
        selectChart(calendar.get(Calendar.DATE), calendar.get(Calendar.MONTH) + 1, false);
    }

    public void chartClick(View view) {
        switch (view.getId()) {
            /* 다른 날짜의 남녀, 연령별 통계를 보고싶으면 클릭
             *  - DatePicker 로 날짜 입력 받음 */
            case R.id.date_rel:
                DatePickerDialog dateDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        dp_year = year;
                        dp_month = month + 1;
                        dp_day = day;

                        /* 화면의 상단 날짜를 선택된 날짜로 바꾸기
                         *  - 날짜가 10 이하일때 : 앞에 0 붙여주기 */
                        if(dp_day < 10) {
                            date_tv.setText("0" + dp_day);
                        }else {
                            date_tv.setText(dp_day + "");
                        }
                        year_tv.setText(dp_year + "");
                        /* 날짜가 10 이하일때 : 앞에 0 붙여주기 */
                        if(dp_month < 10) {
                            month_tv.setText( "0" + dp_month);
                        }else {
                            month_tv.setText(dp_month + "");
                        }
                        selectChart(dp_day, dp_month, true);
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));

                dateDialog.show();
                break;

            /* 통계 화면 종료 */
            case R.id.closeChartBtn:
                finish();
                break;
        }
    }

    /* 시청자 통계 모두 가져오는 메소드 */
    public void selectChart(int chooseDay, int chooseMonth, final boolean isChooseDay) {

        Retrofit_ApiConfig selectApi = Retrofit_Creator.getApiConfig();
        Call<ChartArray> selectCall = selectApi.select_chart(chooseDay, chooseMonth);
        selectCall.enqueue(new Callback<ChartArray>() {
            @Override
            public void onResponse(Call<ChartArray> call, Response<ChartArray> response) {
                int tempDay = 0;
                if(genderChartArray.size() > 0 || dayChartArray.size() > 0 || monthChartArray.size() > 0) {
                    genderChartArray.clear();
                    dayChartArray.clear();
                    monthChartArray.clear();
                }
                genderChartArray.addAll(response.body().getGenderArray());  // 남녀 통계 저장
                dayChartArray.addAll(response.body().getDayArray());    // 연령별 통계 저장
                monthChartArray.addAll(response.body().getMonthArray());    // 일별 통계 저장

                if(!isChooseDay) {
                    setMonthPreviewChart(monthChartArray);
                    tempDay = calendar.get(Calendar.DATE);
                }else {
                    /* 상단 날짜 재설정 했을때 라벨 나오게 하기 */
                    hasLabels = false;
                    tempDay = dp_day;
                }

                setChartLabels();
                setPieChartView(genderChartArray);
                setColumnChartView(dayChartArray);
                /* 해당 날짜의 Total 값을 가져와서 상단의 dayTotalCount 텍스트뷰에 setText 한다 */
                for(int i=1 ; i<=monthChartArray.size() ; ++i) {
                    if(i == tempDay) {
                        Log.e("토탈 값", totalDecimal.format(monthChartArray.get(i-1).getDate()));
                        dayTotalCount.setText(totalDecimal.format(monthChartArray.get(i-1).getDate()));
                    }
                }

                pieChartAnim(); //  차트 애니메이션
            }

            @Override
            public void onFailure(Call<ChartArray> call, Throwable t) {

            }
        });
    }

    /* 남녀 통계 데이터 PieChartView 에 붙이는 메소드 */
    public void setPieChartView(ArrayList<GenderChartVO> genderArray) {
        if(pieValues.size() > 0) {
            pieValues.clear();
        }
        SliceValue pieFemaleSlice = new SliceValue(genderArray.get(0).getFemale(), Color.rgb(254, 183, 41));
        pieFemaleSlice.setLabel(genderArray.get(0).getFemale() + " (F)");
        SliceValue pieMaleSlice = new SliceValue(genderArray.get(0).getMale(), Color.rgb(0, 39, 94));
        pieMaleSlice.setLabel(genderArray.get(0).getMale() + " (M)");
        pieValues.add(pieFemaleSlice);
        pieValues.add(pieMaleSlice);

        pieChartData = new PieChartData(pieValues);
        pieChartData.setHasLabels(hasLabels);   // 차트 라벨 설정
        pieChartData.setHasLabelsOnlyForSelected(hasLabelForSelected);
        pieChart.setPieChartData(pieChartData);
    }

    /* 연령 통계 데이터 ColumnChartView 에 붙이는 메소드 */
    public void setColumnChartView(ArrayList<DayChartVO> dayArray) {
        List<Column> dayColumns = new ArrayList<>();
        List<SubcolumnValue> daySubColumn;
        List<AxisValue> ageAxisArray = new ArrayList<>();
        SubcolumnValue daySubValue;

        /* Nosql 에서 데이터 받아올때 [{"10대":346, "20대":688, ...}] 이런 형식으로 받아왔기 때문에
         * 연령대별 숫자값만 넣어주는 ArrayList */
        ArrayList<Integer> dayAgeArray = new ArrayList<>();
        dayAgeArray.add(dayArray.get(0).getTeenage());
        dayAgeArray.add(dayArray.get(0).getTwenty());
        dayAgeArray.add(dayArray.get(0).getThirty());
        dayAgeArray.add(dayArray.get(0).getFourty());
        dayAgeArray.add(dayArray.get(0).getFifty());
        dayAgeArray.add(dayArray.get(0).getSixty());

        /* Column 당 SubColumn 은 하나이기 때문에 j의 범위는 1까지 */
        for(int i=0 ; i<dayAgeArray.size() ; ++i) {
            daySubColumn = new ArrayList<>();
            for(int j=0 ; j<1 ; ++j) {
                daySubValue = new SubcolumnValue(dayAgeArray.get(i), ChartUtils.pickColor());
                daySubValue.setLabel((i+1) + "0대");     //  Column 에 들어가는 Label 을 내가 원하는 형식으로 표현 (ex. 10대)
                daySubColumn.add(daySubValue);
            }
            Column column = new Column(daySubColumn);
            column.setHasLabels(hasLabels);
            column.setHasLabelsOnlyForSelected(hasLabelForSelected);
            dayColumns.add(column);
        }

        dayChartData = new ColumnChartData(dayColumns);

        /* X, Y 축 이름 세팅 */
        if(hasAxes) {
            Axis axisX = new Axis();
            Axis axisY = new Axis().setHasLines(true);
            for(int i=1 ; i<=dayAgeArray.size() ; i++) {
                ageAxisArray.add(new AxisValue(i*10).setValue(i+10));
            }
            axisX.setValues(ageAxisArray);
            if(hasAxesName) {
                axisX.setName("연령대");
                axisY.setName("시청자수");
            }
            dayChartData.setAxisXBottom(axisX);
            dayChartData.setAxisYLeft(axisY);
        }else {
            dayChartData.setAxisXBottom(null);
            dayChartData.setAxisYLeft(null);
        }

        dayColumnChart.setColumnChartData(dayChartData);
        columnChartAnim(dayAgeArray);
    }

    /* 일별 통계 데이터 View 에 붙이는 메소드 */
    public void setMonthPreviewChart(ArrayList<MonthChartVO> monthArray) {
        List<Column> monthColumn = new ArrayList<>();
        List<SubcolumnValue> monthSubValue;
        for(int i=1 ; i<=monthArray.size() ; ++i) { // monthArray.size() : 그 달의 마지막날짜까지
            monthSubValue = new ArrayList<>();
            for(int j=0 ; j<1 ; ++j) {  // SubColumn 은 1개 이므로 j의 범위는 1
                monthSubValue.add(new SubcolumnValue(monthArray.get(i-1).getDate(), ChartUtils.pickColor()));
            }
            monthColumn.add(new Column(monthSubValue));

        }

        monthChartData = new ColumnChartData(monthColumn);
        monthChartData.setAxisXBottom(new Axis());
        monthChartData.setAxisYLeft(new Axis().setHasLines(true));

        /* 일별 통계 차트에서 하단의 미리보여지는 차트 */
        monthPreviewData = new ColumnChartData(monthChartData);
        for(Column column : monthPreviewData.getColumns()) {
            for(SubcolumnValue value : column.getValues()) {
                value.setColor(ChartUtils.DEFAULT_DARKEN_COLOR);
            }
        }

        int previewColor = Color.rgb(254, 183, 41);
        while(previewColor == monthChart_preview.getPreviewColor()) {
            previewColor = Color.rgb(254, 183, 41);
        }
        monthChart_preview.setPreviewColor(previewColor);

        monthChartView.setColumnChartData(monthChartData);
        monthChartView.setZoomEnabled(false);
        monthChartView.setScrollEnabled(false);

        monthChart_preview.setColumnChartData(monthPreviewData);
        monthChart_preview.setViewportChangeListener(new ViewportListener());
        previewX(false);
    }

    public void previewX(boolean anim) {
        Viewport tempViewport = new Viewport(monthChartView.getMaximumViewport());
        float dx = tempViewport.width() / 4;
        tempViewport.inset(dx,0);
        if(anim) {
            monthChart_preview.setCurrentViewportWithAnimation(tempViewport);
        }else {
            monthChart_preview.setCurrentViewport(tempViewport);
        }
        monthChart_preview.setZoomType(ZoomType.HORIZONTAL);
    }

    /* 차트에 숫자 표시되게 하는 메소드 */
    public void setChartLabels() {
        hasLabels = !hasLabels;
        Log.e("라벨", hasLabels + "");
        if (hasLabels) {
            hasLabelForSelected = false;
            pieChart.setValueSelectionEnabled(hasLabelForSelected);
            dayColumnChart.setValueSelectionEnabled(hasLabelForSelected);

            if (hasLabelOutside) {
                pieChart.setCircleFillRatio(0.7f);
            } else {
                pieChart.setCircleFillRatio(1.0f);
            }
        }
    }

    /* ================= 날짜 재설정 차트 애니메이션 ==================
     *  - 날짜 재설정 후 남녀 통계 차트와 연령별 통계 차트에 애니메이션 적용한다 */
    public void pieChartAnim() {
        for(SliceValue value : pieChartData.getValues()) {
            value.setTarget(genderChartArray.get(0).getFemale());
            value.setTarget(genderChartArray.get(0).getMale());
        }
    }
    public void columnChartAnim(ArrayList<Integer> ageArray) {
        int i = 0;
        for(Column column : dayChartData.getColumns()) {
            for(SubcolumnValue value : column.getValues()) {
                value.setTarget(ageArray.get(i));
            }
            i++;
        }
    }

    private class ViewportListener implements ViewportChangeListener {
        @Override
        public void onViewportChanged(Viewport viewport) {
            monthChartView.setCurrentViewport(viewport);
        }
    }
}
