package com.example.hyunjujung.tbox.streaming_main.map;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hyunjujung.tbox.R;
import com.example.hyunjujung.tbox.adapter.map.MapSearchAdapter;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPOIItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;

import java.util.ArrayList;

/**
 *  [ TMap 이용해서 현재 위치와 도착지 간의 경로를 탐색하는 화면 ]
 *
 *  - 현재 위치를 인터넷을 통해 받아온다
 *  - 검색되는 데이터를 받아오려면 AsyncTask 사용해야함
 *  - 검색되는 데이터의 데이터 유형은 TMapPOIItem 이다
 *
 */

public class TmapSearchPath extends AppCompatActivity implements TMapGpsManager.onLocationChangedCallback{
    RelativeLayout tmapLayout, mapSearch_rel, carType_rel, walkTyep_rel, areaBottom_rel;
    LinearLayout start_endPath_linear;
    TextView areaName, areaBizName, areaDistance, areaAddress;
    ImageView initial_startPath;
    EditText mapSearchEt, startPath_tv, endPath_tv;

    /* 출발지 설정 되어있는지 여부 판단하는 변수 */
    boolean isSetStart = false;

    /* 검색된 데이터 출력할 RecyclerView */
    RecyclerView searchDateRecycle;
    LinearLayoutManager searchLM;
    MapSearchAdapter mapSearchAdapter;
    ArrayList<TMapPOIItem> searchData = new ArrayList<>();

    /* TMap 관련 */
    TMapView tmapView;  //  TMap 지도 출력 클래스
    TMapGpsManager tMapGpsManager = null;   //  인터넷을 통해 현재 위치를 받아오는 클래스
    TMapData tMapData;  //  TMap 에서 경로검색 할때 필요한 데이터를 관리하는 클래스
    String tmapApiKey = "a02fe4d0-6173-4248-b26a-806b18593b77";
    boolean trackingMode = true;

    /* startPoint : 출발지 위도, 경도
     * endPoint : 목적지 위도, 경도 */
    TMapPoint initialPoint, startPoint, endPoint;

    /* Tmap 경로 탐색시 가이드 라인 그려주는 클래스 */
    TMapPolyLine polyLine = null;

    /* 경로 타입 지정하는 변수 (자동차 경로, 보행 경로) */
    TMapData.TMapPathType PATH_TYPE = null;

    /* 출발지나 도착지로 지정한 지역 위도, 경도 저장 변수
     *  - 출발지 설정했을떄 지도의 중앙으로 포커싱 하기 위해 사용 */
    double tempLati, tempLongi = 0;

    /* 출발지, 도착지 아이콘 설정 변수 */
    Bitmap startIcon, endIcon;

    /* 마커 */
    TMapMarkerItem mapMarker;

