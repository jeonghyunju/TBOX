package com.example.hyunjujung.tbox.adapter.user;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.hyunjujung.tbox.R;
import com.example.hyunjujung.tbox.data_vo.user.FavoriteVO;

import java.util.ArrayList;

/**
 *  [ 내가 추가한 영상 목록 어댑터 클래스 ]
 *
 */
public class FavoriteVodAdapter extends RecyclerView.Adapter<FavoriteVodAdapter.Viewholder> {
    Context context;
    ArrayList<FavoriteVO> favoriteArray;
    String PATH_URL = "http://52.78.51.174/vodThumb/";
    int minTime, hourTime = 0;

    public FavoriteVodAdapter(Context context, ArrayList<FavoriteVO> favoriteArray) {
        this.context = context;
        this.favoriteArray = favoriteArray;
    }

    @Override
    public Viewholder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_videoitem, parent, false);
        Viewholder viewholder = new Viewholder(view);
        return viewholder;
    }

    @Override
    public void onBindViewHolder(Viewholder holder, int position) {
        holder.public_private.setVisibility(View.GONE);

        minTime = (int)(favoriteArray.get(position).getStartTime() / (1000 * 60));
        hourTime = (int)(favoriteArray.get(position).getStartTime() / (1000 * 60 * 60));

        Glide.with(context.getApplicationContext())
                .load(PATH_URL + favoriteArray.get(position).getVodPath() + ".png")
                .into(holder.favorite_thumb);
        holder.videoTitle.setText(favoriteArray.get(position).getVodTitle());
        holder.videoHostId.setText(favoriteArray.get(position).getVodHostId());

        /* ~시간전, ~일전 표시 */
        if(minTime < 60) {
            holder.progressTime.setText(minTime + "분전");
        }else if(minTime >= 60 && hourTime < 24) {
            holder.progressTime.setText(hourTime + "시간전");
        }else if(hourTime >= 24 && (hourTime/24) < 7) {
            holder.progressTime.setText(hourTime / 24 + "일전");
        }else if((hourTime/24) >= 7 && (hourTime/24) < 30) {
            holder.progressTime.setText((hourTime/24)/7 + "주전");
        }else if((hourTime/24) >= 30) {
            holder.progressTime.setText((hourTime/24)/30 + "달전");
        }
    }

    @Override
    public int getItemCount() {
        return favoriteArray.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder {
        public ImageView favorite_thumb;
        public TextView videoTitle, videoHostId, progressTime;
        public ImageView public_private;

        public Viewholder(View itemView) {
            super(itemView);
            favorite_thumb = (ImageView)itemView.findViewById(R.id.myVideo_thumb);
            videoTitle = (TextView)itemView.findViewById(R.id.videoTitle);
            videoHostId = (TextView)itemView.findViewById(R.id.videoHostId);
            progressTime = (TextView)itemView.findViewById(R.id.progressTime);
            public_private = (ImageView)itemView.findViewById(R.id.public_private);
        }
    }

    public void updateFavorite(ArrayList<FavoriteVO> fArray) {
        this.favoriteArray = fArray;
        notifyDataSetChanged();
    }
}
