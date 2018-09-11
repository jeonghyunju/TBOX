package com.example.hyunjujung.tbox.retrofit;

import com.example.hyunjujung.tbox.data_vo.user.BookAndMember;
import com.example.hyunjujung.tbox.data_vo.user.BookmarkVO;
import com.example.hyunjujung.tbox.data_vo.chart.ChartArray;
import com.example.hyunjujung.tbox.data_vo.chatting.ChatItemVO;
import com.example.hyunjujung.tbox.data_vo.broadcast.LiveAndVodArray;
import com.example.hyunjujung.tbox.data_vo.user.MemberAndVod;
import com.example.hyunjujung.tbox.data_vo.ServerResponse;
import com.example.hyunjujung.tbox.data_vo.broadcast.UserVideoArray;

import java.util.List;
import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PartMap;

/**
 *  [ Retrofit 과 통신할 PHP 파일들을 선언하고 어떤 형태의 데이터가 넘어가고, 어떤 데이터를 받는지 지정하는 클래스 파일 ]
 */

public interface Retrofit_ApiConfig {

    /* 사용자가 회원가입 하려고 할 때 데이터베이스로 가서 작성된 회원가입 정보 insert 한다 */
    @Multipart
    @POST("insert_member.php")
    Call<ServerResponse> insert_Members(@PartMap Map<String, RequestBody> params);

    /* 로그인 시에 입력된 아이디와 비밀번호가 맞는지 유효성 검사하러 데이터베이스로 간다 */
    @FormUrlEncoded
    @POST("check_login.php")
    Call<ServerResponse> check_Login(@Field("loginId") String loginId,
                                     @Field("loginPw") String loginPw);

    /* 방송 시작 후 데이터베이스에 방송 추가 하기 위해서 */
    @FormUrlEncoded
    @POST("insert_liveList.php")
    Call<ServerResponse> insert_liveList(@Field("loginId") String loginId,
                                         @Field("liveTitle") String liveTitle,
                                         @Field("liveSet") String liveSet,
                                         @Field("livePath") String livePath,
                                         @Field("vodTag") int vodTag,
                                         @Field("livePass") String livePass,
                                         @Field("vod_thumb") String vodThumb,
                                         @Field("liveStart") long liveStart);

    /* 실시간 방송 목록 가져오기 */
    @FormUrlEncoded
    @POST("select_liveList.php")
    Call<LiveAndVodArray> select_liveList(@Field("host_ID") String host_ID);

    /* 생방송 종료 후에 livestream_list 테이블에서 해당 방송 vod_tag 업데이트 하기 */
    @FormUrlEncoded
    @POST("update_liveList.php")
    Call<ServerResponse> update_liveList(@Field("live_path") String live_path,
                                         @Field("startTime") long startTime,
                                         @Field("endTime") long endTime);

    /* 방송 송출 취소 : livestream_list 에서 한 건 삭제 */
    @FormUrlEncoded
    @POST("delete_liveList.php")
    Call<ServerResponse> delete_liveList(@Field("live_path") String live_path);

    /* Nosql 에 채팅 내용 insert 시키기 */
    @FormUrlEncoded
    @POST("insert_chat.php")
    Call<ServerResponse> insert_chat(@Field("aeroPK") String aeroPK,
                                     @Field("sendUser") String sendUser,
                                     @Field("sendProfile") String sendProfile,
                                     @Field("message") String message,
                                     @Field("roomId") String roomId,
                                     @Field("startTime") long startTime,
                                     @Field("messageDate") String messageDate);

    /* Nosql 에 저장된 채팅 내용 select (VOD 에서 채팅내용 불러오기 위해)
     *  - VOD 에 해당하는 roomId 로 채팅 내용 select 한다 */
    @FormUrlEncoded
    @POST("select_chat.php")
    Call<List<ChatItemVO>> select_chat(@Field("roomId") String roomId);

