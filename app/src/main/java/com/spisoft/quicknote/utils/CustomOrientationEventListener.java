package com.spisoft.quicknote.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.provider.Settings;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.WindowManager;

import java.util.concurrent.locks.ReentrantLock;

// src https://stackoverflow.com/questions/43291584/override-onconfigurationchanged-while-maintaining-portrait-mode
public abstract class CustomOrientationEventListener extends OrientationEventListener {
    private String TAG="CustomOrientation";
    private static final int CONFIGURATION_ORIENTATION_UNDEFINED = Configuration.ORIENTATION_UNDEFINED;
    private volatile int defaultScreenOrientation = CONFIGURATION_ORIENTATION_UNDEFINED;
    private int prevOrientation = OrientationEventListener.ORIENTATION_UNKNOWN;
    private Context ctx;
    private final int ROTATION_O    = 1;
    private final int ROTATION_90   = 2;
    private final int ROTATION_180  = 3;
    private final int ROTATION_270  = 4;

    private int rotation = 0;
    private ReentrantLock lock = new ReentrantLock(true);

    public CustomOrientationEventListener(Context context) {
        super(context);
        ctx = context;
    }

    public CustomOrientationEventListener(Context context, int rate) {
        super(context, rate);
        ctx = context;
    }

    @Override
    public void onOrientationChanged(final int orientation) {

        int currentOrientation = OrientationEventListener.ORIENTATION_UNKNOWN;
        if (orientation >= 340 || orientation < 20 && rotation != ROTATION_O) {
            currentOrientation = Surface.ROTATION_0;
            rotation = ROTATION_O;

        } else if (orientation >= 70 && orientation < 110 && rotation != ROTATION_90) {
            currentOrientation = Surface.ROTATION_90;
            rotation = ROTATION_90;

        } else if (orientation >= 160 && orientation < 200 && rotation != ROTATION_180) {
            currentOrientation = Surface.ROTATION_180;
            rotation = ROTATION_180;

        } else if (orientation >= 250 && orientation < 290 && rotation != ROTATION_270) {
            currentOrientation = Surface.ROTATION_270;
            rotation = ROTATION_270;
        }

        if (prevOrientation != currentOrientation
                && orientation != OrientationEventListener.ORIENTATION_UNKNOWN) {
            prevOrientation = currentOrientation;
            if (currentOrientation != OrientationEventListener.ORIENTATION_UNKNOWN)
                reportOrientationChanged(currentOrientation);
        }

    }

    private void reportOrientationChanged(final int currentOrientation) {

        int defaultOrientation = getDeviceDefaultOrientation();
        int orthogonalOrientation = defaultOrientation == Configuration.ORIENTATION_LANDSCAPE ? Configuration.ORIENTATION_PORTRAIT
                : Configuration.ORIENTATION_LANDSCAPE;

        int toReportOrientation;

        if (currentOrientation == Surface.ROTATION_0
                || currentOrientation == Surface.ROTATION_180)
            toReportOrientation = defaultOrientation;
        else
            toReportOrientation = orthogonalOrientation;

        onSimpleOrientationChanged(currentOrientation);
    }

    /**
     * Must determine what is default device orientation (some tablets can have
     * default landscape). Must be initialized when device orientation is
     * defined.
     *
     * @return value of {@link Configuration#ORIENTATION_LANDSCAPE} or
     *         {@link Configuration#ORIENTATION_PORTRAIT}
     */
    private int getDeviceDefaultOrientation() {
        if (defaultScreenOrientation == CONFIGURATION_ORIENTATION_UNDEFINED) {
            lock.lock();
            defaultScreenOrientation = initDeviceDefaultOrientation(ctx);
            lock.unlock();
        }
        return defaultScreenOrientation;
    }

    /**
     * Provides device default orientation
     *
     * @return value of {@link Configuration#ORIENTATION_LANDSCAPE} or
     *         {@link Configuration#ORIENTATION_PORTRAIT}
     */
    private int initDeviceDefaultOrientation(Context context) {

        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Configuration config = context.getResources().getConfiguration();
        int rotation = windowManager.getDefaultDisplay().getRotation();

        boolean isLand = config.orientation == Configuration.ORIENTATION_LANDSCAPE;
        boolean isDefaultAxis = rotation == Surface.ROTATION_0
                || rotation == Surface.ROTATION_180;

        int result = CONFIGURATION_ORIENTATION_UNDEFINED;
        if ((isDefaultAxis && isLand) || (!isDefaultAxis && !isLand)) {
            result = Configuration.ORIENTATION_LANDSCAPE;
        } else {
            result = Configuration.ORIENTATION_PORTRAIT;
        }
        return result;
    }

    /**
     * Fires when orientation changes from landscape to portrait and vice versa.
     *
     * @param orientation
     *            value of {@link Configuration#ORIENTATION_LANDSCAPE} or
     *            {@link Configuration#ORIENTATION_PORTRAIT}
     */
    public abstract void onSimpleOrientationChanged(int orientation);

}