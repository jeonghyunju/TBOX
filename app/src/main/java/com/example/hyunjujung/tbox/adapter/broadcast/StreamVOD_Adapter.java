package com.example.hyunjujung.tbox.adapter.broadcast;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.hyunjujung.tbox.R;
import com.example.hyunjujung.tbox.data_vo.ServerResponse;
import com.example.hyunjujung.tbox.data_vo.broadcast.StreamLiveListVO;
import com.example.hyunjujung.tbox.retrofit.Retrofit_ApiConfig;
import com.example.hyunjujung.tbox.retrofit.Retrofit_Creator;
import com.example.hyunjujung.tbox.streaming_main.OtherUserMain;
import com.example.hyunjujung.tbox.streaming_main.broadcast.VodStreamPlay;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 *  [ 메인화면 VOD 목록 RecyclerView 어댑터 ]
 *
 */

public class StreamVOD_Adapter extends RecyclerView.Adapter<StreamVOD_Adapter.ViewHolder>{
    Context context;
    ArrayList<StreamLiveListVO> vodArray;
    String SERVER_URL = "http://52.78.51.174/";

    public StreamVOD_Adapter(Context context, ArrayList<StreamLiveListVO> vodArray) {
        this.context = context;
        this.vodArray = vodArray;
    }

    @Override
    public StreamVOD_Adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.stream_vod_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        if(vodArray.get(position).getHost_Profile() == null) {
            /* 회원가입 시에 사용자가 프로필 사진을 설정 안했을때는 아이콘으로 프로필을 대체한다 */
            holder.vodHost_profile.setImageResource(R.drawable.ic_clickuser);
        }else {
            Glide.with(context).load(SERVER_URL + "user_profile/" + vodArray.get(position).getHost_Profile()).into(holder.vodHost_profile);
        }

        /* VOD 영상 썸네일 넣기
         *  - 데이터베이스에 썸네일 경로가 null 이면 아이콘을 넣는다 */
        if(vodArray.get(position).getVod_thumbnail() == null) {
            holder.vod_thumbnail.setImageResource(R.drawable.ic_no_vodthumb);
            holder.vod_thumbnail.setBackgroundResource(R.color.thumbnailback);
            //holder.privateTitle.setVisibility(View.GONE);
            holder.invisible_tag.setVisibility(View.GONE);
        }else {
            holder.vod_thumbnail.setBackground(null);
            //holder.privateTitle.setVisibility(View.GONE);
            holder.invisible_tag.setVisibility(View.GONE);
            Glide.with(context.getApplicationContext())
                    .load(SERVER_URL + "vodThumb/" + vodArray.get(position).getVod_thumbnail())
                    .override(500, 170)
                    .into(holder.vod_thumbnail);
        }

        holder.vodHost_ID.setText(vodArray.get(position).getHost_ID());
        holder.vod_title.setText(vodArray.get(position).getLive_title());

        /* 사용자의 VOD 리스트에 해당 영상 추가하는 버튼 클릭 이벤트
         *  - 자기자신의 영상은 추가할 수 없음
         *  - 데이터베이스의 userVod_list 테이블에 추가된다
         *  - 테이블 컬럼 : user_ID (현재 로그인된 사용자 아이디), vodHost_ID (방송 진행했던 호스트 아이디), vod_title (방송제목), vod_path (서버의 VOD 경로), vod_startTime (영상 시작 시간) */
        holder.vodAdd_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!loginShared().equals(vodArray.get(position).getHost_ID())) {
                    insertVod(loginShared(),
                              vodArray.get(position).getHost_ID(),
                              vodArray.get(position).getLive_title(),
                              vodArray.get(position).getLive_path(),
                              vodArray.get(position).getLive_startTime());
                }
            }
        });

        /* VOD 목록 클릭 이벤트
         *  - 클릭 된 VOD의 idx 와 경로를 가지고 플레이 화면으로 이동한다 */
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent go_vodPlay = new Intent(context.getApplicationContext(), VodStreamPlay.class);
                go_vodPlay.putExtra("live_idx", vodArray.get(position).getLive_idx());
                go_vodPlay.putExtra("live_path", vodArray.get(position).getLive_path());
                go_vodPlay.putExtra("vod_tag", vodArray.get(position).getVod_tag());
                go_vodPlay.putExtra("liveStart", vodArray.get(position).getLive_startTime());
                go_vodPlay.putExtra("duration", vodArray.get(position).getDuration());
                go_vodPlay.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(go_vodPlay);
            }
        });

        /* 프로필 이미지 클릭 리스너
         *  - 클릭하면 프로필에 해당하는 유저의 타임라인으로 이동한다 */
        holder.vodHost_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!loginShared().equals(vodArray.get(position).getHost_ID())) {
                    Intent goTimeline = new Intent(context.getApplicationContext(), OtherUserMain.class);
                    goTimeline.putExtra("host_Id", vodArray.get(position).getHost_ID());
                    goTimeline.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(goTimeline);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return vodArray.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView vod_thumbnail, invisible_tag;
        public CircleImageView vodHost_profile;
        public TextView vod_title, vodHost_ID, privateTitle;
        public ImageButton vodAdd_btn;

        public ViewHolder(View itemView) {
            super(itemView);
            vod_thumbnail = (ImageView)itemView.findViewById(R.id.vod_thumbnail);
            invisible_tag = (ImageView)itemView.findViewById(R.id.invisible_tag);
            vodHost_profile = (CircleImageView)itemView.findViewById(R.id.vodHost_profile);
            vod_title = (TextView)itemView.findViewById(R.id.vod_title);
            vodHost_ID = (TextView)itemView.findViewById(R.id.vodHost_ID);
            //privateTitle = (TextView)itemView.findViewById(R.id.privateTitle);
            vodAdd_btn = (ImageButton)itemView.findViewById(R.id.vodAdd_btn);
        }
    }

    public void updateVodlist(ArrayList<StreamLiveListVO> vodlistArray) {
        this.vodArray = vodlistArray;
        notifyDataSetChanged();
    }

    public String loginShared() {
        SharedPreferences loginShared = context.getSharedPreferences("logins", Context.MODE_PRIVATE);
        return loginShared.getString("loginId", null);
    }

    /* VOD 영상의 플러스 버튼 누르면 userVod_list 목록에 추가하는 메소드
     *  - 현재 로그인 아이디, 영상주인 아이디, 영상 제목, 영상 경로, 영상의 생성 시간을 데이터베이스에 저장한다 */
    public void insertVod(String loginId, String hostId, String vodTitle, String vodPath, long startTime) {
        Retrofit_ApiConfig insertApi = Retrofit_Creator.getApiConfig();
        Call<ServerResponse> insertCall = insertApi.insert_vodList(loginId, hostId, vodTitle, vodPath, startTime);
        insertCall.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                Log.e("userVod_list 추가", response.body().getMessage());
                Toast.makeText(context.getApplicationContext(), "영상을 추가하였습니다.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                Log.e("userVod_list 추가", t.getMessage());
            }
        });
    }
}
