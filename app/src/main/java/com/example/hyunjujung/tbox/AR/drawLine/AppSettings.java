package com.example.hyunjujung.tbox.AR.drawLine;

import javax.vecmath.Vector3f;

public class AppSettings {
    private static Vector3f color = new Vector3f(1f, 1f, 1f);
    private static final float strokeDrawDistance = 0.125f;
    private static final float minDistance = 0.000625f;
    private static final float nearClip = 0.001f;
    private static final float farClip = 100.0f;


    public static float getStrokeDrawDistance() {
        return strokeDrawDistance;
    }

    public static Vector3f getColor() {
        return color;
    }

    public static float getMinDistance() {
        return minDistance;
    }

    public static float getNearClip(){
        return nearClip;
    }

    public static float getFarClip(){
        return farClip;
    }


}
