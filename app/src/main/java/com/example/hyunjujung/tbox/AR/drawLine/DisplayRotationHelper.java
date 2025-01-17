package com.example.hyunjujung.tbox.AR.drawLine;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.view.Display;
import android.view.WindowManager;

import com.google.ar.core.Session;

public class DisplayRotationHelper implements DisplayManager.DisplayListener {
    private boolean viewportChanged;
    private int viewportWidth;
    private int viewportHeight;
    private final Context context;
    private final Display display;

    public DisplayRotationHelper(Context context) {
        this.context = context;
        display = context.getSystemService(WindowManager.class).getDefaultDisplay();
    }

    public void onResume() {
        context.getSystemService(DisplayManager.class).registerDisplayListener(this, null);
    }

    public void onPause() {
        context.getSystemService(DisplayManager.class).unregisterDisplayListener(this);
    }

    public void onSurfaceChanged(int width, int height) {
        viewportWidth = width;
        viewportHeight = height;
        viewportChanged = true;
    }

    public void updateSessionIfNeeded(Session session) {
        if (viewportChanged) {
            int displayRotation = display.getRotation();
            session.setDisplayGeometry(displayRotation, viewportWidth, viewportHeight);
            viewportChanged = false;
        }
    }

    public int getRotation() {
        return display.getRotation();
    }

    @Override
    public void onDisplayAdded(int i) {

    }

    @Override
    public void onDisplayRemoved(int i) {

    }

    @Override
    public void onDisplayChanged(int i) {
        viewportChanged = true;
    }
}
