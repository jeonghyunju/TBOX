package com.example.hyunjujung.tbox.streaming_main;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.hyunjujung.tbox.R;
import com.example.hyunjujung.tbox.adapter.user.OtherUserVodAdapter;
import com.example.hyunjujung.tbox.data_vo.user.BookAndMember;
import com.example.hyunjujung.tbox.data_vo.user.BookmarkVO;
import com.example.hyunjujung.tbox.data_vo.user.MemberAndVod;
import com.example.hyunjujung.tbox.data_vo.user.MemberVO;
import com.example.hyunjujung.tbox.data_vo.ServerResponse;
import com.example.hyunjujung.tbox.data_vo.broadcast.StreamLiveListVO;
import com.example.hyunjujung.tbox.retrofit.Retrofit_ApiConfig;
import com.example.hyunjujung.tbox.retrofit.Retrofit_Creator;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 *  [ FragStream_main 화면의 VOD 목록에서 VOD 에 해당하는 프로필을 누르면 넘어오는 화면 ]
 *
 *  - 다른 사용자의 게시물수, 팔로워수, 팔로잉수를 볼 수 있다
 *  - 다른 사용자가 했던 방송의 VOD 목록을 볼 수 있다
 *
 */

public class OtherUserMain extends AppCompatActivity {
    static final String URI_PATH = "http://52.78.51.174/";
    CircleImageView otherUser_pro;
    TextView otherUser_Id, timeline_count, follow_count, following_count;
    ImageButton setAlert_btn;

    /* Streaming Main 에 있는 VOD 목록에서 프로필을 클릭하고 들어오면
       해당 BJ 의 모든 VOD 동영상 목록을 표시하는 recyclerview */
    RecyclerView otherUser_vod;
    LinearLayoutManager otherUserLM;
    OtherUserVodAdapter otherVodAdap;
    ArrayList<StreamLiveListVO> userVodArray = new ArrayList<>();

    /* 클릭한 프로필에 해당하는 유저 정보 저장하는 ArrayList */
    ArrayList<MemberVO> memberArray = new ArrayList<>();

    /* 로그인한 사용자의 북마크 목록 저장하는 ArrayList */
    ArrayList<BookmarkVO> bookArray = new ArrayList<>();

    Intent intent;
    SharedPreferences loginShared;

