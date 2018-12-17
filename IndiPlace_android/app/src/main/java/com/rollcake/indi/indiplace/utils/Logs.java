package com.rollcake.indi.indiplace.utils;

import android.app.Activity;
import android.os.Debug;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by YJSong on 2017. 1. 4..
 */
public class Logs {

    private static class Constants{
        public static boolean DEBUG = true;
    }

    public static void i(String tag, String message){
        if(Constants.DEBUG == true) Log.i(tag, message);
    }

    public static void d(String tag, String message){
        if(Constants.DEBUG == true) Log.d(tag, message);
    }

    public static void w(String tag, String message){
        if(Constants.DEBUG == true) Log.w(tag, message);
    }

    public static void e(String tag, String message){
        if(Constants.DEBUG == true) Log.e(tag, message);
    }

    public static void ShowToast(Activity activity , String str){
        if(Debug.isDebuggerConnected()){
            Logs.e("Log" , str);
        }else{
            Toast.makeText(activity , str , Toast.LENGTH_SHORT).show();
        }
    }
}
