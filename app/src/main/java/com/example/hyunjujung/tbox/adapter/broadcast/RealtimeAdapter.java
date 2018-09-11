package com.example.hyunjujung.tbox.adapter.broadcast;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.hyunjujung.tbox.R;
import com.example.hyunjujung.tbox.data_vo.broadcast.RealtimeListVO;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 *
 *  [ 실시간 급상승 목록 RecyclerView 어댑터 클래스 ]
 *
 *  - FragStream_main 화면에 있는 실시간 급상승 목록의 데이터를 담을 어댑터 클래스
 */

public class RealtimeAdapter extends RecyclerView.Adapter<RealtimeAdapter.ViewHolder>{
    Context context;
    ArrayList<RealtimeListVO> realtimeArray;
    private String SERVER_URL = "http://52.78.51.174/vodThumb/";
    DecimalFormat seeNumDecimal = new DecimalFormat("###,###");
    String tempSeeNum;

    public RealtimeAdapter(Context context, ArrayList<RealtimeListVO> realtimeArray) {
        this.context = context;
        this.realtimeArray = realtimeArray;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.realtime_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Glide.with(context)
                .load(SERVER_URL + realtimeArray.get(position).getRealtimeThumb())
                .into(holder.thumbnailIV);

        holder.titleTV.setText("[제목] " + realtimeArray.get(position).getRealtimeTitle());
        holder.titleTV.setSelected(true);

        if(realtimeArray.get(position).getRealtimeCount() >= 1000) {
            tempSeeNum = seeNumDecimal.format(realtimeArray.get(position).getRealtimeCount());
            holder.seeNumTV.setText(tempSeeNum + " 명 시청 중");
        }else {
            holder.seeNumTV.setText(realtimeArray.get(position).getRealtimeCount() + " 명 시청 중");
        }
    }

    @Override
    public int getItemCount() {
        return realtimeArray.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView thumbnailIV;
        public TextView titleTV, seeNumTV;
        public ViewHolder(View itemView) {
            super(itemView);
            thumbnailIV = itemView.findViewById(R.id.thumbnailIV);
            titleTV = itemView.findViewById(R.id.titleTV);
            seeNumTV = itemView.findViewById(R.id.seeNumTV);
        }
    }

    public void updateRealtime(ArrayList<RealtimeListVO> realtimeArray) {
        this.realtimeArray = realtimeArray;
        notifyDataSetChanged();
    }
}
