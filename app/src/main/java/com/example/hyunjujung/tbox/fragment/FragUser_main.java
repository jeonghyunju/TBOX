package com.example.hyunjujung.tbox.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.hyunjujung.tbox.R;
import com.example.hyunjujung.tbox.adapter.user.Bookmark_Adapter;
import com.example.hyunjujung.tbox.adapter.user.FavoriteVodAdapter;
import com.example.hyunjujung.tbox.adapter.user.MyVodAdapter;
import com.example.hyunjujung.tbox.data_vo.user.BookAndMember;
import com.example.hyunjujung.tbox.data_vo.user.BookmarkVO;
import com.example.hyunjujung.tbox.data_vo.user.FavoriteVO;
import com.example.hyunjujung.tbox.data_vo.broadcast.StreamLiveListVO;
import com.example.hyunjujung.tbox.data_vo.broadcast.UserVideoArray;
//import com.example.hyunjujung.tbox.databinding.FraguserMainBinding;
import com.example.hyunjujung.tbox.retrofit.Retrofit_ApiConfig;
import com.example.hyunjujung.tbox.retrofit.Retrofit_Creator;
import com.example.hyunjujung.tbox.streaming_main.broadcast.LiveStreamSetting;

import java.util.ArrayList;

import retrofit2.Callback;
import retrofit2.Response;


/**
 * Created by hyunjujung on 2018. 5. 24..
 *
 *  [ 하단 버튼 세개중에서 ME 를 누르면 나오는 화면 ]
 *
 *  - 라이브방송 버튼으로 실시간 스트리밍을 시작할 수 있다
 *  - 라이브방송 버튼 밑에는 내가 북마크한 BJ 목록이 있다
 *  - BJ 목록 밑에는 내가 직접 방송했던 VOD 목록과 내가 추가한 VOD 목록이 있다
 *  - 내가 직접 방송했던 VOD 는 비공개/공개 설정을 할 수 있다
 *
 */

public class FragUser_main extends Fragment implements AppBarLayout.OnOffsetChangedListener{
    static final String SERVER = "http://52.78.51.174/";

    //FraguserMainBinding dataBindings_userMain;
    String loginId, login_pro = "";

    /* 북마크 RecyclerView 관련 레이아웃매니저, 어댑터 */
    LinearLayoutManager bookmarkLM, myVodLM, favoriteLM;
    Bookmark_Adapter bookmarkAdap;

    /* 북마크 리스트 담을 어레이 */
    RecyclerView userBookmark;
    ArrayList<BookmarkVO> bookmarkArray = new ArrayList<>();

    /* 사용자가 방송했던 VOD 목록 RecyclerView 관련 어댑터, Arraylist */
    RecyclerView myVodRecycle;
    MyVodAdapter myVodAdapter;
    ArrayList<StreamLiveListVO> myVodArray = new ArrayList<>();

    /* 사용자가 추가한 VOD 목록 RecyclerView 관련 어댑터, Arraylist */
    RecyclerView favoriteVodRecy;
    FavoriteVodAdapter favoriteAdapter;
    ArrayList<FavoriteVO> favoriteArray = new ArrayList<>();

    TextView userMainUserid, timelineCount, followCount, followingCount, noMyvodTv, noFavoriteTv;

    SwipeRefreshLayout userMainSwipe;

    ImageView userMainProfile;

    Button liveStreamStartBtn;

    AppBarLayout userMainAppBarlayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //dataBindings_userMain = DataBindingUtil.inflate(inflater, R.layout.fraguser_main, container, false);
        View view = inflater.inflate(R.layout.fraguser_main, container, false);

        userBookmark = (RecyclerView)view.findViewById(R.id.userBookmark);
        myVodRecycle = (RecyclerView)view.findViewById(R.id.myVod_recycle);
        favoriteVodRecy = (RecyclerView)view.findViewById(R.id.favoriteVod_recy);
        userMainUserid = (TextView)view.findViewById(R.id.user_main_userid);
        timelineCount = (TextView)view.findViewById(R.id.timeline_count);
        followCount = (TextView)view.findViewById(R.id.follow_count);
        followingCount = (TextView)view.findViewById(R.id.following_count);
        noMyvodTv = (TextView)view.findViewById(R.id.noMyvod_tv);
        noFavoriteTv = (TextView)view.findViewById(R.id.noFavorite_tv);
        userMainSwipe = (SwipeRefreshLayout)view.findViewById(R.id.userMain_swipe);
        userMainProfile = (ImageView)view.findViewById(R.id.user_main_profile);
        liveStreamStartBtn = (Button)view.findViewById(R.id.liveStream_startBtn);
        userMainAppBarlayout = (AppBarLayout)view.findViewById(R.id.user_main_appBarlayout);

        SharedPreferences loginShared = getActivity().getSharedPreferences("logins", Context.MODE_PRIVATE);
        loginId = loginShared.getString("loginId", null);
        login_pro = loginShared.getString("login_pro", null);
        Log.e("프로필 경로", login_pro);

        /* 북마크 RecyclerView */
        bookmarkLM = new LinearLayoutManager(getActivity().getApplicationContext());
        bookmarkLM.setOrientation(LinearLayoutManager.HORIZONTAL);
        userBookmark.setLayoutManager(bookmarkLM);
        bookmarkAdap = new Bookmark_Adapter(getActivity().getApplicationContext(), bookmarkArray);
        userBookmark.setAdapter(bookmarkAdap);

