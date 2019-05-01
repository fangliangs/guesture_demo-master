package com.example.android.tflitecamerademo.playerVLC.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ConstraintDragLayout extends ConstraintLayout {
    ViewDragHelper viewDragHelper;
    DragCallback dragCallback;

    public ConstraintDragLayout(Context context) {
        super(context);
        init();
    }

    public ConstraintDragLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ConstraintDragLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    void init() {
        dragCallback = new DragCallback();
        viewDragHelper = ViewDragHelper.create(this, 10.0f, new ViewDragHelper.Callback() {
            @Override
            public boolean tryCaptureView(@NonNull View view, int i) {
                return dragCallback.tryCaptureView(view, i);
            }

            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                return dragCallback.clampViewPositionHorizontal(child, left, dx);
            }

            @Override
            public int clampViewPositionVertical(View child, int top, int dy) {
                return dragCallback.clampViewPositionVertical(child, top, dy);
            }

            @Override
            public int getViewHorizontalDragRange(View child) {
                return dragCallback.getViewHorizontalDragRange(child);
            }

            @Override
            public int getViewVerticalDragRange(View child) {
                return dragCallback.getViewVerticalDragRange(child);
            }

            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
                dragCallback.onViewReleased(releasedChild, xvel, yvel);
            }
        });
    }

    public void setDragCallback(DragCallback callback) {
        if (callback == null) return;
        dragCallback = callback;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return viewDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        viewDragHelper.processTouchEvent(event);
        return true;
    }

    public static class DragCallback {
        public boolean tryCaptureView(@NonNull View view, int i) {
            return false;
        }

        public int clampViewPositionHorizontal(View child, int left, int dx) {
            return 0;
        }

        public int clampViewPositionVertical(View child, int top, int dy) {
            return 0;
        }

        public int getViewHorizontalDragRange(View child) {
            return 0;
        }

        public int getViewVerticalDragRange(View child) {
            return 0;
        }

        public void onViewReleased(View releasedChild, float xvel, float yvel) {

        }
    }

}
