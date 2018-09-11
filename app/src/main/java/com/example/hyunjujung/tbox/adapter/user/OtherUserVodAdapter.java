package com.example.hyunjujung.tbox.adapter.user;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.hyunjujung.tbox.R;
import com.example.hyunjujung.tbox.data_vo.broadcast.StreamLiveListVO;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 *  [ 다른 사용자의 (OtherUserMain.java) 타임라인에 있는 VOD 목록 어댑터 클래스 ]
 *
 *  - 공개/비공개 영상 표시
 *
 */

public class OtherUserVodAdapter extends RecyclerView.Adapter<OtherUserVodAdapter.ViewHolder>{
    Context context;
    ArrayList<StreamLiveListVO> otherVodArray;
    String PATH_URL = "http://52.78.51.174/";

    public OtherUserVodAdapter(Context context, ArrayList<StreamLiveListVO> otherVodArray) {
        this.context = context;
        this.otherVodArray = otherVodArray;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.stream_vod_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        /* VOD 썸네일 적용
         *  - VOD 가 공개일 경우 썸네일 보여주고, 영상 추가 버튼 표시
         *  - 비공개면 비공개 표시 이미지 보이기, 영상 추가 버튼 안보이기 */
        if(otherVodArray.get(position).getLive_set().equals("공개")) {    //  공개
            holder.vod_thumbnail.setBackground(null);
            holder.invisible_tag.setVisibility(View.GONE);
            //holder.privateTitle.setVisibility(View.GONE);
            Glide.with(context.getApplicationContext())
                    .load(PATH_URL + "vodThumb/" + otherVodArray.get(position).getVod_thumbnail())
                    .override(500, 170)
                    .into(holder.vod_thumbnail);
        }else {     //  비공개
            holder.vod_tag.setVisibility(View.GONE);
            holder.vod_thumbnail.setBackground(null);
            holder.invisible_tag.setVisibility(View.VISIBLE);
            holder.vod_thumbnail.setImageResource(R.drawable.ic_no_vodthumb);
            //holder.privateTitle.setVisibility(View.VISIBLE);
            holder.vodAdd_btn.setVisibility(View.GONE);
        }

        Glide.with(context.getApplicationContext())
                .load(PATH_URL + "user_profile/" + otherVodArray.get(position).getHost_Profile())
                .into(holder.vodHost_profile);
        holder.vod_title.setText(otherVodArray.get(position).getLive_title());
        holder.vodHost_ID.setText(otherVodArray.get(position).getHost_ID());

        /* 공개 영상은 userVod_list 에 추가할 수 있도록 한다 */
        holder.vodAdd_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return otherVodArray.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView vod_thumbnail, invisible_tag;
        public CircleImageView vodHost_profile;
        public TextView vod_title, vodHost_ID, privateTitle, vod_tag;
        public ImageButton vodAdd_btn;

        public ViewHolder(View itemView) {
            super(itemView);
            vod_thumbnail = (ImageView)itemView.findViewById(R.id.vod_thumbnail);
            invisible_tag = (ImageView)itemView.findViewById(R.id.invisible_tag);
            vodHost_profile = (CircleImageView)itemView.findViewById(R.id.vodHost_profile);
            vod_title = (TextView)itemView.findViewById(R.id.vod_title);
            vodHost_ID = (TextView)itemView.findViewById(R.id.vodHost_ID);
            //privateTitle = (TextView)itemView.findViewById(R.id.privateTitle);
            vod_tag = (TextView)itemView.findViewById(R.id.vod_tag);
            vodAdd_btn = (ImageButton)itemView.findViewById(R.id.vodAdd_btn);
        }
    }

    public void updateOtherVod(ArrayList<StreamLiveListVO> otherArray) {
        this.otherVodArray = otherArray;
        notifyDataSetChanged();
    }
}
