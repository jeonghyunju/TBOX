package com.example.hyunjujung.tbox.login_join_activity;

import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.hyunjujung.tbox.R;
import com.example.hyunjujung.tbox.data_vo.ServerResponse;
import com.example.hyunjujung.tbox.retrofit.Retrofit_ApiConfig;
import com.example.hyunjujung.tbox.retrofit.Retrofit_Creator;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 *  [ 회원가입을 위한 액티비티 ]
 *
 *  - 상단 좌측에 있는 화살표를 누르면 회원가입을 취소하고 로그인 화면으로 이동한다
 *  - 회원가입 시에 입력 받는 데이터
 *      --> 필수 데이터(null 이면 안되는 데이터) : 이메일, 비밀번호
 *      --> 그외 데이터 : 사용자 프로필, 연령대, 성별
 *
 */

public class Join extends AppCompatActivity {
    Toolbar join_toolbar;
    CircleImageView join_user_profile;
    EditText join_user_email, join_user_password, join_user_confirmPW;
    RadioGroup join_user_radiogroup;
    Spinner join_age_spinner;

    /* startActivityForResult 리퀘스트 코드 */
    static final int PICK_CAMERA = 0;
    static final int PICK_ALBUM = 1;
    static final int CROP_IAMGE = 2;
    static final int SET_FILTER = 3;

    /* 카메라로 찍어서 크롭한 uri 와 앨범에서 가져와서 크롭한 uri */
    Uri cameraUri, albumUri;

    /* 카메라로 찍어서 크롭한 경로와 앨범에서 가져와서 크롭한 경로를 다르게 지정해주기 위해 선언해주는 변수 */
    Boolean isAlbum = false;

    static String imageFileName = "";

    /* 이미지의 현재 경로 */
    static String photoPath = "";

    //SetProfile setProfile = new SetProfile();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.join);

        /* 상단 툴바 */
        join_toolbar = (Toolbar)findViewById(R.id.join_toolbar);

        /* 회원가입에 필요한 데이터 */
        join_user_profile = (CircleImageView)findViewById(R.id.join_user_profile);
        join_user_email = (EditText)findViewById(R.id.join_user_email);
        join_user_password = (EditText)findViewById(R.id.join_user_password);
        join_user_confirmPW = (EditText)findViewById(R.id.join_user_confirmPW);
        join_user_radiogroup = (RadioGroup)findViewById(R.id.join_user_radiogroup);

        /* 연령대 스피너 */
        join_age_spinner = (Spinner)findViewById(R.id.join_age_spinner);
        ArrayAdapter<CharSequence> ageSpinnerAdap = ArrayAdapter.createFromResource(this, R.array.ageGroup, android.R.layout.simple_spinner_item);
        ageSpinnerAdap.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        join_age_spinner.setAdapter(ageSpinnerAdap);

