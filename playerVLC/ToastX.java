package com.example.android.tflitecamerademo.playerVLC;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.widget.Toast;

import com.example.android.tflitecamerademo.MyApp;


public class ToastX {
    private static Toast toast;

    public static synchronized void show(final String text) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    toast.cancel();
                } catch (Exception e) {

                }
                toast = Toast.makeText(MyApp.getContext(), text, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        });
    }


    public static synchronized void showServiceException() {
        show("服务器异常，请稍后再试");
    }

    public static synchronized void show(Exception e) {
        if (e == null) return;
        show(e.getMessage());
    }

    public static synchronized void show(Context context, Exception e) {
        if (e == null) return;
        show(context, e.getMessage());
    }


    public static synchronized void show(final int strId) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    toast.cancel();
                } catch (Exception e) {

                }
                toast = Toast.makeText(MyApp.getContext(), strId, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        });
    }

    public static synchronized void show(final Context conetxt, final String text) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    toast.cancel();
                } catch (Exception e) {

                }
                toast = Toast.makeText(conetxt, text, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        });
    }

    public static synchronized void show(final Context context, final int strId) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    toast.cancel();
                } catch (Exception e) {

                }
                toast = Toast.makeText(context, strId, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        });
    }
}