    PathAsync pathAsync;
    SearchAsync searchAsync;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tmap_search_path);

        tmapLayout = (RelativeLayout)findViewById(R.id.tmapLayout);
        carType_rel = (RelativeLayout)findViewById(R.id.carType_rel);
        walkTyep_rel = (RelativeLayout)findViewById(R.id.walkTyep_rel);
        mapSearch_rel = (RelativeLayout)findViewById(R.id.mapSearch_rel);
        areaBottom_rel = (RelativeLayout)findViewById(R.id.areaBottom_rel);
        start_endPath_linear = (LinearLayout)findViewById(R.id.start_endPath_linear);
        startPath_tv = (EditText) findViewById(R.id.startPath_tv);
        endPath_tv = (EditText) findViewById(R.id.endPath_tv);
        areaName = (TextView)findViewById(R.id.areaName);
        areaBizName = (TextView)findViewById(R.id.areaBizName);
        areaDistance = (TextView)findViewById(R.id.areaDistance);
        areaAddress = (TextView)findViewById(R.id.areaAddress);
        initial_startPath = (ImageView)findViewById(R.id.initial_startPath);
        mapSearchEt = (EditText)findViewById(R.id.mapSearchEt);
        searchDateRecycle = (RecyclerView)findViewById(R.id.searchDateRecycle);

        /* 출발지, 도착지 아이콘 설정 */
        startIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_markerpin);
        endIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_end_markerpin);

        /* 검색된 데이터 출력할 RecyclerView */
        searchLM = new LinearLayoutManager(this);
        searchDateRecycle.setLayoutManager(searchLM);
        mapSearchAdapter = new MapSearchAdapter(this, searchData);
        searchDateRecycle.setAdapter(mapSearchAdapter);

        tmapView = new TMapView(this);
        tMapGpsManager = new TMapGpsManager(this);
        tMapData = new TMapData();

        tmapView.setSKTMapApiKey(tmapApiKey);
        tmapLayout.addView(tmapView);
        tmapView.setZoomLevel(15);
        tmapView.setIconVisibility(true);   //  현재 위치 핀 표시
        tmapView.setCompassMode(true);  //  현재 보는 방향
        tMapGpsManager.setProvider(tMapGpsManager.NETWORK_PROVIDER);    //  연결된 인터넷으로 현재 위치를 받는다
        tMapGpsManager.OpenGps();
        tmapView.setTrackingMode(true); //  화면 중심을 현재위치로 이동
        tmapView.setSightVisible(true);

        start_endPath_linear.setVisibility(View.GONE);
        visibilityBottom(false);

        mapSearchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                /* 검색바의 글자를 모두 지웠을때 Recyclerview 를 안보이게한다 */
                if(editable.toString().equals("")) {
                    searchData.clear();
                    mapSearchAdapter.updateMapDate(searchData);
                    searchDateRecycle.setVisibility(View.GONE);
                }
            }
        });

    }

    /* 연결된 인터넷을 통해 TmapGPS 로 위도와 경도를 받아온다
     *  - 출발지가 설정되지 않았으면 현재 위치를 initialPoint 에 넣어준다 */
    @Override
    public void onLocationChange(Location location) {
        if(trackingMode) {
            tmapView.setLocationPoint(location.getLongitude(), location.getLatitude());
            initialPoint = new TMapPoint(location.getLatitude(), location.getLongitude());
        }

    }

    /* View 클릭 이벤트 */
    public void searchMap(View view) {
        switch (view.getId()) {
            /* 장소 검색 */
            case R.id.mapSearchIv:
                try{
                    searchDateRecycle.setVisibility(View.VISIBLE);
                    searchAsync = new SearchAsync();
                    searchAsync.execute(mapSearchEt.getText().toString());
                }catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            /* 자동차 경로로 경로탐색 설정 */
            case R.id.carType_rel:
                walkTyep_rel.setBackgroundResource(R.drawable.map_search_circle);
                carType_rel.setBackgroundResource(R.drawable.map_type_circle);
                PATH_TYPE = TMapData.TMapPathType.CAR_PATH; //  자동차 경로로 탐색
                break;

            /* 보행 경로로 경로탐색 설정 */
            case R.id.walkTyep_rel:
                carType_rel.setBackgroundResource(R.drawable.map_search_circle);
                walkTyep_rel.setBackgroundResource(R.drawable.map_type_circle);
                PATH_TYPE = TMapData.TMapPathType.PEDESTRIAN_PATH;  //  보행 경로로 탐색
                break;

            /* 출발지 재설정
             *  - 지도에 표시된 경로와 마커를 모두 제거한다 */
            case R.id.initial_startPath:
                tMapGpsManager.OpenGps();
                start_endPath_linear.setVisibility(View.GONE);
                mapSearch_rel.setVisibility(View.VISIBLE);
                mapSearchEt.setText("");
                if(polyLine != null) {
                    tmapView.removeAllTMapPolyLine();
                    tmapView.removeAllMarkerItem();
                    pathAsync.cancel(true);
                }

                tmapView.setIconVisibility(true);
                tmapView.setSightVisible(true);
                tmapView.setTrackingMode(true);
                tmapView.setCompassMode(true);

                /* 출발지와 도착지를 초기화한다
                 *  - 출발지는 설정해줬을때만 초기화한다 */
                startPoint = null;
                endPoint = null;
                startPath_tv.setText("");
                endPath_tv.setText("");
                break;

            /* 하단 출발지 설정 버튼
             *  - startPoint 변수에 데이터 재설정 해주고
             *  - 선택된 출발지를 지도 중앙으로 포커싱한다 */
            case R.id.setStart_btn:
                tMapGpsManager.CloseGps();
                isSetStart = true;
                startPoint = new TMapPoint(tempLati, tempLongi);
                if(start_endPath_linear.getVisibility() == View.GONE) {
                    start_endPath_linear.setVisibility(View.VISIBLE);
                }
                tmapView.setCenterPoint(tempLongi, tempLati);   //  설정된 출발지로 화면 중앙 이동
                tmapView.setIconVisibility(false);
                tmapView.setSightVisible(false);
                tmapView.setTrackingMode(false);
                addMarker(startPoint, startIcon, true);     //  출발지 마커 추가
                startPath_tv.setText(areaName.getText().toString());    //  출발지명을 상단 텍스트뷰에 넣는다
                Toast.makeText(this, "출발지를 설정했습니다.", Toast.LENGTH_SHORT).show();
                visibilityBottom(false);
                mapSearchEt.setText("");
                break;

            /* 하단 도착지 설정 버튼
             *  - endPoint 변수에 데이터 재설정 해주고
             *  - 출발지에서부터의 경로를 탐색한다
             *  - 출발지가 지정되어있지 않으면 현재 위치에서부터의 경로를 탐색한다 */
            case R.id.setEnd_btn:
                endPoint = new TMapPoint(tempLati, tempLongi);
                endPath_tv.setText(areaName.getText().toString());
                start_endPath_linear.setVisibility(View.VISIBLE);
                mapSearch_rel.setVisibility(View.GONE);
                addMarker(endPoint, endIcon, false);
                visibilityBottom(false);
                polyLine = new TMapPolyLine();
                searchAsync.cancel(true);
                pathAsync = new PathAsync();
                pathAsync.execute(polyLine);
                break;

            /* 하단 Bottom sheet 취소 */
            case R.id.areaBotton_cancel:
                walkTyep_rel.setBackgroundResource(R.drawable.map_search_circle);
                carType_rel.setBackgroundResource(R.drawable.map_search_circle);
                visibilityBottom(false);
                PATH_TYPE = null;
                mapSearchEt.setText("");
                break;

        }
    }

    /* 검색어로 검색된 장소 데이터 받아오는 AsyncTask */
    class SearchAsync extends AsyncTask<String, Void, ArrayList<TMapPOIItem>> {

        @Override
        protected ArrayList<TMapPOIItem> doInBackground(String... strings) {
            String searchText = strings[0];
            ArrayList<TMapPOIItem> tempSearchData = new ArrayList<>();
            try {
                tempSearchData = tMapData.findTitlePOI(searchText); //  검색어에 해당되는 지역 데이터를 모두 가져온다
                return tempSearchData;
            }catch (Exception e) {
                e.printStackTrace();
                return tempSearchData;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<TMapPOIItem> tMapPOIItems) {
            super.onPostExecute(tMapPOIItems);
            Log.e("검색 데이터", tMapPOIItems.toString() + ", 크기 : " + tMapPOIItems.size());
            mapSearchAdapter.updateMapDate(tMapPOIItems);
            mapSearchAdapter.setPoint(initialPoint);  //  검색된 지역과 현재 위치와의 거리를 계산하기 위해 현재 위치를 넘긴다
        }
    }

    /* 경로탐색 AsyncTask
     *  - 출발지가 설정되어있을 경우에는 출발지부터 도착지까지의 경로를 탐색한다
     *  - 출발지가 설정되어있지 않은 경우에는 현재 위치부터 도착지까지의 경로를 탐색한다 */
    class PathAsync extends AsyncTask<TMapPolyLine, Void, TMapPolyLine> {
        @Override
        protected TMapPolyLine doInBackground(TMapPolyLine... tMapPolyLines) {
            TMapPolyLine tempPoly = tMapPolyLines[0];
            try{
                if(PATH_TYPE != null) { // 경로 타입을 지정했을 경우
                    if(isSetStart) {    // 출발지 설정
                        tempPoly = tMapData.findPathDataWithType(PATH_TYPE, startPoint, endPoint);
                    }else { // 출발지 설정 안함
                        tempPoly = tMapData.findPathDataWithType(PATH_TYPE, initialPoint, endPoint);
                    }
                }else { // 경로 타입 지정안했을 경우
                    if(isSetStart) {    // 출발지 설정
                        tempPoly = tMapData.findPathData(startPoint, endPoint);
                    }else { // 출발지 설정 안함
                        tempPoly = tMapData.findPathData(initialPoint, endPoint);
                    }
                }
                Log.e("경로 검색", tempPoly.toString());
                tempPoly.setLineColor(Color.rgb(255, 59,29));
                tempPoly.setLineWidth(3);
            }catch (Exception e) {

            }
            return tempPoly;
        }

        @Override
        protected void onPostExecute(TMapPolyLine tMapPolyLine) {
            super.onPostExecute(tMapPolyLine);
            polyLine = tMapPolyLine;
            tmapView.addTMapPolyLine("searchPath", polyLine);   //  경로를 그린다
        }
    }

    /* 경로 검색을 하기 위해서
     *  - 검색된 주소 중 하나를 클릭했을때 클릭된 곳의 위도와 경도를 tempLati, tempLongi 에 저장하고
     *  - 하단의 Bottom sheet 에 클릭된 곳의 지명, 주소, 거리를 넣어준다 */
    public void setEndpoint(final double latitude, final double longitude, String areaN, String areaD, String areaAd, String areaBiz) {
        tempLati = latitude;
        tempLongi = longitude;
        searchDateRecycle.setVisibility(View.GONE);
        visibilityBottom(true);
        areaName.setText(areaN);
        areaDistance.setText(areaD);
        areaBizName.setText(areaBiz);
        areaAddress.setText(areaAd);
    }

    /* 지도에 마커 찍기 메소드 */
    public void addMarker(TMapPoint tPoint, Bitmap mIcon, boolean isStartM) {
        mapMarker = new TMapMarkerItem();
        mapMarker.setTMapPoint(tPoint);
        mapMarker.setName(areaName.getText().toString());
        mapMarker.setVisible(TMapMarkerItem.VISIBLE);
        mapMarker.setIcon(mIcon);
        if(isStartM) {
            tmapView.addMarkerItem("startMaker", mapMarker);
        }else {
            tmapView.addMarkerItem("endMarker", mapMarker);
        }
    }

    /* 하단 지역 정보 뷰 보일지 안보일지 여부 메소드 */
    public void visibilityBottom(boolean isVisible) {
        if(isVisible) {
            areaBottom_rel.setVisibility(View.VISIBLE);
            carType_rel.setVisibility(View.VISIBLE);
            walkTyep_rel.setVisibility(View.VISIBLE);
        }else {
            areaBottom_rel.setVisibility(View.GONE);
            walkTyep_rel.setVisibility(View.GONE);
            carType_rel.setVisibility(View.GONE);
        }
    }

}