        /* 입력한 비밀번호와 비밀번화 확인란이 일치하는지 검사한다 */
        join_user_confirmPW.addTextChangedListener(new TextWatcher() {
            /* EditText 밑줄색 변경하기 위한 색상 저장 변수 */
            int etColor_incor = Color.parseColor("#B80028");
            int etColor_correct = Color.parseColor("#82c12f");

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.toString().equals(join_user_password.getText().toString())) {
                    join_user_confirmPW.getBackground().setColorFilter(etColor_correct, PorterDuff.Mode.SRC_IN);
                }else {
                    join_user_confirmPW.getBackground().setColorFilter(etColor_incor, PorterDuff.Mode.SRC_IN);
                }
            }
        });
    }

    /**
     *  [ 회원가입 화면에 있는 버튼 클릭 이벤트 모음 메소드 ]
     *
     */
    public void join_onclick(View view) {
        int id = view.getId();
        switch (id) {
            /* 좌측 상단의 뒤로가기 버튼
             * 회원가입을 취소하고 다시 로그인 화면으로 돌아간다 */
            case R.id.join_toolbar_back_btn:
                finish();
                break;

            /* 회원가입 완료 버튼
             * 회원가입 완료 후에 DB 에 저장 후 로그인 화면으로 돌아간다 */
            case R.id.join_done_btn:

                /* 라디오 버튼과 스피너에서 선택된 유저 값 저장 */
                RadioButton join_select_gender = (RadioButton)findViewById(join_user_radiogroup.getCheckedRadioButtonId());
                String select_gender = join_select_gender.getText().toString();
                String select_age = join_age_spinner.getSelectedItem().toString();

                /* Retrofit 사용하여 데이터베이스 (members 테이블) 에 사용자 저장 */
                Retrofit_ApiConfig apiConfig = Retrofit_Creator.getApiConfig();

                /* 데이터베이스에 저장하기 위해 사용자에게서 입력받은 회원가입 정보를 Map 에 담는다 */
                Map<String, RequestBody> joinMaps = new HashMap<>();
                joinMaps.put("user_email", toRequestBody(join_user_email.getText().toString()));
                joinMaps.put("user_pass", toRequestBody(join_user_password.getText().toString()));
                joinMaps.put("user_gender", toRequestBody(select_gender));
                joinMaps.put("user_age", toRequestBody(select_age));

                /* 사용자가 프로필 사진을 설정했을때 이미지를 multipart 로 전송한다 */
                //File user_profile_path = new File(pabhotopath);
                //RequestBody image_body = RequestBody.create(MediaType.parse("image/*"), user_profile_path);
                //joinMaps.put("uploaded_file\"; filename=\"" + imageFileName + "\"", fileBody);

                /* insert_member.php 로 가서 입력받은 데이터를 저장한 후 성공적으로 저장되었으면 success 리턴 */
                Call<ServerResponse> responseCall = apiConfig.insert_Members(joinMaps);
                responseCall.enqueue(new Callback<ServerResponse>() {
                    @Override
                    public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                        ServerResponse serverResponse = response.body();
                        Log.e("데이터베이스에 저장 완료", serverResponse.getMessage());
                    }

                    @Override
                    public void onFailure(Call<ServerResponse> call, Throwable t) {
                        Log.e("실패", t.toString());
                    }
                });

                Toast.makeText(this, "회원가입이 완료되었습니다", Toast.LENGTH_SHORT).show();

                /* 데이터베이스에 저장 완료 후에 다시 로그인 화면으로 돌아간다 */
                Intent go_login = new Intent(this, ToysLogin.class);
                go_login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(go_login);
                finish();
                break;

            /* 회원가입 시에 단말기의 카메라나 앨범을 사용해서 프로필을 지정한다
             *  - cameraListener : 사진 촬영
             *  - albumListener : 앨범에서 선택
             *  - cancelListener : 취소 */
            case R.id.join_user_profile:
                DialogInterface.OnClickListener cameraListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        takePicture();
                    }
                };
                DialogInterface.OnClickListener albumListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        takeAlbum();
                    }
                };
                DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                };
                new AlertDialog.Builder(this)
                        .setTitle("이미지를 선택하세요")
                        .setPositiveButton("카메라", cameraListener)
                        .setNegativeButton("앨범", albumListener)
                        .setNeutralButton("취소", cancelListener)
                        .show();

                break;

            /* 회원가입 시에 지정한 프로필에 필터 적용하는 액티비티로 넘어간다 */
            case R.id.join_profile_filter:
                Toast.makeText(this, "필터 설정", Toast.LENGTH_SHORT).show();
                Intent filterIntent = new Intent(this, ProfileFilter.class);
                filterIntent.putExtra("filterImage", photoPath);
                startActivityForResult(filterIntent, SET_FILTER);
                break;
        }
    }

    /**
     *  [ HTTP Body 에 데이터를 실어 보내기 위해 text 인걸 알려주는 메소드 ]
     */
    public static RequestBody toRequestBody(String value) {
        RequestBody body = RequestBody.create(MediaType.parse("text/plain"), value);
        return body;
    }

    /**
     *  [ 프로필 설정 메소드들 ]
     *
     *  - takePicture() : 카메라로 사진 찍어 크롭하는 메소드
     *  - takeAlbum() : 앨범에서 사진 선택하는 메소드
     *  - createImagefile() : 이미지 크롭한 후에 저장할 파일 위치 생성하는 메소드
     *  - cropImage() : 이미지 크롭하는 메소드
     *  - galleryAddPic() : 앨범에 크롭한 사진을 업로드한다
     *
     */
    public Uri takePicture() {
        String state = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state)) {                               //  외장메모리 검사
            Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);       //  인텐트로 카메라 호출
            if(takePicture.resolveActivity(getPackageManager()) != null) {
                File photoFile = null;                                              //  사진 찍은 후 저장할 임시파일 선언
                try {
                    photoFile = createImagefile();
                }catch (Exception e) {
                    Toast.makeText(this, "파일 생성 실패", Toast.LENGTH_SHORT).show();
                }
                //  폴더가 생성되어 있다면 그 폴더에 사진을 저장한다
                if(photoFile != null) {
                    //  getUriForFile의 두번째 인자는 Manifest의 provider에서 authorities의 경로와 같아야한다
                    cameraUri = FileProvider.getUriForFile(this, "com.example.hyunjujung.tbox.provider", photoFile);
                    takePicture.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);

                    startActivityForResult(takePicture, PICK_CAMERA);
                }
            }
        }else {
            Toast.makeText(this, "저장공간에 접근이 불가능한 기기입니다", Toast.LENGTH_SHORT).show();
        }

        return cameraUri;
    }

    /* 앨범에서 프로필로 설정할 사진을 선택하는 메소드 */
    public void takeAlbum() {
        Intent albumIntent = new Intent(Intent.ACTION_PICK);
        albumIntent.setType("image/*");
        albumIntent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(albumIntent, PICK_ALBUM);
    }

    /* 카메라로 찍거나 앨범에서 사진을 선택하고 크롭한 후에 저장할 파일 위치를 생성하는 메소드 */
    public File createImagefile() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        imageFileName = "temp_" + timestamp + ".jpg";
        File imageFiles = null;
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/Pictures", "TBOX");
        if(!storageDir.exists()) {
            storageDir.mkdir();
        }
        imageFiles = new File(storageDir, imageFileName);
        photoPath = imageFiles.getAbsolutePath();
        return imageFiles;
    }

    /* 이미지를 크롭하는 메소드 */
    public void cropImage() {
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        cropIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        cropIntent.setDataAndType(cameraUri, "image/*");
        cropIntent.putExtra("scale", true);
        if(isAlbum == false) {
            cropIntent.putExtra("output", cameraUri);
        }else {
            cropIntent.putExtra("output", albumUri);
        }
        startActivityForResult(cropIntent, CROP_IAMGE);
    }

    /* 앨범에 크롭한 사진을 업로드하는 메소드 */
    public void galleryAddPic() {
        Intent mediaIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File newfiles = new File(photoPath);
        Uri contentUri = Uri.fromFile(newfiles);
        mediaIntent.setData(contentUri);
        sendBroadcast(mediaIntent);
        Toast.makeText(this, "프로필 사진이 등록되었습니다", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != RESULT_OK) {
            Log.e("onActivityResult", "RESULT_NOT_OK");
        }else {
            switch (requestCode) {

                /* 앨범에서 사진 가져오기 */
                case PICK_ALBUM:
                    isAlbum = true;
                    File albumFile = null;
                    try {
                        albumFile = createImagefile();
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                    if(albumFile != null) {
                        albumUri = Uri.fromFile(albumFile);
                    }
                    cameraUri = data.getData();
                    cropImage();

                    break;

                /* 카메라로 사진촬영 후 프로필 설정 */
                case PICK_CAMERA:
                    isAlbum = false;
                    cropImage();
                    break;

                /* 사진 크롭하여 이미지뷰에 넣기
                 *  - isAlbum == false : 카메라로 사진찍었을 경우
                 *  - isAlbum == true : 앨범에서 사진 선택했을 경우 */
                case CROP_IAMGE:
                    galleryAddPic();
                    if(isAlbum == false) {
                        join_user_profile.setImageURI(cameraUri);
                    }else {
                        join_user_profile.setImageURI(albumUri);
                    }
                    break;

                /* 이미지 필터 적용된 이미지를 join_user_profile 에 넣어준다 */
                case SET_FILTER:
                    join_user_profile.setImageURI(Uri.parse(data.getStringExtra("filterImage")));

                    /* 임시 저장된 파일 경로를 가져온다 */
                    photoPath = data.getStringExtra("filterImage");
                    if(data.getStringExtra("fileName") != null) {
                        imageFileName = data.getStringExtra("fileName");
                    }
                    break;
            }
        }
    }



}
