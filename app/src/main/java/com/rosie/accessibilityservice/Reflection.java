package com.rosie.accessibilityservice;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static android.view.Surface.ROTATION_0;

/**
 * Created by ryuji on 2017-04-24.
 */

public class Reflection {

    private final static String TAG = "Reflection";
    private Class<?> mClass;
    private MyService service;
    private final static String className = "android.view.SurfaceControl";
    private final static String methodName = "screenshot";

    private static String STORE_DIRECTORY;

    Reflection(MyService service){

        this.service = service;

        try {
            mClass = Class.forName(className);
            createDirectory();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    void callMethod(AccessibilityNodeInfo node)  {
        try{

            Method cropshot = mClass.getMethod(methodName, Rect.class, int.class, int.class, int.class, int.class, boolean.class, int.class);

            if(node == null) {
                Log.d(TAG, "null icon");
                return;
            }

            Rect rect = new Rect();
            node.getBoundsInScreen(rect);
            Log.d("TAG", "icon[ " + node.getClassName() + "]  " + rect.width() + " : " + rect.height());
            Bitmap result = (Bitmap) cropshot.invoke(null, rect, rect.width(), rect.height(), 0, 1000000, false, ROTATION_0);

            if(result != null){
                saveFile(result);
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }


    ////////////////////////// save bitmap image ////////////////////////////////

    private void createDirectory (){

        File externalFilesDir = service.getExternalFilesDir(null);
        if (externalFilesDir != null) {
            STORE_DIRECTORY = externalFilesDir.getAbsolutePath() + "/screenshots/";
            File storeDirectory = new File(STORE_DIRECTORY);
            if (!storeDirectory.exists()) {
                boolean success = storeDirectory.mkdirs();
                if (!success) {
                    Log.e(TAG, "failed to create file storage directory.");
                    return;
                }
            }
        } else {
            Log.e(TAG, "failed to create file storage directory, getExternalFilesDir is null.");
            return;
        }
    }

    void saveFile(Bitmap result) throws FileNotFoundException {

        FileOutputStream fos = null;
        long num = System.currentTimeMillis();
        fos = new FileOutputStream(STORE_DIRECTORY + "/myscreen_" + num + ".png");
        result.compress(Bitmap.CompressFormat.PNG, 100, fos);
        Log.d(TAG, "captured image: " + num);

    }

}