    /* 데이터베이스의 bookmark_list 에 insert */
    @FormUrlEncoded
    @POST("insert_bookmark.php")
    Call<ServerResponse> insert_bookmark(@Field("loginId") String loginId,
                                         @Field("BJ_ID") String BJ_ID,
                                         @Field("BJ_Pro") String BJ_Pro,
                                         @Field("login_token") String loginToken);

    /* 데이터베이스의 members 테이블에서 한 건의 사용자 정보와  */
    @FormUrlEncoded
    @POST("select_member.php")
    Call<MemberAndVod> select_member(@Field("sendUser") String sendUser,
                                     @Field("loginId") String loginId);

    /* 로그인 후에 FCM 에서 생성된 토큰을 데이터베이스의 members 테이블에 업데이트 */
    @FormUrlEncoded
    @POST("update_FCMToken.php")
    Call<ServerResponse> update_token(@Field("userId") String userId,
                                      @Field("userToken") String userToken);

    /* FCM 메세지 보내기 */
    @FormUrlEncoded
    @POST("sendFCM.php")
    Call<ServerResponse> sendFCM(@Field("vodHost_id") String vodHost_id,
                                 @Field("message") String message);

    /* 북마크를 한건 지운다 */
    @FormUrlEncoded
    @POST("delete_bookmark.php")
    Call<ServerResponse> delete_bookmark(@Field("vodHost_id") String vodHost_id,
                                         @Field("login_id") String login_id);

    /* 로그인한 사용자의 북마크 리스트를 모두 가져온다 */
    @FormUrlEncoded
    @POST("select_bookmark.php")
    Call<BookAndMember> select_bookmark(@Field("loginId") String loginId);

    /* 데이터베이스의 userVod_list 테이블에 insert 된다
     *  - userVod_list : 스트리밍 메인 화면에서 VOD 영상의 플러스 버튼을 누르면 해당 테이블에 insert 된다 */
    @FormUrlEncoded
    @POST("insert_vodList.php")
    Call<ServerResponse> insert_vodList(@Field("loginId") String loginId,
                                        @Field("hostId") String hostId,
                                        @Field("vodTitle") String vodTitle,
                                        @Field("vodPath") String vodPath,
                                        @Field("startTime") long vodTime);

    /* 데이터베이스의 userVod_list 테이블에서 사용자가 추가한 영상 목록과 방송했던 영상 목록을 모두 가져온다 */
    @FormUrlEncoded
    @POST("select_userVod.php")
    Call<UserVideoArray> select_userVod(@Field("loginId") String loginId);

    /* FragUser_main.java 에서 MY 비디오 목록의 눈모양 버튼 눌렀을때
     *  - 데이터베이스의 livestream_list 테이블의 live_set 의 공개, 비공개를 업데이트 한다 */
    @FormUrlEncoded
    @POST("update_liveset.php")
    Call<ServerResponse> update_liveset(@Field("vod_idx") int vodIdx,
                                        @Field("live_set") String liveSet);

    /* 사용자가 방송 알림을 켜고 끌때 데이터베이스의 bookmark_list 테이블 업데이트 */
    @FormUrlEncoded
    @POST("update_bookmark.php")
    Call<ServerResponse> update_bookmark(@Field("loginId") String loginId,
                                         @Field("isAlert") int isAlert);

    /* 시청자 통계를 위해 Nosql 의 tbox.chart 에 시청자 한 건 insert */
    @FormUrlEncoded
    @POST("insert_chart.php")
    Call<ServerResponse> insert_chart(@Field("chartPK") String chartPK,
                                      @Field("loginAge") String loginAge,
                                      @Field("loginGender") String loginGender,
                                      @Field("hostId") String hostId);

    /* 전달되는 월과 일에 해당되는 시청자 통계 가져오기 */
    @FormUrlEncoded
    @POST("select_chart.php")
    Call<ChartArray> select_chart(@Field("chooseDay") int date,
                                  @Field("chooseMonth") int month);
}
