package com.example.android.tflitecamerademo.playerVLC.view;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Guideline;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.android.tflitecamerademo.R;
import com.example.android.tflitecamerademo.playerVLC.model.PlayState;

import java.util.List;

public class MediaControllerB extends FrameLayout implements BaseMediaController {
    View backBtn;
    ImageView backIv;
    TextView titleTv;
    View playListBtn;
    ImageView playListIv;
    View downloadBtn;
    ImageView downloadIv;
    ConstraintLayout topBarLayout;
    ImageView playerLockBtn;
    ImageView playBtn;
    ImageView pauseBtn;
    View bottomBarCenterLine;
    View bottomBarBg;
    TextView playedTimeTv;
    TextView mirrorTv;
    TextView speedTv;
    TextView abTv;
    TextView totalTimeTv;
    View mirrorBtn;
    View speedBtn;
    View abBtn;
    View abProgressView;
    SeekBar seekBar;
    FrameLayout aLayout;
    FrameLayout bLayout;
    Guideline baseLine;
    ConstraintDragLayout constraintDragLayout;
    ConstraintLayout bottomBarLayout;

    PlayState playState;

    public MediaControllerB(Context context) {
        super(context);
        init();
    }

    public MediaControllerB(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MediaControllerB(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.media_controller_b, this, true);
        bindView();
        playState = new PlayState();
        constraintDragLayout.setDragCallback(new ConstraintDragLayout.DragCallback() {
            @Override
            public boolean tryCaptureView(View view, int i) {
                return view.getId() == R.id.aLayout || view.getId() == R.id.bLayout;
            }

            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                return left;
            }

            @Override
            public int clampViewPositionVertical(View child, int top, int dy) {
                return child.getTop();
            }

            @Override
            public int getViewHorizontalDragRange(View child) {
                return 1;
            }

            @Override
            public int getViewVerticalDragRange(View child) {
                return 1;
            }

            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {

            }
        });
    }

    public void setMediaList(List<PlayState.Media> mediaList) {
        if (mediaList == null || mediaList.isEmpty()) return;
        playState.setMediaList(mediaList);
        for (PlayState.Media media : playState.getMediaList()) {
            if (media.isSelected()) {
                titleTv.setText(media.getTitle());
                break;
            }
        }
    }


    void bindView() {
        backBtn = findViewById(R.id.backBtn);
        backIv = findViewById(R.id.backIv);
        titleTv = findViewById(R.id.titleTv);
        playListBtn = findViewById(R.id.playListBtn);
        playListIv = findViewById(R.id.playListIv);
        downloadBtn = findViewById(R.id.downloadBtn);
        downloadIv = findViewById(R.id.downloadIv);
        topBarLayout = findViewById(R.id.topBarLayout);
        playerLockBtn = findViewById(R.id.playerLockBtn);
        playBtn = findViewById(R.id.playBtn);
        pauseBtn = findViewById(R.id.pauseBtn);
        bottomBarCenterLine = findViewById(R.id.bottomBarCenterLine);
        bottomBarBg = findViewById(R.id.bottomBarBg);
        playedTimeTv = findViewById(R.id.playedTimeTv);
        mirrorTv = findViewById(R.id.mirrorTv);
        speedTv = findViewById(R.id.speedTv);
        abTv = findViewById(R.id.abTv);
        totalTimeTv = findViewById(R.id.totalTimeTv);
        mirrorBtn = findViewById(R.id.mirrorBtn);
        speedBtn = findViewById(R.id.speedBtn);
        abBtn = findViewById(R.id.abBtn);
        abProgressView = findViewById(R.id.abProgressView);
        seekBar = findViewById(R.id.seekBar);
        aLayout = findViewById(R.id.aLayout);
        bLayout = findViewById(R.id.bLayout);
        baseLine = findViewById(R.id.baseLine);
        constraintDragLayout = findViewById(R.id.constraintDragLayout);
        bottomBarLayout = findViewById(R.id.bottomBarLayout);
    }

}