    static boolean isBookmark = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.other_user_main);

        otherUser_pro = (CircleImageView)findViewById(R.id.otherUser_pro);
        otherUser_Id = (TextView)findViewById(R.id.otherUser_Id);
        timeline_count = (TextView)findViewById(R.id.timeline_count);
        follow_count = (TextView)findViewById(R.id.follow_count);
        following_count = (TextView)findViewById(R.id.following_count);
        otherUser_vod = (RecyclerView)findViewById(R.id.otherUser_vod);
        setAlert_btn = (ImageButton)findViewById(R.id.setAlert_btn);

        otherUserLM = new LinearLayoutManager(this);
        otherUser_vod.setLayoutManager(otherUserLM);
        otherVodAdap = new OtherUserVodAdapter(this, userVodArray);
        otherUser_vod.setAdapter(otherVodAdap);
    }

    @Override
    protected void onResume() {
        super.onResume();

        intent = getIntent();

        loginShared = getSharedPreferences("logins", MODE_PRIVATE);

        /* VOD 목록에서 프로필을 누르고 해당 사용자의 타임라인 화면에 넘어오면
         *  - 해당 사용자의 정보를 데이터베이스에서 모두 가져와서 출력한다
         *  - 해당 사용자의 VOD 목록 또한 모두 가져온다 */
        Retrofit_ApiConfig apiConfig = Retrofit_Creator.getApiConfig();
        Call<MemberAndVod> call = apiConfig.select_member(intent.getStringExtra("host_Id"),
                                                            loginShared.getString("loginId", null));
        call.enqueue(new Callback<MemberAndVod>() {
            @Override
            public void onResponse(Call<MemberAndVod> call, Response<MemberAndVod> response) {
                memberArray.addAll(response.body().getMemberArray());   //  클릭한 BJ의 정보를 모두 가져옴
                userVodArray.addAll(response.body().getVodArray()); //  클릭한 BJ의 VOD 목록을 모두 가져옴

                /* 해당 사용자의 정보를 뷰에 모두 출력한다 */
                otherUser_Id.setText(memberArray.get(0).getUserId());
                timeline_count.setText(memberArray.get(0).getVodCount() + "");
                follow_count.setText(memberArray.get(0).getFollowCount() + "");
                following_count.setText(memberArray.get(0).getFollowingCount() + "");
                Glide.with(getApplicationContext())
                        .load(URI_PATH + "user_profile/" + memberArray.get(0).getUserProfile())
                        .into(otherUser_pro);

                otherVodAdap.updateOtherVod(userVodArray);
            }

            @Override
            public void onFailure(Call<MemberAndVod> call, Throwable t) {

            }
        });

        /* 로그인한 사용자가 현재 화면에서 보이는 다른 사용자를 북마크 했다면 종모양의 색이 하얀색
         * 북마크 안했으면 테두리만 하얀색 */
        final Retrofit_ApiConfig bookApi = Retrofit_Creator.getApiConfig();
        Call<BookAndMember> bookCall = bookApi.select_bookmark(loginShared.getString("loginId", null));
        bookCall.enqueue(new Callback<BookAndMember>() {
            @Override
            public void onResponse(Call<BookAndMember> call, Response<BookAndMember> response) {
                bookArray.addAll(response.body().getBookArray());   //  로그인 사용자의 북마크 리스트 모두 가져옴
                for(int i=0 ; i<bookArray.size() ; i++) {
                    if(bookArray.get(i).getBookmarkId().equals(intent.getStringExtra("host_Id"))) {
                        isBookmark = true;  //  북마크 목록에 현재 화면의 BJ 아이디가 존재한다면 true 저장
                        setAlert_btn.setImageResource(R.drawable.ic_adduser_cancel_24dp);   //  흰색종으로 이미지 넣어줌
                    }
                }
            }

            @Override
            public void onFailure(Call<BookAndMember> call, Throwable t) {

            }
        });

    }

    public void otherUser_click(View view) {
        int id = view.getId();
        loginShared = getSharedPreferences("logins", MODE_PRIVATE);
        switch (id) {

            /* 종 모양 버튼을 누르면 데이터 베이스의 bookmark_list 테이블에 insert 한다
             *  - insert 된 사용자가 방송을 시작하면 북마크한 유저에게 알림이 가게 한다
             *  - 이미 북마크 된 사용자라면 다시 클릭했을때 북마크를 취소할꺼냐는 다어얼로그 띄운다 */
            case R.id.setAlert_btn:
                if(isBookmark) {
                    /* 이미 북마크된 사용자일때 다이얼로그 띄움
                     *  - bookmark_cancel : 데이터베이스의 북마크 테이블에서 한건 지운다
                     */
                    DialogInterface.OnClickListener bookmark_cancel = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Retrofit_ApiConfig apiConfig = Retrofit_Creator.getApiConfig();
                            Call<ServerResponse> call = apiConfig.delete_bookmark(loginShared.getString("loginId", null),
                                                                                    memberArray.get(0).getUserId());
                            call.enqueue(new Callback<ServerResponse>() {
                                @Override
                                public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                                    Log.e("북마크 삭제 ", response.body().getMessage());
                                    setAlert_btn.setImageResource(R.drawable.ic_add_user);
                                }

                                @Override
                                public void onFailure(Call<ServerResponse> call, Throwable t) {
                                    Log.e("북마크 삭제", t.getMessage());
                                }
                            });

                            isBookmark = false;
                        }
                    };
                    DialogInterface.OnClickListener cancel_dial = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    };
                    new AlertDialog.Builder(this)
                            .setTitle("알림")
                            .setMessage("방송 알림을 받지 않겠습니까?")
                            .setPositiveButton("예", bookmark_cancel)
                            .setNegativeButton("아니오", cancel_dial)
                            .show();

                }else {
                    /* 북마크된 사용자가 아닐떄
                     *  - 데이터베이스에 insert 한다 */
                    Retrofit_ApiConfig apiConfig = Retrofit_Creator.getApiConfig();
                    Call<ServerResponse> call = apiConfig.insert_bookmark(loginShared.getString("loginId", null),
                                                                            memberArray.get(0).getUserId(), memberArray.get(0).getUserProfile(),
                                                                            loginShared.getString("login_token", null));
                    call.enqueue(new Callback<ServerResponse>() {
                        @Override
                        public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                            Log.e("북마크 insert ", response.body().getMessage());
                        }

                        @Override
                        public void onFailure(Call<ServerResponse> call, Throwable t) {
                            Log.e("북마크 insert ", t.getMessage());
                        }
                    });
                    Toast.makeText(this, "방송 알림을 설정합니다.", Toast.LENGTH_SHORT).show();
                    setAlert_btn.setImageResource(R.drawable.ic_adduser_cancel_24dp);
                    isBookmark = true;
                }
                break;

            /* 현재 화면 닫기 */
            case R.id.closeActivityBtn:
                finish();
                break;
        }
    }
}
