package com.rosie.accessibilityservice;

import android.app.Instrumentation;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SdkSuppress;
import android.support.test.runner.AndroidJUnit4;

import android.support.test.uiautomator.UiDevice;
import android.util.Log;


import org.junit.runner.RunWith;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by ryuji on 2017-04-17.
 */

@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = 18)
public class ScreenShot {
    String TAG = "ScreenShot";
    private AccessibilityService service;
    private UiDevice mDevice;
    private String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/IMG/";
    private File file = new File(dirPath);

    ScreenShot(AccessibilityService service){
        this.service = service;
    }

    public void capture() {
        // 디렉터리 존재하지 않으면 생성
        if (!file.exists())
            file.mkdirs();
        try{
            Bitmap bitmap = InstrumentationRegistry.getInstrumentation().getUiAutomation().takeScreenshot();
        }catch(Exception e){
            Log.d(TAG, e.toString());
        }
    }

}
