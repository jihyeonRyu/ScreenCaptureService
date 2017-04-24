package com.rosie.accessibilityservice;

import android.util.Log;

import java.lang.reflect.Method;

/**
 * Created by ryuji on 2017-04-24.
 */

public class Reflection {
    private MyService service;
    private final static String TAG = "Reflection";
    private Class mClass;

    private final static String className = "android.view.SurfaceControl";
    private final static String methodName = "screenshot";

    Reflection(MyService service){
        this.service = service;

        try {
            mClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    void printMethod()  {
        try{

            Method screenshot = mClass.getMethod(methodName, Integer.class, Integer.class);
            Object bitmap = screenshot.invoke(null, service.screenWidth, service.screenHeight);
            Log.d(TAG, "Object created!");

        }catch(Exception e){
            e.printStackTrace();
        }


    }


}
