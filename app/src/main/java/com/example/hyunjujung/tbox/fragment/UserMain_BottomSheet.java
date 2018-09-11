package com.example.hyunjujung.tbox.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.hyunjujung.tbox.R;
import com.example.hyunjujung.tbox.adapter.user.MyVodAdapter;
import com.example.hyunjujung.tbox.data_vo.ServerResponse;
import com.example.hyunjujung.tbox.retrofit.Retrofit_ApiConfig;
import com.example.hyunjujung.tbox.retrofit.Retrofit_Creator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 *  [ FragUser_main.java 의 MY 비디오 목록 공개/비공개 Bottom Sheet Dialog ]
 *
 */

public class UserMain_BottomSheet extends BottomSheetDialogFragment implements View.OnClickListener {
    public static UserMain_BottomSheet getInstance() {
        return new UserMain_BottomSheet();
    }
    private TextView private_tv, cancel_tv, bt_title;
    private Toolbar bt_toolbar;
    private int vod_idx;
    private static String vodSet;
    private MyVodAdapter.Viewholder viewholder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.usermain_bottom_sheet, container, false);
        bt_toolbar = (Toolbar)view.findViewById(R.id.bt_toolbar);
        private_tv = (TextView)view.findViewById(R.id.private_tv);
        cancel_tv = (TextView)view.findViewById(R.id.cancel_tv);
        bt_title = (TextView)view.findViewById(R.id.bt_title);

        private_tv.setOnClickListener(this);
        cancel_tv.setOnClickListener(this);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e("Bottom Sheet", "onResume" + ", " + getVodSet());
        if(getVodSet().equals("공개")) {
            bt_title.setText("공개 영상입니다. 비공개 하시겠습니까?");
            private_tv.setText("비공개");
        }else {
            bt_title.setText("비공개 영상입니다. 공개 하시겠습니까?");
            private_tv.setText("공개");
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.private_tv:
                if(private_tv.getText().toString().equals("비공개")) {
                    /* 비공개로 바꿈 */
                    setVodSet("비공개");
                    updateDB(getVod_idx(), private_tv.getText().toString());
                    getViewholder().public_private.setImageResource(R.drawable.ic_visibility_off);
                }else {
                    /* 공개로 바꿈 */
                    setVodSet("공개");
                    updateDB(getVod_idx(), private_tv.getText().toString());
                    getViewholder().public_private.setImageResource(R.drawable.ic_visibility);
                }
                break;
            case R.id.cancel_tv:
                dismiss();
                break;
        }
        dismiss();
    }

    /* 데이터베이스에 업데이트
     *  - livestream_list 테이블의 live_set 컬럼 업데이트 */
    public void updateDB(int vod_idx, String vodSet) {
        Retrofit_ApiConfig updateApi = Retrofit_Creator.getApiConfig();
        Call<ServerResponse> updateCall = updateApi.update_liveset(vod_idx, vodSet);
        updateCall.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                Log.e("공개/비공개 업데이트", response.body().getMessage());
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                Log.e("공개/비공개 업데이트", t.getMessage());
            }
        });
    }

    public int getVod_idx() {
        return vod_idx;
    }

    public void setVod_idx(int vod_idx) {
        this.vod_idx = vod_idx;
    }

    public String getVodSet() {
        return vodSet;
    }

    public void setVodSet(String vodSet) {
        this.vodSet = vodSet;
    }

    public MyVodAdapter.Viewholder getViewholder() {
        return viewholder;
    }

    public void setViewholder(MyVodAdapter.Viewholder viewholder) {
        this.viewholder = viewholder;
    }
}
