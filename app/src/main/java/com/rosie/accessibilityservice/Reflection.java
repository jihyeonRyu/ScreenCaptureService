package com.rosie.accessibilityservice;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.Method;

/**
 * Created by ryuji on 2017-04-24.
 */

public class Reflection {
    private MyService service;
    private final static String TAG = "Reflection";
    private Class<?> mClass;

    private final static String className = "android.view.SurfaceControl";
    private final static String methodName = "screenshot";

    private static String STORE_DIRECTORY;
    private static int IMAGES_PRODUCED = 0;

    Reflection(MyService service){
        this.service = service;

        try {
            mClass = Class.forName(className);
            createDirectory();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

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


    void printMethod()  {
        try{

          //  Method[] methods = mClass.getMethods();
            Method screenshot = mClass.getMethod(methodName, int.class, int.class);

          //  Method screenshot = methods[23];
            Object result = screenshot.invoke(null, service.screenWidth, service.screenHeight);

            if(result != null){
                Log.d(TAG, result.getClass() + " Object created!");
                saveFile(result);
            }


        }catch(Exception e){
            e.printStackTrace();
        }
    }
    void saveFile(Object result) throws FileNotFoundException {

        FileOutputStream fos = null;
        fos = new FileOutputStream(STORE_DIRECTORY + "/myscreen_" + IMAGES_PRODUCED + ".png");
        Bitmap bitmap = (Bitmap) result;
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);


        IMAGES_PRODUCED++;
        Log.d(TAG, "captured image: " + IMAGES_PRODUCED);

    }


}
