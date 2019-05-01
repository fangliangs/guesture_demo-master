package com.example.android.tflitecamerademo.event;

import android.text.TextUtils;

import java.util.List;

public class EventGesture {
    public String getGestureResult() {
        return gestureResult;
    }

    public void setGestureResult(String gestureResult) {
        this.gestureResult = gestureResult;
    }

    private String gestureResult;

    private EventGesture(Builder builder) {
        setGestureResult(builder.gestureResult);
        setGestureEnum(builder.GestureEnum);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public GestureEnum getGestureEnum() {
        return GestureEnum;
    }

    public void setGestureEnum(GestureEnum gestureEnum) {
        GestureEnum = gestureEnum;
    }

    private GestureEnum GestureEnum;
    public void EventGesture() {

    }

    public void EventGesture(GestureEnum GestureEnum) {
        this.GestureEnum = GestureEnum;
    }


    public static final class Builder {
        private GestureEnum GestureEnum;
        private String gestureResult;

        private Builder() {
        }

        public Builder GestureEnum(GestureEnum val) {
            GestureEnum = val;
            return this;
        }

        public EventGesture build() {
            return new EventGesture(this);
        }

        public Builder gestureResult(String val) {
            gestureResult = val;
            return this;
        }
    }


    public boolean hasOk(List<String> resultList) {
        if (resultList == null || resultList.isEmpty()) {
            return false;
        }
        for (String result : resultList) {
            if (result.equals(GestureEnum.TYPE_OK.getValue())) {
                return true;
            }
        }
        return false;
    }


    public boolean hasFive(List<String> resultList) {
        if (resultList == null || resultList.isEmpty()) {
            return false;
        }
        for (String result : resultList) {
            if (result.equals(GestureEnum.TYPE_FIVE.getValue())) {
                return true;
            }
        }
        return false;
    }

    public boolean hasXin(List<String> resultList) {
        if (resultList == null || resultList.isEmpty()) {
            return false;
        }
        for (String result : resultList) {
            if (result.equals(GestureEnum.TYPE_XIN.getValue())) {
                return true;
            }
        }
        return false;
    }

    public boolean hasStop(List<String> resultList) {
        if (resultList == null || resultList.isEmpty()) {
            return false;
        }
        for (String result : resultList) {
            if (result.equals(GestureEnum.TYPE_STOP.getValue())) {
                return true;
            }
        }
        return false;
    }

    public boolean hasYe(List<String> resultList) {
        if (resultList == null || resultList.isEmpty()) {
            return false;
        }
        for (String result : resultList) {
            if (result.equals(GestureEnum.TYPE_YE.getValue())) {
                return true;
            }
        }
        return false;
    }
}
