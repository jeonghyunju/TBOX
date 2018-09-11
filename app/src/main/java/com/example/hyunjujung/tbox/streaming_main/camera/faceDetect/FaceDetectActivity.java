package com.example.hyunjujung.tbox.streaming_main.camera.faceDetect;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.example.hyunjujung.tbox.R;
import com.example.hyunjujung.tbox.adapter.faceDetect.EmoticonAdapter;
import com.example.hyunjujung.tbox.data_vo.faceDetect.EmoticonArray;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 *  [ 얼굴 인식 하는 카메라 화면 ]
 *
 *  - 전면이나 후면 카메라로 얼굴을 인식한다
 *  - 인식 된 얼굴에 마스크를 씌운다
 */

public class FaceDetectActivity extends AppCompatActivity{
    private static final String TAG = "FaceDetectActivity";
    private CameraSource mCameraSource = null;

    private CameraPreview mPreview; //  카메라 영상 보여주는 View
    private GraphicOverlay mGraphicOverlay; //  마스크가 그려질 View
    private FrameLayout cameraFrame;    //  사진 찍기 위해서 화면 캡처할 View

    private static final int RC_HANDLE_GMS = 9001;  //  카메라 권한 변수
    private static final int RC_HANDLE_CAMERA_PERM = 2; //  카메라 권한 변수
    private boolean mIsFrontFacing = true;

    static FaceGraphic faceGraphic = null;  //  실제 마스크 그래픽
    EmoticonArray emoticonVO = new EmoticonArray(); // 마스크 이미지 모두 담겨있는 클래스

    /* 하단 마스크 RecyclerView */
    RecyclerView emoticonRecycle;
    EmoticonAdapter emoticonAdapter;
    ArrayList<Integer> emoticonArray = new ArrayList<>();

    /* 하단 버튼 레이아웃 */
    RelativeLayout bottomBtn;

    String fileName, absoluteCapturePath = "";   //  찍은 사진 이름과, 절대경로 저장할 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.face_detect);

        cameraFrame = (FrameLayout)findViewById(R.id.cameraFrame);
        bottomBtn = (RelativeLayout)findViewById(R.id.bottomBtn);
        mPreview = (CameraPreview)findViewById(R.id.cameraPreview);
        mGraphicOverlay = (GraphicOverlay)findViewById(R.id.graphicOverlay);
        emoticonRecycle = (RecyclerView)findViewById(R.id.emoticonRecycle);

        emoticonArray = emoticonVO.setEmoticon();   //  마스크 이미지 모두 담겨있음

        if(savedInstanceState != null) {
            /* 카메라 화면 전환을 위한 변수 */
            mIsFrontFacing = savedInstanceState.getBoolean("IsFrontFacing");
        }

        /* RecyclerView */
        emoticonAdapter = new EmoticonAdapter(this, emoticonArray);
        emoticonRecycle.setAdapter(emoticonAdapter);

