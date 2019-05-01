package com.example.android.tflitecamerademo.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.vondear.rxtool.RxActivityTool;

import org.greenrobot.eventbus.EventBus;

public class BaseActivity extends AppCompatActivity {

    private boolean autoRegisterEventBus = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RxActivityTool.addActivity(this);
    }

    /**
     * 自动注册和解除EventBus
     *
     * @param register
     */
    protected void setAutoRegisterEventBus(boolean register) {
        autoRegisterEventBus = register;
    }


    @Override
    protected void onStart() {
        super.onStart();
        try {
            if (autoRegisterEventBus && !EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().register(this);
            }
        } catch (Exception e) {

        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (autoRegisterEventBus && EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().unregister(this);
            }
        } catch (Exception e) {

        }
        RxActivityTool.finishActivity(this);
    }
}
