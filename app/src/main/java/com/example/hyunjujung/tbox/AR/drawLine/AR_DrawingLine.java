package com.example.hyunjujung.tbox.AR.drawLine;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.example.hyunjujung.tbox.AR.drawLine.rendering.BackgroundRenderer;
import com.example.hyunjujung.tbox.AR.drawLine.rendering.LineShaderRenderer;
import com.example.hyunjujung.tbox.AR.drawLine.rendering.LineUtils;
import com.example.hyunjujung.tbox.AR.drawLine.rendering.PermissionHelper;
import com.example.hyunjujung.tbox.R;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.core.Frame;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

public class AR_DrawingLine extends AppCompatActivity implements GLSurfaceView.Renderer, GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener{
    private static final String TAG = AR_DrawingLine.class.getSimpleName();

    /* 레이아웃 */
    private GLSurfaceView surfaceview;
    private LinearLayout ARbottom_linear;   // 하단 5개 버튼 레이아웃
    private LinearLayout settingUI; // 설정 아이콘 누르면 나오는 상단 레이아웃
    private SeekBar lineWidthBar, distanceBar, smoothingBar;    // 상단 레이아웃 안에 SeekBar
    private ImageView colorWhite, colorBlack, colorGray, colorBrown, colorBlue, colorCyan, colorGreen, colorOrange, colorMagenta;   // 상단 레이아웃 안의 색상 이미지뷰

    private Config mDefaultConfig;
    private Session mSession;   // AR 시스템 상태를 관리하고 세션 수명주기를 처리
    private BackgroundRenderer backgroundRenderer = new BackgroundRenderer();
    private LineShaderRenderer lineShaderRenderer = new LineShaderRenderer();
    private Frame mFrame;

    private float[] projmtx = new float[16];
    private float[] viewmtx = new float[16];
    private float[] mZeroMatrix = new float[16];

    private boolean mPaused = false;

    private float mScreenWidth = 0;
    private float mScreenHeight = 0;

    private BiquadFilter biquadFilter;
    private Vector3f mLastPoint;
    private Vector3f changeColor;   // 브러쉬 색상
    private AtomicReference<Vector2f> lastTouch = new AtomicReference<>();

    private GestureDetectorCompat gestureDetector;

    private float lineWidthMax = 0.33f; // 초기 브러쉬 사이즈
    private float distanceScale = 0.0f; // 초기 거리
    private float smoothingLine = 0.1f; // 초기 Smoothing

    private float[] mLastFramePosition;

    private AtomicBoolean bIsTracking = new AtomicBoolean(true);
    private AtomicBoolean bReCenterView = new AtomicBoolean(false);
    private AtomicBoolean bTouchDown = new AtomicBoolean(false);
    private AtomicBoolean bClearDrawing = new AtomicBoolean(false);
    private AtomicBoolean bLineParameters = new AtomicBoolean(false);
    private AtomicBoolean bUndo = new AtomicBoolean(false);
    private AtomicBoolean bNewStroke = new AtomicBoolean(false);

    private ArrayList<ArrayList<Vector3f>> mStrokes;

    private DisplayRotationHelper mDisplayRotationHelper;
    private Snackbar mMessageSnackbar;

    private boolean bInstallRequested;

    private TrackingState mState;