        /* 카메라 권한 확인후 카메라 리소스 Create */
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource();
        } else {
            requestCameraPermission();
        }

        faceGraphic = new FaceGraphic(mGraphicOverlay, this.getApplicationContext());

    }

    CameraSource.ShutterCallback shutterCallback = new CameraSource.ShutterCallback() {
        @Override
        public void onShutter() {
            Log.d(TAG, "onShutter Callback");
        }
    };

    CameraSource.PictureCallback jpegCallback = new CameraSource.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] bytes) {
            takeCapture(bytes);
        }
    };

    /* 버튼 클릭 이벤트 */
    public void faceDetectOnclick(View view) {
        switch (view.getId()) {
            /* 카메라 전환 */
            case R.id.switchCamera:
                mIsFrontFacing = !mIsFrontFacing;
                if(mCameraSource != null) {
                    mCameraSource.release();
                    mCameraSource = null;
                }
                createCameraSource();
                startCameraSource();
                break;

            /* 현재 화면 나가기 */
            case R.id.exitActivity:
                finish();
                break;

            /* 사진 찍기 */
            case R.id.takePicture:
                mCameraSource.takePicture(shutterCallback, jpegCallback);
                break;

            /* 마스크 선택 */
            case R.id.selectEmoticon:
                if(emoticonRecycle.getVisibility() == View.GONE) {
                    emoticonRecycle.setVisibility(View.VISIBLE);
                }else {
                    emoticonRecycle.setVisibility(View.GONE);
                }
                break;
        }
    }

    /* 인식 된 얼굴에 마스크를 씌우기 위한 메소드
     *  - RecyclerView 에서 클릭 된 마스크의 ResourceID 를 받아서
     *  - 실제 마스크가 그려지는 FaceGraphic 클래스에 넣어준다 */
    public void setMask(int resourceId) {
        faceGraphic = new FaceGraphic(mGraphicOverlay, this.getApplicationContext());
        faceGraphic.setMask(true);
        faceGraphic.setResourceId(resourceId);
        faceGraphic.draw(new Canvas());
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mCameraSource != null) {
            mCameraSource.release();
            faceGraphic.setMask(false);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putBoolean("InFrontFacing", mIsFrontFacing);
    }

    /*
    *
    *   카메라 권한 관련 메소드
    *
    */
    private void requestCameraPermission() {
        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(mGraphicOverlay, R.string.CameraPermissionScript,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.PermissionOK, listener)
                .show();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            createCameraSource();
            return;
        }

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.alert)
                .setMessage(R.string.CameraPermissionScript)
                .setPositiveButton(R.string.PermissionOK, listener)
                .show();
    }

    /*
    *
    *   카메라 소스 : 카메라 전면, 후면 전환될 때 카메라 소스를 초기화한다
    *
    */
    private void createCameraSource() {
        Context context = getApplicationContext();
        FaceDetector detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        detector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory(this.getApplicationContext()))
                        .build());

        /* 카메라 전면, 후면 전환 하기 위해서 */
        int facing = CameraSource.CAMERA_FACING_FRONT;
        if(!mIsFrontFacing) {
            facing = CameraSource.CAMERA_FACING_BACK;
        }

        if (!detector.isOperational()) {
            Log.w(TAG, "얼굴인식 라이브러리 버전을 사용할 수 없습니다");
        }

        mCameraSource = new CameraSource.Builder(context, detector)
                .setRequestedPreviewSize(320, 240)
                .setFacing(facing)
                .setRequestedFps(30.0f)
                .build();
    }
    private void startCameraSource() {
        /* 실제 카메라 영상을 화면에 보여준다 */
        Log.e(TAG, "카메라 프리뷰 스타트");

        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg = GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                /* 카메라 영상을 보여주고, 마스크를 씌울 그래픽 뷰도 올려준다 */
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    /*
    *
    *   Face Detector : 화면의 얼굴을 감지한다
    *
    */
    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        Context context;

        public GraphicFaceTrackerFactory(Context context) {
            this.context = context;
        }

        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker(mGraphicOverlay, context, mIsFrontFacing);    //  새로운 얼굴이 탐지되면 불려지는 메소드
        }
    }

    /* 화면 내의 얼굴이 여러개 있을때 감지하기 위한 클래스 */
    private class GraphicFaceTracker extends Tracker<Face> {
        //  얼굴이 탐지될때 불려진다
        private GraphicOverlay mOverlay;    //  화면에 마스크를 그리기위한 View
        private FaceGraphic mFaceGraphic;   //  실제 마스크 그래픽
        private Context mContext;
        private boolean mIsFrontFacing;

        GraphicFaceTracker(GraphicOverlay overlay, Context context, boolean isFrontFacing) {
            mOverlay = overlay;
            mContext = context;
            mIsFrontFacing = isFrontFacing;
            mFaceGraphic = new FaceGraphic(overlay, mContext);
        }

        @Override
        public void onNewItem(int faceId, Face item) {
            mFaceGraphic.setId(faceId);
        }

        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            mOverlay.add(mFaceGraphic);
            mFaceGraphic.updateFace(face);
        }

        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            mOverlay.remove(mFaceGraphic);  //  화면에서 얼굴이 보이지 않으면 마스크를 지운다
        }

        @Override
        public void onDone() {
            mOverlay.remove(mFaceGraphic);
        }
    }

    /* 사진 찍기 메소드 */
    public void takeCapture(byte[] bytes) {
        cameraFrame.setDrawingCacheEnabled(true);
        cameraFrame.buildDrawingCache();    //  캡처할 뷰를 지정하여 기기의 캐쉬에 쓴다
        Bitmap captureView = cameraFrame.getDrawingCache();
        Bitmap cameraBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);    //  CameraSource 에서 영상 프레임의 ByteArray 를 받아 Bitmap 으로 바꿔준다
        Bitmap overlayBitmap = Bitmap.createBitmap(cameraBitmap.getWidth(), cameraBitmap.getHeight(), cameraBitmap.getConfig());
        Canvas canvas = new Canvas(overlayBitmap);

        /* 캡처 이미지를 화면 사이즈에 맞추기 위한 가로, 세로 변수 */
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        Bitmap captureResize = Bitmap.createScaledBitmap(captureView, width, height, true);
        Bitmap cameraResize = Bitmap.createScaledBitmap(cameraBitmap, width, height, true);

        canvas.drawBitmap(cameraResize, 0, 0, null);
        canvas.drawBitmap(captureResize, 0, 0, null);

        saveImage(overlayBitmap);
        cameraFrame.setDrawingCacheEnabled(false);
    }

    /* 캡처된 이미지 임시저장
     *  - 임시 저장한 후 저장된 절대 경로를 가지고 AfterTakePicture 액티비티로 넘어간다
     *  - AfterTakePicture 액티비티에서 사진을 갤러리에 저장함 */
    public void saveImage(Bitmap saveBit) {
        File tempStorage = getApplicationContext().getCacheDir();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        fileName = "temp_" + timeStamp + ".jpg";
        File cardFile = new File(tempStorage, fileName);
        try{
            cardFile.createNewFile();
            FileOutputStream out = new FileOutputStream(cardFile);
            saveBit.compress(Bitmap.CompressFormat.JPEG, 100, out);
        }catch (Exception e) {
            e.printStackTrace();
        }
        absoluteCapturePath = cardFile.getAbsolutePath();
        Intent goAfterPicture = new Intent(this, AfterTakePicture.class);
        goAfterPicture.putExtra("captureAddress", absoluteCapturePath);
        startActivity(goAfterPicture);
    }

}
