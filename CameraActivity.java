/* Copyright 2017 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package com.example.android.tflitecamerademo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.android.tflitecamerademo.base.BaseActivity;
import com.example.android.tflitecamerademo.playerNE.HttpPostUtils;
import com.example.android.tflitecamerademo.playerNE.VodActivity;
import com.example.android.tflitecamerademo.playerNE.VodActivity2;
import com.example.android.tflitecamerademo.playerVLC.ToastX;
import com.example.android.tflitecamerademo.util.AppUtils;
import com.netease.neliveplayer.playerkit.sdk.PlayerManager;
import com.netease.neliveplayer.playerkit.sdk.model.SDKInfo;
import com.netease.neliveplayer.playerkit.sdk.model.SDKOptions;
import com.netease.neliveplayer.sdk.NELivePlayer;
import com.vondear.rxtool.RxFileTool;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/** Main {@code Activity} class for the Camera app. */
public class CameraActivity extends BaseActivity {

  private static final String TAG = "CameraActivity";
  GestureViewPlus gestureView;
  public static final String MEDIA_FILE_NAME = "kaluli.mp4";
  public static final String MEDIA_PATH = RxFileTool.getSDCardPath() + MEDIA_FILE_NAME;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_camera);
    try {
      gestureView = findViewById(R.id.gestureView);
      getLifecycle().addObserver(gestureView);
    } catch (Exception e) {
      Log.i("CameraActivity","CameraActivity oncreate error : "+ e.toString());
      ToastX.show("程序异常:"+e.toString());
    }
    String mediaPath = MEDIA_PATH;
    if (!RxFileTool.fileExists(mediaPath)) {
      AppUtils.copy(this,RxFileTool.getSDCardPath(),MEDIA_FILE_NAME);
    }
    initPlayer();
//    if (null == savedInstanceState) {
//      getFragmentManager()
//          .beginTransaction()
//          .replace(R.id.container, Camera2BasicFragment.newInstance())
//          .commit();
//    }
  }

  public void gotoPlayer(View view) {
    String decodeType = "hardware";// decodeType = "software";
    Intent intent = new Intent(this, VodActivity2.class);
//    intent.putExtra("media_type", "localaudio");
//    intent.putExtra("decode_type", decodeType);
    intent.putExtra("videoPath", MEDIA_PATH);
    startActivity(intent);
    finish();

  }





  private boolean sendData(final String urlStr, final String content) {
    int response = 0;
    try {
      URL url = new URL(urlStr);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setConnectTimeout(5000);
      conn.setDoOutput(true);
      conn.setDoInput(true);
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
      OutputStream outputStream = conn.getOutputStream();
      outputStream.write(content.getBytes());
      response = conn.getResponseCode();
      if (response == HttpURLConnection.HTTP_OK) {
        Log.i(TAG, " sendData finished,data:" + content);

      } else {
        Log.i(TAG, " sendData, response: " + response);

      }
    } catch (IOException e) {
      Log.e(TAG, "sendData, recv code is error: " + e.getMessage());

    } catch (Exception e) {
      Log.e(TAG, "sendData, recv code is error2: " + e.getMessage());

    }
    return (response == HttpURLConnection.HTTP_OK);
  }


  private NELivePlayer.OnDataUploadListener mOnDataUploadListener = new NELivePlayer.OnDataUploadListener() {

    @Override
    public boolean onDataUpload(String url, String data) {
      Log.d(TAG, "onDataUpload url:" + url + ", data:" + data);
      sendData(url, data);
      return true;
    }

    @Override
    public boolean onDocumentUpload(String url, Map<String, String> params, Map<String, String> filepaths) {
      Log.d(TAG, "onDataUpload url:" + url + ", params:" + params + ",filepaths:" + filepaths);
      return (new HttpPostUtils(url, params, filepaths).connPost());
    }
  };

  private NELivePlayer.OnSupportDecodeListener mOnSupportDecodeListener = new NELivePlayer.OnSupportDecodeListener() {

    @Override
    public void onSupportDecode(boolean isSupport) {
      Log.d(TAG, "是否支持H265硬件解码 onSupportDecode isSupport:" + isSupport);
      //如果支持H265硬件解码，那么可以使用H265的视频源进行播放
    }
  };


  private void initPlayer() {
    SDKOptions config = new SDKOptions();
    //是否开启动态加载功能，默认关闭
    //        config.dynamicLoadingConfig = new NEDynamicLoadingConfig();
    //        config.dynamicLoadingConfig.isDynamicLoading = true;
    //        config.dynamicLoadingConfig.isArmeabiv7a = true;
    //        config.dynamicLoadingConfig.armeabiv7aUrl = "your_url";
    //        config.dynamicLoadingConfig.onDynamicLoadingListener = mOnDynamicLoadingListener;
    // SDK将内部的网络请求以回调的方式开给上层，如果需要上层自己进行网络请求请实现config.dataUploadListener，
    // 如果上层不需要自己进行网络请求而是让SDK进行网络请求，这里就不需要操作config.dataUploadListener
    config.dataUploadListener = mOnDataUploadListener;
    //是否支持H265解码回调
    config.supportDecodeListener = mOnSupportDecodeListener;
    //这里可以绑定客户的账号系统或device_id，方便出问题时双方联调
    //        config.thirdUserId = "your_id";
    PlayerManager.init(this, config);
    SDKInfo sdkInfo = PlayerManager.getSDKInfo(this);
    Log.i(TAG, "NESDKInfo:version" + sdkInfo.version + ",deviceId:" + sdkInfo.deviceId);
  }
}
