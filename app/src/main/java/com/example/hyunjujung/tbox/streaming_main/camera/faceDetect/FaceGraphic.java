package com.example.hyunjujung.tbox.streaming_main.camera.faceDetect;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.example.hyunjujung.tbox.R;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.Landmark;

import java.util.List;

/**
 *  [ 실제로 화면에 마스크를 그리는 클래스 ]
 *
 */

public class FaceGraphic extends GraphicOverlay.Graphic{
    static boolean isMaskSet = false;   // 마스크 씌울지 여부 변수
    static int resourceId;  //  마스크 drawable 아이디 변수
    private Drawable hatDrawable;
    private Context mContext;

    private volatile Face mFace;
    private int mFaceId;

    public FaceGraphic(GraphicOverlay overlay, Context context) {
        super(overlay);
        mContext = context;
    }

    void setId(int id) {
        mFaceId = id;
    }

    void setMask(boolean setMask) {
        isMaskSet = setMask;
    }

    void setResourceId(int reId) {
        resourceId = reId;
    }

    public boolean isMaskSet() {
        return isMaskSet;
    }

    public int getResourceId() {
        return resourceId;
    }

    void updateFace(Face face) {
        mFace = face;
        postInvalidate();
    }

    @Override
    public void draw(Canvas canvas) {
        Face face = mFace;
        if (face == null || isMaskSet() == false) {
            return;
        }else if(isMaskSet()){

            float x = translateX(face.getPosition().x + face.getWidth() / 2.0f);    //  머리 위치 (x)
            float y = translateY(face.getPosition().y + face.getHeight() / 4.0f); //  머리 위치 (y)
            float xOffset = scaleX(face.getWidth() / 2.0f);
            float yOffset = scaleY(face.getHeight() / 2.0f);
            float left = x - xOffset;
            float top = y - yOffset;
            float right = x + xOffset;
            float bottom = y + yOffset;

            Log.e("Face Graphic", "draw : x - " + xOffset + ", y - " + yOffset);

            drawHat(canvas, getResourceId(), (int)left, (int)top, (int)right, (int)bottom, mContext);
        }
    }

    private void drawHat(Canvas canvas, int resourceId, int left, int top, int right, int bottom, Context context) {
        hatDrawable = ContextCompat.getDrawable(context, resourceId);
        hatDrawable.setBounds(left, top, right, bottom);
        hatDrawable.draw(canvas);
    }

}

