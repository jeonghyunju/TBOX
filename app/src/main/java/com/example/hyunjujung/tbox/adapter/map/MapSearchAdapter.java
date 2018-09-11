package com.example.hyunjujung.tbox.adapter.map;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.hyunjujung.tbox.R;
import com.example.hyunjujung.tbox.streaming_main.map.TmapSearchPath;
import com.skt.Tmap.TMapPOIItem;
import com.skt.Tmap.TMapPoint;

import java.util.ArrayList;

/**
 *  [ TMap 경로 탐색에서 장소 검색 후 검색되는 데이터의 어댑터 클래스 ]
 *
 */

public class MapSearchAdapter extends RecyclerView.Adapter<MapSearchAdapter.Viewholder>{
    Context context;
    ArrayList<TMapPOIItem> searchDataArray;
    TMapPoint startPoint;

    public MapSearchAdapter(Context context, ArrayList<TMapPOIItem> searchDataArray) {
        this.context = context;
        this.searchDataArray = searchDataArray;
    }

    @Override
    public Viewholder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context.getApplicationContext()).inflate(R.layout.map_search_item, parent, false);
        Viewholder viewholder = new Viewholder(view);
        return viewholder;
    }

    @Override
    public void onBindViewHolder(final Viewholder holder, final int position) {
        holder.addressIv.setImageResource(R.drawable.ic_location_on_black);
        holder.addressName.setText(searchDataArray.get(position).name); //  검색된 지명 이름
        holder.addressRoadName.setText(searchDataArray.get(position).upperAddrName + " "
                                        + searchDataArray.get(position).middleAddrName + " "
                                        + searchDataArray.get(position).lowerAddrName); //  검색된 지역 주소
        holder.addressDistance.setText(changeDistance(searchDataArray.get(position).getDistance(startPoint)));  //  현재 위치와 검색된 지역간의 거리

        /* 검색된 데이터 한건 클릭
         *  - 클릭된 주소의 위도, 경도를 가지고 현재 위치나 출발지에서 경로 탐색을 시작한다 */
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((TmapSearchPath)context).setEndpoint(Double.parseDouble(searchDataArray.get(position).noorLat),
                                                        Double.parseDouble(searchDataArray.get(position).noorLon),
                                                        searchDataArray.get(position).name,
                                                        changeDistance(searchDataArray.get(position).getDistance(startPoint)),
                                                        searchDataArray.get(position).upperAddrName + " "
                                                                + searchDataArray.get(position).middleAddrName + " "
                                                                + searchDataArray.get(position).lowerAddrName,
                                                        searchDataArray.get(position).middleBizName);
            }
        });
    }

    @Override
    public int getItemCount() {
        if(searchDataArray.size() > 0) {
            return searchDataArray.size();
        }
        return 0;
    }

    public class Viewholder extends RecyclerView.ViewHolder {
        public ImageView addressIv;
        public TextView addressName, addressRoadName, addressDistance;
        public Viewholder(View itemView) {
            super(itemView);
            addressIv = (ImageView)itemView.findViewById(R.id.addressIv);
            addressName = (TextView)itemView.findViewById(R.id.addressName);
            addressRoadName = (TextView)itemView.findViewById(R.id.addressRoadName);
            addressDistance = (TextView)itemView.findViewById(R.id.addressDistance);
        }
    }

    public void updateMapDate(ArrayList<TMapPOIItem> mapArray) {
        this.searchDataArray = mapArray;
        notifyDataSetChanged();
    }

    public void setPoint(TMapPoint startPoint) {
        this.startPoint = startPoint;
    }

    /* 거리 숫자 표시 메소드
     *  - 1000m 가 넘으면 km 로 표시한다 */
    public String changeDistance(double dis) {
        String temp;
        if((int)Math.floor(dis) >= 1000) {
            temp = ((int)Math.floor(dis)) / 1000 + "km";
        }else {
            temp = (int)Math.floor(dis) + "m";
        }
        return temp;
    }
}
