package com.example.android.tflitecamerademo.playerVLC.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.example.android.tflitecamerademo.R;

public class MediaControllerA extends FrameLayout implements BaseMediaController {


    public MediaControllerA(Context context) {
        super(context);
        init();
    }

    public MediaControllerA(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MediaControllerA(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.media_controller_a, this, true);
    }

}