    /* 전에 선택했던 색상 ImageView 의 아이디값 저장하는 변수
     *  - 색상 선택할때 전에 선택했던 색상의 테두리를 없애기 위해서 필요함 */
    private static View previousColor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE); // SeekBar 의 값을 저장하기 위한 Shared

        setContentView(R.layout.ar_drawing_line);

        surfaceview = (GLSurfaceView)findViewById(R.id.surfaceview);
        settingUI = (LinearLayout)findViewById(R.id.settingUI); // 설정 아이콘 클릭하면 나오는 상단 세팅 레이아웃
        ARbottom_linear = (LinearLayout)findViewById(R.id.ARbottom_linear); // 하단 5개 버튼 레이아웃

        lineWidthBar = (SeekBar)findViewById(R.id.lineWidthBar);    // 브러쉬 사이즈 Seekbar
        distanceBar = (SeekBar)findViewById(R.id.distanceBar);  // 거리 Seekbar
        smoothingBar = (SeekBar)findViewById(R.id.smoothingBar);    // Smoothing Seekbar

        lineWidthBar.setProgress(sharedPref.getInt("LineWidth", 10));
        distanceBar.setProgress(sharedPref.getInt("Distance", 1));
        smoothingBar.setProgress(sharedPref.getInt("Smoothing", 50));

        lineWidthMax = LineUtils.map((float)lineWidthBar.getProgress(), 0f, 100f, 0.1f, 5f, true);
        distanceScale = LineUtils.map((float)distanceBar.getProgress(), 0, 100, 1, 200, true);
        smoothingLine = LineUtils.map((float)smoothingBar.getProgress(), 0, 100, 0.01f, 0.2f, true);

        /* 설정 누르면 상단에 나타나는 setting 레이아웃에서
         *  - 선두께, 거리, smoothing Seekbar 리스너 */
        SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                SharedPreferences.Editor seekEdit = sharedPref.edit();

                if(seekBar == lineWidthBar) {   // 선두께 설정
                    seekEdit.putInt("LineWidth", progress);
                    lineWidthMax = LineUtils.map((float)progress, 0f, 100f, 0.1f, 5f, true);
                }else if(seekBar == distanceBar) {  // 거리 설정
                    seekEdit.putInt("Distance", progress);
                    distanceScale = LineUtils.map((float)progress, 0f, 100f, 1f, 200f, true);
                }else if(seekBar == smoothingBar) { // Smoothing 설정
                    seekEdit.putInt("Smoothing", progress);
                    smoothingLine = LineUtils.map((float)progress, 0, 100, 0.01f, 0.2f, true);
                }
                lineShaderRenderer.bNeedsUpdate.set(true);
                seekEdit.apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };

        /* 초기 선 색 지정 */
        changeColor = AppSettings.getColor();
        colorWhite = findViewById(R.id.colorWhite);
        colorWhite.setBackgroundResource(R.drawable.drawing_line_choose);
        previousColor = colorWhite;

        lineWidthBar.setOnSeekBarChangeListener(seekBarChangeListener);
        distanceBar.setOnSeekBarChangeListener(seekBarChangeListener);
        smoothingBar.setOnSeekBarChangeListener(seekBarChangeListener);

        settingUI.setVisibility(View.GONE); // 상단 Setting 레이아웃 숨기기

        mDisplayRotationHelper = new DisplayRotationHelper(this);
        Matrix.setIdentityM(mZeroMatrix, 0);

        mLastPoint = new Vector3f(0,0,0);

        bInstallRequested = false;

        /* Set up Renderer */
        surfaceview.setPreserveEGLContextOnPause(true);
        surfaceview.setEGLContextClientVersion(2);
        surfaceview.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        surfaceview.setRenderer(this);
        surfaceview.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        /* 화면 터치 이벤트 리스너 */
        gestureDetector = new GestureDetectorCompat(this, this);
        gestureDetector.setOnDoubleTapListener(this);
        mStrokes = new ArrayList<>();
    }

    /*
    *
    *   하단 버튼 클릭 이벤트
    *
    */
    public void onclickAR(View view) {
        switch (view.getId()) {

            /* 현재 화면 나가기 */
            case R.id.exitActivity_btn:
                finish();
                break;

            /* 그렸던 선 모두 지우기 */
            case R.id.deleteAll_btn:
                AlertDialog.Builder deleteBuilder = new AlertDialog.Builder(this)
                        .setMessage("모두 지우시겠습니까?");
                deleteBuilder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        bClearDrawing.set(true);
                    }
                });
                deleteBuilder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                deleteBuilder.show();
                break;

            /* 한 단계 전으로 되돌리기 */
            case R.id.return_btn:
                bUndo.set(true);
                break;

            /* 상단의 setting 레이아웃 보이기 */
            case R.id.settings_btn:
                if(settingUI.getVisibility() == View.GONE) {
                    settingUI.setVisibility(View.VISIBLE);
                    settingUI.animate().translationY(20);
                }else {
                    settingUI.animate().translationY(-20);
                    settingUI.setVisibility(View.GONE);
                }
                break;

            /* 현재 화면 녹화하기 */
            case R.id.record_btn:
                break;
        }
    }

    /*
    *
    *   상단 Setting 레이아웃에서 컬러 선택 이벤트
    *
    */
    public void changeLineColor(View view) {
        switch (view.getId()) {
            case R.id.colorWhite:   // 흰색 (초기색)
                getPreviousColor().setBackground(null); //  전에 지정했던 색상의 테두리를 없앤다
                changeColor = new Vector3f(1.0f, 1.0f, 1.0f);
                setPreviousColor(colorWhite);
                break;
            case R.id.colorBlack:   // 검은색
                getPreviousColor().setForeground(null);
                colorBlack = findViewById(R.id.colorBlack);
                changeColor = new Vector3f(0f, 0f, 0f);
                setPreviousColor(colorBlack);
                break;
            case R.id.colorGray:    // 회색
                getPreviousColor().setForeground(null);
                colorGray = findViewById(R.id.colorGray);
                changeColor = new Vector3f(0.2f, 0.2f, 0.2f);
                setPreviousColor(colorGray);
                break;
            case R.id.colorBrown:   // 갈색
                getPreviousColor().setForeground(null);
                colorBrown = findViewById(R.id.colorBrown);
                changeColor = new Vector3f(139f/255f, 69f/255f, 19f/255f);
                setPreviousColor(colorBrown);
                break;
            case R.id.colorBlue:    // 파란색
                getPreviousColor().setForeground(null);
                colorBlue = findViewById(R.id.colorBlue);
                changeColor = new Vector3f(0f, 0f, 1.0f);
                setPreviousColor(colorBlue);
                break;
            case R.id.colorCyan:    // 형광 하늘색
                getPreviousColor().setForeground(null);
                colorCyan = findViewById(R.id.colorCyan);
                changeColor = new Vector3f(0f, 1.0f, 1.0f);
                setPreviousColor(colorCyan);
                break;
            case R.id.colorGreen:   // 녹색
                getPreviousColor().setForeground(null);
                colorGreen = findViewById(R.id.colorGreen);
                changeColor = new Vector3f(0f, 1.0f, 0f);
                setPreviousColor(colorGreen);
                break;
            case R.id.colorOrange:  // 오렌지색
                getPreviousColor().setForeground(null);
                colorOrange = findViewById(R.id.colorOrange);
                changeColor = new Vector3f(251f/255f, 130f/255f, 0f);
                setPreviousColor(colorOrange);
                break;
            case R.id.colorMagenta: // 핑크색
                getPreviousColor().setForeground(null);
                colorMagenta = findViewById(R.id.colorMagenta);
                changeColor = new Vector3f(1.0f, 0f, 1.0f);
                setPreviousColor(colorMagenta);
                break;
        }
        lineShaderRenderer.bNeedsUpdate.set(true);
    }

    /* 현재 선택된 색상 외에 다른 색상은 foreground 를 null 로 지정하기 위해서
     *  - 전에 지정했던 색상의 View 를 previousColor 에 저장한다
     *  - 색상이 클릭됐을때
     *       : 선택한 색상의 테두리를 노란색으로 지정해주고,
     *       : 전에 선택한 색상의 테두리는 없앤다 */
    public View getPreviousColor() {
        return previousColor;   // 전에 지정했던 색상의 View 를 가져온다
    }
    public void setPreviousColor(View view) {
        view.setForeground(this.getResources().getDrawable(R.drawable.drawing_line_choose));    // 현재 선택한 색상의 테두리를 노란색으로 지정
        previousColor = view;   // 현재 선택한 색상의 View 를 저장한다
    }

    public void onClickDebug(View view) {
        bLineParameters.set(!bLineParameters.get());
    }

    private void addStroke(Vector2f touchPoint) {
        Vector3f newPoint = LineUtils.GetWorldCoords(touchPoint, mScreenWidth, mScreenHeight, projmtx, viewmtx);
        addStroke(newPoint);
    }

    private void addPoint(Vector2f touchPoint) {
        Vector3f newPoint = LineUtils.GetWorldCoords(touchPoint, mScreenWidth, mScreenHeight, projmtx, viewmtx);
        addPoint(newPoint);
    }

    private void addStroke(Vector3f newPoint) {
        biquadFilter = new BiquadFilter(smoothingLine);
        for (int i = 0; i < 1500; i++) {
            biquadFilter.update(newPoint);
        }
        Vector3f p = biquadFilter.update(newPoint);
        mLastPoint = new Vector3f(p);
        mStrokes.add(new ArrayList<Vector3f>());
        mStrokes.get(mStrokes.size() - 1).add(mLastPoint);
    }

    private void addPoint(Vector3f newPoint) {
        if (LineUtils.distanceCheck(newPoint, mLastPoint)) {
            Vector3f p = biquadFilter.update(newPoint);
            mLastPoint = new Vector3f(p);
            mStrokes.get(mStrokes.size() - 1).add(mLastPoint);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(mSession == null) {
            try{
                switch (ArCoreApk.getInstance().requestInstall(this, !bInstallRequested)) {
                    case INSTALL_REQUESTED:
                        bInstallRequested = true;
                        return;
                    case INSTALLED:
                        break;
                }

                if(!PermissionHelper.hasCameraPermission(this)) {
                    PermissionHelper.requestCameraPermission(this);
                    return;
                }

                mSession = new Session(this);
            }catch (Exception e) {
                e.printStackTrace();
            }

            Config config = new Config(mSession);
            if(!mSession.isSupported(config)) {
                Log.e(TAG, "ARCore 지원하지 않는 기계");
            }

            mSession.configure(config); // 세션을 구성
       }

        try {
            mSession.resume();
            surfaceview.onResume();
            mDisplayRotationHelper.onResume();
            mPaused = false;
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(mSession != null) {
            mDisplayRotationHelper.onPause();
            surfaceview.onPause();
            mSession.pause();
        }

        mPaused = false;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        mScreenHeight = displayMetrics.heightPixels;
        mScreenWidth = displayMetrics.widthPixels;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(!PermissionHelper.hasCameraPermission(this)) {
            Toast.makeText(this, "카메라 권한이 필요합니다", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    /* GLSurfaceView Override */
    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        if(mSession == null) {
            return;
        }

        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

        backgroundRenderer.createOnGlThread(this);
        try {

            mSession.setCameraTextureName(backgroundRenderer.getTextureId());
            lineShaderRenderer.createOnGlThread(this);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES20.glViewport(0,0, width, height);
        mDisplayRotationHelper.onSurfaceChanged(width, height);
        mScreenWidth = width;
        mScreenHeight = height;
    }
    @Override
    public void onDrawFrame(GL10 gl10) {
        if(mPaused) {
            return;
        }
        update();

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        if(mFrame == null) {
            return;
        }

        backgroundRenderer.draw(mFrame);    // Draw Background

        if(mFrame.getCamera().getTrackingState() == TrackingState.TRACKING) {   // Draw Lines
            lineShaderRenderer.draw(viewmtx, projmtx, mScreenWidth, mScreenHeight, AppSettings.getNearClip(), AppSettings.getFarClip());
        }
    }

    private void update() {
        if(mSession == null) {
            return;
        }

        mDisplayRotationHelper.updateSessionIfNeeded(mSession);

        try {
            mSession.setCameraTextureName(backgroundRenderer.getTextureId());

            mFrame = mSession.update();
            Camera camera = mFrame.getCamera();
            mState = camera.getTrackingState();

            /* Update tracking states */
            if(mState == TrackingState.TRACKING && !bIsTracking.get()) {
                bIsTracking.set(true);
            }else if(mState == TrackingState.STOPPED && bIsTracking.get()) {
                bIsTracking.set(false);
                bTouchDown.set(false);
            }

            /* Get Projection matrix */
            camera.getProjectionMatrix(projmtx, 0, AppSettings.getNearClip(), AppSettings.getFarClip());
            camera.getViewMatrix(viewmtx, 0);
            float[] position = new float[3];
            camera.getPose().getTranslation(position, 0);

            // Check if camera has moved much, if thats the case, stop touchDown events
            // (stop drawing lines abruptly through the air)
            if (mLastFramePosition != null) {
                Vector3f distance = new Vector3f(position[0], position[1], position[2]);
                distance.sub(new Vector3f(mLastFramePosition[0], mLastFramePosition[1], mLastFramePosition[2]));

                if (distance.length() > 0.15) {
                    bTouchDown.set(false);
                }
            }
            mLastFramePosition = position;

            // Multiply the zero matrix
            Matrix.multiplyMM(viewmtx, 0, viewmtx, 0, mZeroMatrix, 0);

            if (bNewStroke.get()) {
                bNewStroke.set(false);
                addStroke(lastTouch.get());
                lineShaderRenderer.bNeedsUpdate.set(true);
            } else if (bTouchDown.get()) {
                addPoint(lastTouch.get());
                lineShaderRenderer.bNeedsUpdate.set(true);
            }

            if (bReCenterView.get()) {
                bReCenterView.set(false);
                mZeroMatrix = getCalibrationMatrix();
            }

            if (bClearDrawing.get()) {
                bClearDrawing.set(false);
                clearDrawing();
                lineShaderRenderer.bNeedsUpdate.set(true);
            }

            if (bUndo.get()) {
                bUndo.set(false);
                if (mStrokes.size() > 0) {
                    mStrokes.remove(mStrokes.size() - 1);
                    lineShaderRenderer.bNeedsUpdate.set(true);
                }
            }
            lineShaderRenderer.setDrawDebug(bLineParameters.get());
            if (lineShaderRenderer.bNeedsUpdate.get()) {
                lineShaderRenderer.setColor(changeColor);
                lineShaderRenderer.mDrawDistance = AppSettings.getStrokeDrawDistance();
                lineShaderRenderer.setDistanceScale(distanceScale);
                lineShaderRenderer.setLineWidth(lineWidthMax);
                lineShaderRenderer.clear();
                lineShaderRenderer.updateStrokes(mStrokes);
                lineShaderRenderer.upload();
            }

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public float[] getCalibrationMatrix() {
        float[] t = new float[3];
        float[] m = new float[16];

        mFrame.getCamera().getPose().getTranslation(t, 0);
        float[] z = mFrame.getCamera().getPose().getZAxis();
        Vector3f zAxis = new Vector3f(z[0], z[1], z[2]);
        zAxis.y = 0;
        zAxis.normalize();

        double rotate = Math.atan2(zAxis.x, zAxis.z);

        Matrix.setIdentityM(m, 0);
        Matrix.translateM(m, 0, t[0], t[1], t[2]);
        Matrix.rotateM(m, 0, (float) Math.toDegrees(rotate), 0, 1, 0);
        return m;
    }

    public void clearDrawing() {
        mStrokes.clear();
        lineShaderRenderer.clear();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.gestureDetector.onTouchEvent(event);

        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            lastTouch.set(new Vector2f(event.getX(), event.getY()));
            bTouchDown.set(true);
            bNewStroke.set(true);
            return true;
        }else if(event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_POINTER_DOWN) {
            lastTouch.set(new Vector2f(event.getX(), event.getY()));
            bTouchDown.set(true);
            return true;
        }else if(event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
            bTouchDown.set(false);
            lastTouch.set(new Vector2f(event.getX(), event.getY()));
            return true;
        }
        return super.onTouchEvent(event);
    }

    /* GestureDetector.OnGestureListener Override */
    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }
    @Override
    public void onShowPress(MotionEvent motionEvent) {
    }
    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }
    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }
    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }
    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }


    /* GestureDetector.OnDoubleTapListener Override */
    @Override
    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        return false;
    }
    @Override
    public boolean onDoubleTap(MotionEvent motionEvent) {   // 화면을 더블클릭하면 하단 버튼을 숨기거나 나타나게 함
        if(ARbottom_linear.getVisibility() == View.GONE) {
            ARbottom_linear.setVisibility(View.VISIBLE);
        }else {
            ARbottom_linear.setVisibility(View.GONE);
        }
        return false;
    }
    @Override
    public boolean onDoubleTapEvent(MotionEvent motionEvent) {
        return false;
    }
}
