package com.example.hyunjujung.tbox.login_join_activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.example.hyunjujung.tbox.R;
import com.google.android.exoplayer2.util.Util;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *  [ 프로필 이미지 필터 설정하는 화면 ]
 */

public class ProfileFilter extends AppCompatActivity {

    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("native-lib");
    }

    ImageView change_filter, original_image, gray_filter, sketch_filter, reverse_filter;

    /* OpenCV */
    private Mat img_input;
    private Mat imgOut_gray, imgOut_sketch, imgOut_reverse;

    Bitmap bitmapOut_gray, bitmapOut_sketch, bitmapOut_reverse, saveBitmap;

    /* 회원가입 화면에서 넘어온 기존 이미지 Uri 저장 변수 */
    Uri originUri;

    /* 바뀐 이미지 저장하는 변수 */
    String filterImage, fileName;

    Intent joinIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_filter);

        change_filter = (ImageView)findViewById(R.id.change_filter);
        original_image = (ImageView)findViewById(R.id.original_image);
        gray_filter = (ImageView)findViewById(R.id.gray_filter);
        sketch_filter = (ImageView)findViewById(R.id.sketch_filter);
        reverse_filter = (ImageView)findViewById(R.id.reverse_filter);

        joinIntent = getIntent();
        /* Join 액티비티에서 필터 적용하기 위해 넘겨준 오리지널 이미지 Uri */
        originUri = Uri.parse(joinIntent.getStringExtra("filterImage"));
        filterImage = joinIntent.getStringExtra("filterImage");

        /* 필터 클릭할때마다 바뀌는 상단에 위치한 이미지뷰 */
        change_filter.setImageURI(originUri);

        /* 필터 목록에서 가장 왼쪽에 있는 원본 이미지뷰 */
        original_image.setImageURI(originUri);
    }

    @Override
    protected void onResume() {
        super.onResume();

        read_image();
        read_reverse();
        showFilterImage();
    }

    private void read_image() {
        img_input = new Mat();
        imgOut_gray = new Mat();
        imgOut_sketch = new Mat();

        loadImage(joinIntent.getStringExtra("filterImage"), img_input.getNativeObjAddr());
    }

    private void showFilterImage() {
        imageProcessing(img_input.getNativeObjAddr(),
                        imgOut_gray.getNativeObjAddr(),
                        imgOut_sketch.getNativeObjAddr());

        reverseProcessing(img_input.getNativeObjAddr(), imgOut_reverse.getNativeObjAddr());

        /* 필터 효과 적용한 이미지를 이미지뷰에 출력 */
        /* Gray */
        bitmapOut_gray = Bitmap.createBitmap(imgOut_gray.cols(), imgOut_gray.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(imgOut_gray, bitmapOut_gray);
        gray_filter.setImageBitmap(bitmapOut_gray);

        /* Sketch */
        bitmapOut_sketch = Bitmap.createBitmap(imgOut_reverse.cols(), imgOut_reverse.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(imgOut_sketch, bitmapOut_sketch);
        sketch_filter.setImageBitmap(bitmapOut_sketch);

        /* Reverse */
        bitmapOut_reverse = Bitmap.createBitmap(imgOut_reverse.cols(), imgOut_reverse.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(imgOut_reverse, bitmapOut_reverse);
        reverse_filter.setImageBitmap(bitmapOut_reverse);
    }

    private void read_reverse() {
        img_input = new Mat();
        imgOut_reverse = new Mat();

        loadImageSketch(joinIntent.getStringExtra("filterImage"), img_input.getNativeObjAddr());
    }

    /* 이미지 필터 메소드 (jni) */
    public native void loadImage(String imageFileName,
                                 long img);
    public native void imageProcessing(long inputImage,
                                       long outputGray,
                                       long outputSketch);

    /* 반전 이미지 효과 적용하는 메소드는 따로 구현 */
    public native void loadImageSketch(String imageFileName, long imgs);
    public native int reverseProcessing(long inputimages, long outreverse);

    /* 이미지 필터 적용 버튼 */
    public void changeImage(View view) {
        int id = view.getId();
        switch (id) {
            /* 필터 적용 취소하고 원본 이미지 보이기 */
            case R.id.original_image:
                change_filter.setImageURI(originUri);
                filterImage = joinIntent.getStringExtra("filterImage");
                saveBitmap = null;
                break;

            /* Gray 필터 적용 */
            case R.id.gray_filter:
                change_filter.setImageBitmap(bitmapOut_gray);
                saveBitmap = bitmapOut_gray;
                break;

            /* Sketch 필터 적용 */
            case R.id.sketch_filter:
                change_filter.setImageBitmap(bitmapOut_sketch);
                saveBitmap = bitmapOut_sketch;
                break;

            /* Reverse 필터 적용 */
            case R.id.reverse_filter:
                change_filter.setImageBitmap(bitmapOut_reverse);
                saveBitmap = bitmapOut_reverse;
                break;
        }
    }

    /* 필터 적용 완료 or 취소 */
    public void filter_onclick(View view) {
        int id = view.getId();
        switch (id) {

            /* 필터 적용 취소하면 원본 이미지 Uri 다시 가지고 회원가입 창으로 이동 */
            case R.id.filter_back:
                DialogInterface.OnClickListener cancelFilter = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent cancelIntent = new Intent(getApplicationContext(), Join.class);
                        cancelIntent.putExtra("filterImage", joinIntent.getStringExtra("filterImage"));
                        setResult(RESULT_OK, cancelIntent);
                        finish();
                    }
                };
                DialogInterface.OnClickListener noCancel = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                };
                new AlertDialog.Builder(this)
                        .setTitle("알림")
                        .setMessage("필터 적용을 취소하시겠습니까?")
                        .setNegativeButton("아니오", noCancel)
                        .setPositiveButton("예", cancelFilter)
                        .show();
                break;

            /* 필터 적용 완료 후 회원가입 창으로 이동
             *  - 원본 이미지일땐 saveBitmap 이 null 이다
             *  - 필터 적용한 이미지일때는 saveBitmap에 필터가 적용된 bitmap 값이 들어있다 */
            case R.id.filter_done:


                if(saveBitmap != null) {
                    Intent filterIntent = new Intent(this, Join.class);
                    filterIntent.putExtra("filterImage", saveFilterImage(saveBitmap));
                    filterIntent.putExtra("fileName", fileName);
                    setResult(RESULT_OK, filterIntent);
                    finish();
                }
                break;
        }
    }

    /* 필터링 된 이미지 임시 저장 */
    public String saveFilterImage(Bitmap outBitmap) {
        String tempFilterImages;
        File tempStorage = getApplicationContext().getCacheDir();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        fileName = "temp_" + timeStamp + ".jpg";
        File cardfile = new File(tempStorage, fileName);
        try {
            cardfile.createNewFile();
            FileOutputStream out = new FileOutputStream(cardfile);
            outBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
        }catch (Exception e) {
            e.printStackTrace();
        }
        tempFilterImages = cardfile.getAbsolutePath();
        filterImage = cardfile.getAbsolutePath();
        return tempFilterImages;
    }


}
