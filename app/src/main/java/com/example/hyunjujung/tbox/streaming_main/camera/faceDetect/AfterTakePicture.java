package com.example.hyunjujung.tbox.streaming_main.camera.faceDetect;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.hyunjujung.tbox.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 *  [ FaceDetectActivity 화면에서 사진 찍은 후 사진을 저장하기 위한 화면 ]
 *
 *  - 사진을 저장하거나 다시 이전 화면으로 돌아가서 재촬영 할 수 있다
 *
 */

public class AfterTakePicture extends AppCompatActivity {
    private static final String TAG = "AfterTakePicture";

    ImageView captureIV;
    Intent intent;

    String faceImagePath, imageFileName;

    Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.after_take_picture);

        captureIV = (ImageView)findViewById(R.id.captureIV);

        intent = getIntent();
        faceImagePath = intent.getStringExtra("captureAddress");
        captureIV.setImageURI(Uri.parse(intent.getStringExtra("captureAddress")));
    }

    public void afterPictureOnclick(View view) {
        switch (view.getId()) {
            /* 이전 화면으로 가기 */
            case R.id.backBtn:
                DialogInterface.OnClickListener goBack = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };
                DialogInterface.OnClickListener cancel = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                };
                new AlertDialog.Builder(this)
                        .setTitle(R.string.alert)
                        .setMessage(R.string.SaveImageAlert)
                        .setPositiveButton("예", goBack)
                        .setNegativeButton("아니오", cancel)
                        .show();
                break;

            /* 갤러리에 사진 저장하기 */
            case R.id.downloadBtn:
                saveImageFile(faceImagePath);
                Toast.makeText(this, R.string.SaveImageDone, Toast.LENGTH_SHORT).show();
                finish();
                break;
        }
    }

    /* 갤러리에 사진 저장 */
    public void saveImageFile(String path) {
        Log.e(TAG, path);
        Intent mediaIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(path));
        File newfiles = new File(path);
        Uri contentUri = Uri.fromFile(newfiles);
        mediaIntent.setData(Uri.parse(path));
        sendBroadcast(mediaIntent);
    }
}
