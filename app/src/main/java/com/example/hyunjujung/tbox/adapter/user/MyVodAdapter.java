package com.example.hyunjujung.tbox.adapter.user;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.hyunjujung.tbox.R;
import com.example.hyunjujung.tbox.data_vo.broadcast.StreamLiveListVO;
import com.example.hyunjujung.tbox.fragment.UserMain_BottomSheet;

import java.util.ArrayList;

/**
 *  [ FragUser_main.java 화면에서 MY 비디오 목록 어댑터 클래스 ]
 *
 *  - 아이템에는 VOD 썸네일, VOD 제목, VOD 호스트아이디, 경과 시간, 공개/비공개 아이콘이 포함된다
 *  - 공개/비공개는 Bottom Sheet Dialog (UserMain_BottomSheet.java) 로 구현
 *  - UserMai_BottomSheet.java 에 영상의 인덱스 번호를 set 한다
 *  - set 한 인덱스 번호로 데이터베이스의 live_set 을 업데이트 한다
 */

public class MyVodAdapter extends RecyclerView.Adapter<MyVodAdapter.Viewholder>{
    Context context;
    ArrayList<StreamLiveListVO> myVodArray;
    String PATH_URL = "http://52.78.51.174/vodThumb/";
    android.support.v4.app.FragmentManager fragmentManager;
    long minuteTime, hourTime = 0;

    public MyVodAdapter(Context context, ArrayList<StreamLiveListVO> myVodArray, FragmentManager fragmentManager) {
        this.context = context;
        this.myVodArray = myVodArray;
        this.fragmentManager = fragmentManager;
    }

    @Override
    public Viewholder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_videoitem, parent, false);
        Viewholder viewholder = new Viewholder(view);
        return viewholder;
    }

    @Override
    public void onBindViewHolder(final Viewholder holder, final int position) {
        Glide.with(context.getApplicationContext())
                .load(PATH_URL + myVodArray.get(position).getVod_thumbnail())
                .into(holder.myVideo_thumb);    // VOD 썸네일
        holder.videoTitle.setText(myVodArray.get(position).getLive_title());    // VOD 타이틀
        holder.videoHostId.setText(myVodArray.get(position).getHost_ID());  // 영상주인의 아이디

        if(myVodArray.get(position).getLive_set().equals("공개")) {
            holder.public_private.setImageResource(R.drawable.ic_visibility);
        }else {
            holder.public_private.setImageResource(R.drawable.ic_visibility_off);
        }

        minuteTime = myVodArray.get(position).getDuration() / (1000 * 60);
        hourTime = myVodArray.get(position).getDuration() / (1000 * 60 * 60);

        /* ~시간전, ~일전 표시하기 위해 */
        if(minuteTime < 60) {
            holder.progressTime.setText(minuteTime + "분 전");
        }else if(minuteTime >= 60 && hourTime < 24) {
            holder.progressTime.setText(hourTime + "시간 전");
        }else if(hourTime >= 24 && (hourTime/24) < 7) {
            holder.progressTime.setText(hourTime / 24 + "일 전");
        }else if((hourTime/24) >= 7 && (hourTime/24) < 30) {
            holder.progressTime.setText((hourTime/24)/7 + "주 전");
        }else if((hourTime/24) >= 30) {
            holder.progressTime.setText((hourTime/24)/30 + "달 전");
        }

        /* 비공개/공개 설정 버튼
         *  - 클릭 시에 하단에서 알림창 띄운다 (BottomSheet) */
        holder.public_private.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserMain_BottomSheet bottomSheet = UserMain_BottomSheet.getInstance();
                bottomSheet.setVod_idx(myVodArray.get(position).getLive_idx());
                bottomSheet.setVodSet(myVodArray.get(position).getLive_set());
                bottomSheet.setViewholder(holder);
                bottomSheet.show(fragmentManager, "Public Bottom Sheet");
            }
        });
    }

    @Override
    public int getItemCount() {
        return myVodArray.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder{
        public ImageView myVideo_thumb;
        public TextView videoTitle, videoHostId, progressTime;
        public ImageView public_private;

        public Viewholder(View itemView) {
            super(itemView);
            myVideo_thumb = (ImageView)itemView.findViewById(R.id.myVideo_thumb);
            videoTitle = (TextView)itemView.findViewById(R.id.videoTitle);
            videoHostId = (TextView)itemView.findViewById(R.id.videoHostId);
            progressTime = (TextView)itemView.findViewById(R.id.progressTime);
            public_private = (ImageView) itemView.findViewById(R.id.public_private);
        }
    }

    public void updateMyvod(ArrayList<StreamLiveListVO> myarray) {
        this.myVodArray = myarray;
        notifyDataSetChanged();
    }
}