        /* 내가 방송한 VOD 목록 RecyclerView */
        myVodLM = new LinearLayoutManager(getActivity().getApplicationContext());
        myVodRecycle.setLayoutManager(myVodLM);
        myVodAdapter = new MyVodAdapter(getActivity().getApplicationContext(), myVodArray, getActivity().getSupportFragmentManager());
        myVodRecycle.setAdapter(myVodAdapter);

        /* 내가 추가한 VOD 목록 RecyclerView */
        favoriteLM = new LinearLayoutManager(getActivity().getApplicationContext());
        favoriteVodRecy.setLayoutManager(favoriteLM);
        favoriteAdapter = new FavoriteVodAdapter(getActivity().getApplicationContext(), favoriteArray);
        favoriteVodRecy.setAdapter(favoriteAdapter);

        userMainUserid.setText(loginId);

        /* 당겨서 새로고침 */
        userMainSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(bookmarkArray.size() > 0 || myVodArray.size() > 0 || favoriteArray.size() > 0) {
                    bookmarkArray.clear();
                    myVodArray.clear();
                    favoriteArray.clear();
                }
                selectBookmark(loginId);
                selectVideo(loginId);

                userMainSwipe.setRefreshing(false);
            }
        });

        if(login_pro != null) {
            Glide.with(this).load(SERVER + "user_profile/" + login_pro).into(userMainProfile);
        }else {
            userMainProfile.setImageResource(R.drawable.usershape);
        }

        /* 프로필 밑의 라이브 방송 버튼 클릭하면 방송 세팅 화면으로 넘어간다 */
        liveStreamStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent liveSettings = new Intent(getContext(), LiveStreamSetting.class);
                startActivity(liveSettings);
            }
        });

        //return dataBindings_userMain.getRoot();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        selectBookmark(loginId);
        selectVideo(loginId);

        userMainAppBarlayout.addOnOffsetChangedListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        userMainAppBarlayout.removeOnOffsetChangedListener(this);
    }

    /* 현재 로그인한 사용자의 북마크 리스트 모두 가져오는 메소드 */
    public void selectBookmark(String loginId) {
        Retrofit_ApiConfig apiConfig = Retrofit_Creator.getApiConfig();
        retrofit2.Call<BookAndMember> call = apiConfig.select_bookmark(loginId);
        call.enqueue(new Callback<BookAndMember>() {
            @Override
            public void onResponse(retrofit2.Call<BookAndMember> call, Response<BookAndMember> response) {
                if(bookmarkArray.size() > 0) {
                    bookmarkArray.clear();
                }
                bookmarkArray.addAll(response.body().getBookArray());
                Log.e("내 북마크 리스트", bookmarkArray.toString());
                bookmarkAdap.updateBookmark(bookmarkArray);

                timelineCount.setText(response.body().getMemberArray().get(0).getVodCount() + "");
                followCount.setText(response.body().getMemberArray().get(0).getFollowCount() + "");
                followingCount.setText(response.body().getMemberArray().get(0).getFollowingCount() + "");
            }

            @Override
            public void onFailure(retrofit2.Call<BookAndMember> call, Throwable t) {

            }
        });
    }

    /* 현재 로그인한 사용자가 추가한 동영상 & 방송한 동영상 보이는 메소드 */
    public void selectVideo(String loginId) {
        Retrofit_ApiConfig selectVod = Retrofit_Creator.getApiConfig();
        retrofit2.Call<UserVideoArray> vodCall = selectVod.select_userVod(loginId);
        vodCall.enqueue(new Callback<UserVideoArray>() {
            @Override
            public void onResponse(retrofit2.Call<UserVideoArray> call, Response<UserVideoArray> response) {
                if(myVodArray.size() > 0) {
                    myVodArray.clear();
                }else if(favoriteArray.size() > 0) {
                    favoriteArray.clear();
                }
                myVodArray.addAll(response.body().getMyVideoArray());   //  내가 방송했던 영상 목록
                favoriteArray.addAll(response.body().getFavoritArray());    //  내가 추가한 VOD 영상 목록
                if(myVodArray.size() <= 0) {
                    noMyvodTv.setVisibility(View.VISIBLE);
                }else {
                    noMyvodTv.setVisibility(View.INVISIBLE);
                }

                if(favoriteArray.size() <= 0) {
                    noFavoriteTv.setVisibility(View.VISIBLE);
                }else {
                    noFavoriteTv.setVisibility(View.INVISIBLE);
                }

                myVodAdapter.updateMyvod(myVodArray);
                favoriteAdapter.updateFavorite(favoriteArray);

            }

            @Override
            public void onFailure(retrofit2.Call<UserVideoArray> call, Throwable t) {
                Log.e("유저 VOD 목록 가져오기", t.getMessage());
            }
        });
    }

    /* CollapsingToolbar 와 SwipeLayout 같이 사용했을때 스크롤 아래로 내리려고하면 리프레쉬 되는 현상 막기 위해서
     * 스크롤의 위치값이 0 일때만 리프레쉬 되도록 한다 */
    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        userMainSwipe.setEnabled(verticalOffset == 0);
    }
}
