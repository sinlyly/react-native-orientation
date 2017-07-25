package com.github.yamill.orientation;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.WindowManager;

import com.facebook.common.logging.FLog;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.common.ReactConstants;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class OrientationModule extends ReactContextBaseJavaModule implements LifecycleEventListener{
    int deviceOrientation = 0;
    String lastDeviceOrientation = "";
    double mWidthPixels;
    double mHeightPixels;

    public OrientationModule(ReactApplicationContext reactContext) {
        super(reactContext);
        final ReactApplicationContext ctx = reactContext;
        OrientationEventListener mOrientationListener;

        mOrientationListener = new OrientationEventListener(reactContext) {
            @Override
            public void onOrientationChanged(int orientation) {
                deviceOrientation = orientation;
                if(lastDeviceOrientation.compareTo(getDeviceOrientationAsString())!=0){
                    lastDeviceOrientation = getDeviceOrientationAsString();
                    //WritableNativeMap data = getDataMap();
                    WritableMap params = Arguments.createMap();
                    params.putString("orientation", getSingleOrientationAsString());
                    try{
                        ctx.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                                .emit("orientationDidChange", params);
                    } catch (RuntimeException e) {
                        Log.e("ERROR ", "java.lang.RuntimeException: Trying to invoke JS before CatalystInstance has been set!");
                    }
                }
            }
        };
        if (mOrientationListener.canDetectOrientation() == true) {
            mOrientationListener.enable();
        } else {
            mOrientationListener.disable();
        }

        ctx.addLifecycleEventListener(this);
    }

    @Override
    public String getName() {
        return "Orientation";
    }

    @ReactMethod
    public void getOrientation(Callback callback) {
        final int orientationInt = getReactApplicationContext().getResources().getConfiguration().orientation;

        String orientation = this.getOrientationString(orientationInt);

        if (orientation == "null") {
            callback.invoke(orientationInt, null);
        } else {
            callback.invoke(null, orientation);
        }
    }

    @ReactMethod
    public void lockToPortrait() {
        final Activity activity = getCurrentActivity();
        if (activity == null) {
            return;
        }
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @ReactMethod
    public void lockToLandscape() {
        final Activity activity = getCurrentActivity();
        if (activity == null) {
            return;
        }
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
    }

    @ReactMethod
    public void lockToLandscapeLeft() {
        final Activity activity = getCurrentActivity();
        if (activity == null) {
            return;
        }
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @ReactMethod
    public void lockToLandscapeRight() {
        final Activity activity = getCurrentActivity();
        if (activity == null) {
            return;
        }
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
    }

    @ReactMethod
    public void unlockAllOrientations() {
        final Activity activity = getCurrentActivity();
        if (activity == null) {
            return;
        }
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    @Override
    public @Nullable Map<String, Object> getConstants() {
        HashMap<String, Object> constants = new HashMap<String, Object>();
        int orientationInt = getReactApplicationContext().getResources().getConfiguration().orientation;

        String orientation = this.getOrientationString(orientationInt);
        if (orientation == "null") {
            constants.put("initialOrientation", null);
        } else {
            constants.put("initialOrientation", orientation);
        }

        return constants;
    }

    private String getOrientationString(int orientation) {
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return "LANDSCAPE";
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            return "PORTRAIT";
        } else if (orientation == Configuration.ORIENTATION_UNDEFINED) {
            return "UNKNOWN";
        } else {
            return "null";
        }
    }

    @Override
    public void onHostResume() {
//        final Activity activity = getCurrentActivity();
//
//        assert activity != null;
//        activity.registerReceiver(receiver, new IntentFilter("onConfigurationChanged"));
    }
    @Override
    public void onHostPause() {
//        final Activity activity = getCurrentActivity();
//        if (activity == null) return;
//        try
//        {
//            activity.unregisterReceiver(receiver);
//        }
//        catch (java.lang.IllegalArgumentException e) {
//            FLog.e(ReactConstants.TAG, "receiver already unregistered", e);
//        }
    }

    @Override
    public void onHostDestroy() {
//        final Activity activity = getCurrentActivity();
//        if (activity == null) return;
//        try
//        {
//            activity.unregisterReceiver(receiver);
//        }
//        catch (java.lang.IllegalArgumentException e) {
//            FLog.e(ReactConstants.TAG, "receiver already unregistered", e);
//        }
    }

    /**
     * Return the device orientation
     * @return the device orientation of a String
     */
    private String getDeviceOrientationAsString(){
        if((deviceOrientation>=0&&deviceOrientation<45)||(deviceOrientation>=315&&deviceOrientation<360)){
            return "PORTRAIT";
        }else if(deviceOrientation>=45&&deviceOrientation<135){
            return "LANDSCAPE LEFT";
        }else if(deviceOrientation>=135&&deviceOrientation<225){
            return "PORTRAIT UPSIDE DOWN";
        }else if(deviceOrientation>=225&&deviceOrientation<315){
            return "LANDSCAPE RIGHT";
        }else return "UNKNOWN";
    }

    private String getSingleOrientationAsString(){
        if((deviceOrientation>=0&&deviceOrientation<45)||(deviceOrientation>=315&&deviceOrientation<360
            || (deviceOrientation>=135&&deviceOrientation<225))){
            return "PORTRAIT";
        }else if((deviceOrientation>=45&&deviceOrientation<135) || (deviceOrientation>=225&&deviceOrientation<315)){
            return "LANDSCAPE";
        }
        else return "UNKNOWN";
    }

    public WritableNativeMap getDataMap(){
        WritableNativeMap data = new WritableNativeMap();
        data.putString("deviceOrientation",getDeviceOrientationAsString());
        data.putString("applicationOrientation", getApplicationOrientation());
        data.putString("device", getModel());
        final int width = getDimension()[0];
        final int height = getDimension()[1];
        data.putMap("size",new WritableNativeMap(){{putInt("width",width);putInt("height",height);}});
        return data;
    }

    /**
     * Return the application orientation
     * @return the application orientation of a String
     */
    private String getApplicationOrientation() {
        String orientationStr = "";
        switch (getApplicationOrientationAsNumber()) {
            case 0:
                orientationStr = "PORTRAIT";
                break;
            case 1:
                orientationStr = "LANDSCAPE RIGHT";
                break;
            case 2:
                orientationStr = "PORTRAIT UPSIDE DOWN";
                break;
            case 3:
                orientationStr = "LANDSCAPE LEFT";
                break;
            default:
                orientationStr = "UNKNOWN";
                break;
        }
        return orientationStr;
    }

    /**
     * Return the dimension of the screen
     * @return the dimension of the screen
     */
    private int[] getDimension() {
        final Display display = ((WindowManager) getReactApplicationContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int[] dim;
        int width;
        int height;

        DisplayMetrics dm = new DisplayMetrics();
        display.getMetrics(dm);
        setRealDeviceSizeInPixels();
        width = (int)(mWidthPixels/dm.density);
        height = (int)(mHeightPixels/dm.density);

        dim = new int[]{width, height};
        if (dim.length == 2)
            return dim;
        else return new int[]{-1, -1};
    }


    /**
     * To get the model of the phone
     * @return the model of the phone as String
     */
    public String getModel() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    /**
     * Return the application orientation
     * @return the application orientation of a Integer
     */
    private int getApplicationOrientationAsNumber() {

        final Display display = ((WindowManager) getReactApplicationContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

        switch (display.getRotation()) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 1;
            case Surface.ROTATION_180:
                return 2;
            case Surface.ROTATION_270:
                return 3;
        }
        return 0;
    }

    private void setRealDeviceSizeInPixels() {
        WindowManager windowManager = ((WindowManager) getReactApplicationContext().getSystemService(Context.WINDOW_SERVICE));
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);


        // since SDK_INT = 1;
        mWidthPixels = displayMetrics.widthPixels;
        mHeightPixels = displayMetrics.heightPixels;

        // includes window decorations (statusbar bar/menu bar)
        if (Build.VERSION.SDK_INT >= 14 && Build.VERSION.SDK_INT < 17) {
            try {
                mWidthPixels = (Integer) Display.class.getMethod("getRawWidth").invoke(display);
                mHeightPixels = (Integer) Display.class.getMethod("getRawHeight").invoke(display);
            } catch (Exception ignored) {
            }
        }

        // includes window decorations (statusbar bar/menu bar)
        if (Build.VERSION.SDK_INT >= 17) {
            try {
                Point realSize = new Point();
                Display.class.getMethod("getRealSize", Point.class).invoke(display, realSize);
                mWidthPixels = realSize.x;
                mHeightPixels = realSize.y;
            } catch (Exception ignored) {
            }
        }
    }





}
