package com.example.android.tflitecamerademo.playerVLC.model;

import java.util.ArrayList;
import java.util.List;

public class PlayState {
    public static final int SPEED_20 = 0;
    public static final int SPEED_40 = 1;
    public static final int SPEED_60 = 2;
    public static final int SPEED_80 = 3;
    public static final int SPEED_100 = 4;
    public static final int SPEED_150 = 5;
    public static final int SPEED_200 = 6;

    public static final int PLAY_STATUS_PLAYING = 0;
    public static final int PLAY_STATUS_PAUSE = 1;

    public static final int PLAY_MODE_SEQUENCE = 0;
    public static final int PLAY_MODE_LOOP = 1;


    boolean ab = false;
    boolean mirror = false;
    int speed = SPEED_100;
    int playStatus = PLAY_STATUS_PLAYING;
    int playMode = PLAY_MODE_SEQUENCE;
    boolean lock = false;
    long totalDuration = 0;
    long currentPosition = 0;
    long aPosition = 0;
    long bPosition = 0;
    boolean fullScreen = false;
    List<Media> mediaList = new ArrayList<>();
    List<Speed> speedList = new ArrayList<>();

    public PlayState() {
        speedList.add(Speed.newBuilder().speed(SPEED_20).title("20%").selected(false).build());
        speedList.add(Speed.newBuilder().speed(SPEED_40).title("40%").selected(false).build());
        speedList.add(Speed.newBuilder().speed(SPEED_60).title("60%").selected(false).build());
        speedList.add(Speed.newBuilder().speed(SPEED_80).title("80%").selected(false).build());
        speedList.add(Speed.newBuilder().speed(SPEED_100).title("100%").selected(false).build());
        speedList.add(Speed.newBuilder().speed(SPEED_150).title("150%").selected(false).build());
        speedList.add(Speed.newBuilder().speed(SPEED_200).title("200%").selected(false).build());
    }

    public boolean isAb() {
        return ab;
    }

    public void setAb(boolean ab) {
        this.ab = ab;
    }

    public boolean isMirror() {
        return mirror;
    }

    public void setMirror(boolean mirror) {
        this.mirror = mirror;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getPlayStatus() {
        return playStatus;
    }

    public void setPlayStatus(int playStatus) {
        this.playStatus = playStatus;
    }

    public int getPlayMode() {
        return playMode;
    }

    public void setPlayMode(int playMode) {
        this.playMode = playMode;
    }

    public boolean isLock() {
        return lock;
    }

    public void setLock(boolean lock) {
        this.lock = lock;
    }

    public long getTotalDuration() {
        return totalDuration;
    }

    public void setTotalDuration(long totalDuration) {
        this.totalDuration = totalDuration;
    }

    public long getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(long currentPosition) {
        this.currentPosition = currentPosition;
    }

    public long getaPosition() {
        return aPosition;
    }

    public void setaPosition(long aPosition) {
        this.aPosition = aPosition;
    }

    public long getbPosition() {
        return bPosition;
    }

    public void setbPosition(long bPosition) {
        this.bPosition = bPosition;
    }

    public boolean isFullScreen() {
        return fullScreen;
    }

    public void setFullScreen(boolean fullScreen) {
        this.fullScreen = fullScreen;
    }

    public List<Media> getMediaList() {
        return mediaList;
    }

    public void setMediaList(List<Media> mediaList) {
        this.mediaList = mediaList;
    }

    public static class Media {
        String title;
        boolean selected;

        public Media() {
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }
    }


    public static class Speed {
        int speed;
        String title;
        boolean selected;

        public Speed() {
        }

        private Speed(Builder builder) {
            setSpeed(builder.speed);
            setTitle(builder.title);
            setSelected(builder.selected);
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public int getSpeed() {
            return speed;
        }

        public void setSpeed(int speed) {
            this.speed = speed;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public static final class Builder {
            private int speed;
            private String title;
            private boolean selected;

            private Builder() {
            }

            public Builder speed(int val) {
                speed = val;
                return this;
            }

            public Builder title(String val) {
                title = val;
                return this;
            }

            public Builder selected(boolean val) {
                selected = val;
                return this;
            }

            public Speed build() {
                return new Speed(this);
            }
        }
    }
}
