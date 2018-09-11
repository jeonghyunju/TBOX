package com.example.hyunjujung.tbox.retrofit;

import com.example.hyunjujung.tbox.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by hyunjujung on 2018. 5. 22..
 *
 * [ 데이터베이스에 접속하기 위해서 HTTP 연결하고, Retrofit 설정 해주는 파일 ]
 *
 */

public class Retrofit_Creator {
    private static final String SERVER = "http://52.78.51.174";
    public static Retrofit createRetrofit() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                //.readTimeout(3, TimeUnit.MINUTES)
                //.connectTimeout(3, TimeUnit.MINUTES)
                .build();
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        return new Retrofit.Builder()
                .baseUrl(SERVER)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    public static Retrofit_ApiConfig getApiConfig() {
        return createRetrofit().create(Retrofit_ApiConfig.class);
    }
}
