package com.example.android.tflitecamerademo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.tflitecamerademo.event.EventGesture;
import com.example.android.tflitecamerademo.util.AppUtils;
import com.example.gesturerecognition.HandRecognition;
import com.github.dfqin.grantor.PermissionListener;
import com.github.dfqin.grantor.PermissionsUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class GestureViewPlus extends FrameLayout implements LifecycleObserver {

    private static final String TAG = GestureViewPlus.class.getSimpleName();

    private static final String tag = "GeustureDebugTag";

    Context mContext;

    View rootView;

    int OrientationAngle=0;

    public GestureViewPlus(Context context) {
        super(context);
        this.mContext = context;
        init();
    }

    public GestureViewPlus(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        init();
    }

    public GestureViewPlus(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
//        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.AiyigePlayerView);
//        ta.recycle();
        init();
    }

    private void init() {
        rootView = LayoutInflater.from(getContext()).inflate(R.layout.gesture_view_plus, this, true);
        textureView = rootView.findViewById(R.id.texture);
        imageview_instance = (ImageView) rootView.findViewById(R.id.imageView);
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                // 打开相机 0后置 1前置
                mCamera = Camera.open(1);
                if (mCamera != null) {
                    // 设置相机预览宽高，此处设置为TextureView宽高
                    Camera.Parameters params = mCamera.getParameters();
                    params.setPreviewSize(width, height);
                    // 设置自动对焦模式
                    List<String> focusModes = params.getSupportedFocusModes();
                    if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                        params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                        mCamera.setParameters(params);
                    }
                    try {
//                      mCamera.setDisplayOrientation(0);// 设置预览角度，并不改变获取到的原始数据方向
                        // 绑定相机和预览的View
                        mCamera.setPreviewTexture(surface);
                        // 开始预览
                        mCamera.startPreview();
                        addCallBack();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
    }

    boolean IsRun=false;
    private void addCallBack() {
        if(mCamera!=null){
            mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    Camera.Size size = camera.getParameters().getPreviewSize();
                    try{

                        YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
                        if(image!=null){
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            image.compressToJpeg(new Rect(0, 0, size.width, size.height), 80, stream);
                            Bitmap bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
                            Bitmap img=adjustPhotoRotation(bmp,OrientationAngle);
                            if(!IsRun){
                                IsRun=true;
                                new MyThread(img).start();
                            }
                            imageview_instance.setImageBitmap(img);
                            stream.close();
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public class MyThread extends Thread
    {
        Bitmap bitmap;
        public MyThread(Bitmap bitmap){
            this.bitmap=bitmap;
        }
        public void run() {
            RunRecognition(bitmap);
        }
    }

    public Bitmap adjustPhotoRotation(Bitmap bm, final int orientationDegree) {

        Matrix m = new Matrix();
        if(orientationDegree==-90){ m.postRotate(orientationDegree);}
        if(orientationDegree==90){ m.postRotate(orientationDegree);}
        if(orientationDegree==180){ m.setRotate(orientationDegree, (float)bm.getWidth(), (float) bm.getHeight());}
        if(orientationDegree==0){ m.setRotate(orientationDegree, (float)bm.getWidth(), (float) bm.getHeight());}
        try {
            Bitmap bm1 = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
            return bm1;
        } catch (OutOfMemoryError ex) {
        }
        return null;

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void onActivityCreate() {
        int numberOfCameras = Camera.getNumberOfCameras();// 获取摄像头个数
        if(numberOfCameras<1){
            Toast.makeText(getActivity(), "没有相机无法使用手势控制", Toast.LENGTH_SHORT).show();
            return;
        }
        System.out.println("@@@@@@@@MyObserver:ON_CREATE");
        handRecognition = new HandRecognition(this.getActivity().getAssets());
//        startBackgroundThread();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onActivityResume() {
        System.out.println("@@@@@@@@MyObserver:ON_RESUME");
//        startBackgroundThread();
//
//        // When the screen is turned off and turned back on, the SurfaceTexture is already
//        // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
//        // a camera and start preview from here (otherwise, we wait until the surface is ready in
//        // the SurfaceTextureListener).
//        if (textureView.isAvailable()) {
//            openCamera(textureView.getWidth(), textureView.getHeight());
//        } else {
//            textureView.setSurfaceTextureListener(surfaceTextureListener);
//        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onActivityPause() {
        System.out.println("@@@@@@@@MyObserver:ON_PAUSE");

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onActivityStop() {
        System.out.println("@@@@@@@@MyObserver:ON_STOP");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onActivityDestroy() {
        System.out.println("@@@@@@@@MyObserver:ON_DESTROY");

    }


    private static final String HANDLE_THREAD_NAME = "CameraBackground";
    private static ImageView imageview_instance;
    private static final int PERMISSIONS_REQUEST_CODE = 1;
    private final Object lock = new Object();
    private boolean runClassifier = false;
    private boolean checkedPermissions = false;
    private TextView textView;
    private long lastProcessingTimeMss;
    private HandRecognition handRecognition;
    /** Max preview width that is guaranteed by Camera2 API */
    private static final int MAX_PREVIEW_WIDTH = 1920;

    /** Max preview height that is guaranteed by Camera2 API */
    private static final int MAX_PREVIEW_HEIGHT = 1080;

//    /**
//     * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a {@link
//     * TextureView}.
//     */
//    private final TextureView.SurfaceTextureListener surfaceTextureListener =
//            new TextureView.SurfaceTextureListener() {
//
//                @Override
//                public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
//                    openCamera(width, height);
//                }
//
//                @Override
//                public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
////                    configureTransform(width, height);
//                }
//
//                @Override
//                public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
//                    return true;
//                }
//
//                @Override
//                public void onSurfaceTextureUpdated(SurfaceTexture texture) {
//                }
//            };

    /** ID of the current {@link CameraDevice}. */
    private String cameraId;

    /** An {@link AutoFitTextureView} for camera preview. */
    private TextureView textureView;

    private Camera mCamera;

    /** A {@link CameraCaptureSession } for camera preview. */
    private CameraCaptureSession captureSession;

    /** A reference to the opened {@link CameraDevice}. */
    private CameraDevice cameraDevice;

    /** The {@link Size} of camera preview. */
    private Size previewSize;

    /** {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its state. */
    private final CameraDevice.StateCallback stateCallback =
            new CameraDevice.StateCallback() {

                @Override
                public void onOpened(@NonNull CameraDevice currentCameraDevice) {
                    // This method is called when the camera is opened.  We start camera preview here.
                    cameraOpenCloseLock.release();
                    cameraDevice = currentCameraDevice;
                    createCameraPreviewSession();
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice currentCameraDevice) {
                    cameraOpenCloseLock.release();
                    currentCameraDevice.close();
                    cameraDevice = null;
                }

                @Override
                public void onError(@NonNull CameraDevice currentCameraDevice, int error) {
                    cameraOpenCloseLock.release();
                    currentCameraDevice.close();
                    cameraDevice = null;
                    Activity activity = getActivity();
                    if (null != activity) {
                        activity.finish();
                    }
                }
            };

    /** An additional thread for running tasks that shouldn't block the UI. */
    private HandlerThread backgroundThread;

    /** A {@link Handler} for running tasks in the background. */
    private Handler backgroundHandler;

    /** An {@link ImageReader} that handles image capture. */
    private ImageReader imageReader;

    /** {@link CaptureRequest.Builder} for the camera preview */
    private CaptureRequest.Builder previewRequestBuilder;

    /** {@link CaptureRequest} generated by {@link #previewRequestBuilder} */
    private CaptureRequest previewRequest;

    /** A {@link Semaphore} to prevent the app from exiting before closing the camera. */
    private Semaphore cameraOpenCloseLock = new Semaphore(1);

    /** A {@link CameraCaptureSession.CaptureCallback} that handles events related to capture. */
    private CameraCaptureSession.CaptureCallback captureCallback =
            new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureProgressed(
                        @NonNull CameraCaptureSession session,
                        @NonNull CaptureRequest request,
                        @NonNull CaptureResult partialResult) {
                }

                @Override
                public void onCaptureCompleted(
                        @NonNull CameraCaptureSession session,
                        @NonNull CaptureRequest request,
                        @NonNull TotalCaptureResult result) {
                }
            };

    /**
     * Shows a {@link Toast} on the UI thread for the classification results.
     *
     * @param text The message to show
     */
    private void showToast(final String text) {
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            textView.setText(text);
                        }
                    });
        }
    }


//    /** Opens the camera specified by {@link }. */
//    private void openCamera(int width, int height) {
////    if (!checkedPermissions && !allPermissionsGranted()) {
////      FragmentCompat.requestPermissions(this, getRequiredPermissions(), PERMISSIONS_REQUEST_CODE);
////      return;
////    } else {
////      checkedPermissions = true;
////    }
//        PermissionsUtil.requestPermission(MyApp.getContext(), new PermissionListener() {
//            @Override
//            public void permissionGranted(@NonNull String[] permissions) {
//                checkedPermissions = true;
//                Toast.makeText(MyApp.getContext(), "访问摄像头", Toast.LENGTH_LONG).show();
//            }
//
//            @Override
//            public void permissionDenied(@NonNull String[] permissions) {
//                checkedPermissions = false;
//                Toast.makeText(MyApp.getContext(), "用户拒绝了访问摄像头", Toast.LENGTH_LONG).show();
//            }
//        }, Manifest.permission.CAMERA);
//        if (!checkedPermissions) {
//            requestReadContact();
//            return;
//        }
////        setUpCameraOutputs(width, height);
//        configureTransform(width, height);
//        Activity activity = getActivity();
//        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
//        try {
//            if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
//                throw new RuntimeException("Time out waiting to lock camera opening.");
//            }
//            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//                // TODO: Consider calling
//                //    ActivityCompat#requestPermissions
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for ActivityCompat#requestPermissions for more details.
//                return;
//            }
//            manager.openCamera(cameraId, stateCallback, backgroundHandler);
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
//        }
//    }


//    /** Closes the current {@link CameraDevice}. */
//    private void closeCamera() {
//        try {
//            cameraOpenCloseLock.acquire();
//            if (null != captureSession) {
//                captureSession.close();
//                captureSession = null;
//            }
//            if (null != cameraDevice) {
//                cameraDevice.close();
//                cameraDevice = null;
//            }
//            if (null != imageReader) {
//                imageReader.close();
//                imageReader = null;
//            }
//        } catch (InterruptedException e) {
//            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
//        } finally {
//            cameraOpenCloseLock.release();
//        }
//    }

//    /** Starts a background thread and its {@link Handler}. */
//    private void startBackgroundThread() {
//        backgroundThread = new HandlerThread(HANDLE_THREAD_NAME);
//        backgroundThread.start();
//        backgroundHandler = new Handler(backgroundThread.getLooper());
//        synchronized (lock) {
//            runClassifier = true;
//        }
//        backgroundHandler.post(periodicClassify);
//    }
//
//    /** Stops the background thread and its {@link Handler}. */
//    private void stopBackgroundThread() {
//        backgroundThread.quitSafely();
//        try {
//            backgroundThread.join();
//            backgroundThread = null;
//            backgroundHandler = null;
//            synchronized (lock) {
//                runClassifier = false;
//            }
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }

//    /** Takes photos and classify them periodically. */
//    private Runnable periodicClassify =
//            new Runnable() {
//                @Override
//                public void run() {
//                    synchronized (lock) {
//                        if (runClassifier) {
//                            RunRecognition();
//                        }
//                    }
//                    backgroundHandler.post(periodicClassify);
//                }
//            };

    /** Creates a new {@link CameraCaptureSession} for camera preview. */
    private void createCameraPreviewSession() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;

            // We configure the size of default buffer to be the size of camera preview we want.
            texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());

            // This is the output Surface we need to start preview.
            Surface surface = new Surface(texture);

            // We set up a CaptureRequest.Builder with the output Surface.
            previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewRequestBuilder.addTarget(surface);

            // Here, we create a CameraCaptureSession for camera preview.
            cameraDevice.createCaptureSession(
                    Arrays.asList(surface),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            // The camera is already closed
                            if (null == cameraDevice) {
                                return;
                            }

                            // When the session is ready, we start displaying the preview.
                            captureSession = cameraCaptureSession;
                            try {
                                // Auto focus should be continuous for camera preview.
                                previewRequestBuilder.set(
                                        CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

                                // Finally, we start displaying the camera preview.
                                previewRequest = previewRequestBuilder.build();
                                captureSession.setRepeatingRequest(
                                        previewRequest, captureCallback, backgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                            showToast("Failed");
                        }
                    },
                    null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void RunRecognition(Bitmap bitmap) {

        if (bitmap == null) {
            IsRun=false;
            return;
        }

        final long startTime = SystemClock.uptimeMillis();
        handRecognition.RunHandRecognition(bitmap);
//        if (handRecognition.Pensonlocation.size() > 0) {
//
//        } else {
//            Log.e("Result:: ", "Pensonlocation : " + handRecognition.Pensonlocation.size());
//        }
        Log.e("Result:: ", "Pensonlocation : " + handRecognition.Pensonlocation.size());
        long lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;
        for (int i = 0; i < handRecognition.DetectorPensonInfoList.size(); i++) {

            ArrayList<ArrayList> DetectorInfoList=handRecognition.DetectorPensonInfoList.get(i);
            String Gesture1="",Gesture2="";
            for (int j = 0; j < DetectorInfoList.size(); j++) {
                ArrayList<Object> Info=DetectorInfoList.get(j);
                if(Info.size()>=2){
                    String name=(String)Info.get(1);
                    if(name!="face"&&Gesture1==""){
                        Gesture1=name;
                    }
                    if(name!="face"&&Gesture2==""){
                        Gesture2=name;
                    }
                    Log.e("Result:: ",name + " : " + lastProcessingTimeMs);
                }
            }
            GestureResult(Gesture1,Gesture2);

        }
        IsRun=false;
    }
    public void GestureResult(String Gesture1,String Gesture2){
        if((Gesture1=="yes"&&Gesture2=="five")||(Gesture1=="five"&&Gesture2=="yes")){
            //
            EventBus.getDefault().post(EventGesture.newBuilder().gestureResult("音量增加").build());
        }
        if((Gesture1=="ok"&&Gesture2=="five")||(Gesture1=="five"&&Gesture2=="ok")){
            //
            EventBus.getDefault().post(EventGesture.newBuilder().gestureResult("音量减小").build());
        }
        if((Gesture1=="ok"&&Gesture2=="yes")||(Gesture1=="yes"&&Gesture2=="ok")){
            //退出App
            EventBus.getDefault().post(EventGesture.newBuilder().gestureResult("退出App").build());
        }
        if((Gesture1=="stop"||Gesture2=="stop")){
            //stop
            EventBus.getDefault().post(EventGesture.newBuilder().gestureResult("停止播放").build());
        }
        if((Gesture1=="ok"&&Gesture2=="ok")){
            //播放开始
            EventBus.getDefault().post(EventGesture.newBuilder().gestureResult("播放开始").build());
        }
        if((Gesture1=="yes"&&Gesture2=="yes")){
            //后退
            EventBus.getDefault().post(EventGesture.newBuilder().gestureResult("后退").build());
        }
        if((Gesture1=="five"&&Gesture2=="five")){
            //视频镜像
            EventBus.getDefault().post(EventGesture.newBuilder().gestureResult("视频镜像").build());
        }
    }
//    /**
//     * Configures the necessary {@link Matrix} transformation to `textureView`. This
//     * method should be called after the camera preview size is determined in setUpCameraOutputs and
//     * also the size of `textureView` is fixed.
//     *
//     * @param viewWidth The width of `textureView`
//     * @param viewHeight The height of `textureView`
//     */
//    private void configureTransform(int viewWidth, int viewHeight) {
//        Activity activity = getActivity();
//        if (null == textureView || null == previewSize || null == activity) {
//            return;
//        }
//        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
//        Matrix matrix = new Matrix();
//        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
//        RectF bufferRect = new RectF(0, 0, previewSize.getHeight(), previewSize.getWidth());
//        float centerX = viewRect.centerX();
//        float centerY = viewRect.centerY();
//        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
//            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
//            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
//            float scale =
//                    Math.max(
//                            (float) viewHeight / previewSize.getHeight(),
//                            (float) viewWidth / previewSize.getWidth());
//            matrix.postScale(scale, scale, centerX, centerY);
//            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
//        } else if (Surface.ROTATION_180 == rotation) {
//            matrix.postRotate(180, centerX, centerY);
//        }
//        textureView.setTransform(matrix);
//    }
//    String Direction="";
//    private void RunRecognition() {
//        if (MyApp.getContext() == null && cameraDevice == null) {
//            showToast("未初始化的分类器或无效的上下文.");
//            return;
//        }
//        Bitmap bitmap = textureView.getBitmap();
//        if (bitmap == null) {
//            return;
//        }
//
//        final long startTime = SystemClock.uptimeMillis();
//
//        Bitmap bitmaps = Bitmap.createBitmap(bitmap);
//        final Canvas canvas = new Canvas(bitmaps);
//        final Paint paint = new Paint();
//        paint.setColor(Color.RED);
//        paint.setStyle(Paint.Style.STROKE);
//        paint.setStrokeWidth(10.0f);
//        handRecognition.RunHandRecognition(bitmap);
//        long lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;
//        for(int i=0;i<handRecognition.Pensonlocation.size();i++){
//            canvas.drawRect(handRecognition.Pensonlocation.get(i), paint);
//        }
//        for(int i=0;i<handRecognition.HandlocationList.size();i++){
//
//            canvas.drawRect(handRecognition.HandlocationList.get(i), paint);
//            paint.setTextSize(100);
//            canvas.drawText(handRecognition.HandNameList.get(i),handRecognition.HandlocationList.get(i).centerX(),handRecognition.HandlocationList.get(i).centerY(),paint);
//        }
//        if (handRecognition.HandNameList != null && !handRecognition.HandNameList.isEmpty()) {
//            Log.i(tag,"EventBus resultList结果："+ AppUtils.getResultString(handRecognition.HandNameList));
//            EventBus.getDefault().post(EventGesture.newBuilder().gestureResult(handRecognition.HandNameList).build());
//        }
//        if (imageview_instance != null) {
//            imageview_instance.post(new Runnable() {
//                @Override
//                public void run() {
//                    imageview_instance.setImageBitmap(bitmaps);
//                }
//            });
//
//        }
//        bitmap.recycle();
//    }
    /** Compares two {@code Size}s based on their areas. */
    private static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum(
                    (long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
        }
    }


    private void requestReadContact() {
        PermissionsUtil.TipInfo tip = new PermissionsUtil.TipInfo("注意:", "我就是想看下你的帅不帅", "不让看", "打开权限");
        PermissionsUtil.requestPermission(MyApp.getContext(), new PermissionListener() {
            @Override
            public void permissionGranted(@NonNull String[] permissions) {
//                JSONArray arr = null;
//                try {
//                    arr = getContactInfo(MainActivity.this);
//                    if (arr.length() == 0) {
//                        Toast.makeText(MainActivity.this, "请确认通讯录不为空且有访问权限", Toast.LENGTH_LONG).show();
//                    } else {
//                        Toast.makeText(MainActivity.this, arr.toString(), Toast.LENGTH_LONG).show();
//                    }
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
                Toast.makeText(MyApp.getContext(), "您成功授权摄像头权限", Toast.LENGTH_LONG).show();
            }

            @Override
            public void permissionDenied(@NonNull String[] permissions) {
                Toast.makeText(MyApp.getContext(), "您拒绝了摄像头权限", Toast.LENGTH_LONG).show();
            }
        }, new String[]{Manifest.permission.CAMERA}, true, tip);
    }

    private Activity getActivity() {
        return (Activity)mContext;
    }


    private void chechPermissions() {
        PermissionsUtil.requestPermission(MyApp.getContext(), new PermissionListener() {
            @Override
            public void permissionGranted(@NonNull String[] permissions) {
                checkedPermissions = true;
                Toast.makeText(MyApp.getContext(), "访问摄像头", Toast.LENGTH_LONG).show();
            }

            @Override
            public void permissionDenied(@NonNull String[] permissions) {
                checkedPermissions = false;
                Toast.makeText(MyApp.getContext(), "用户拒绝了访问摄像头", Toast.LENGTH_LONG).show();
            }
        }, Manifest.permission.CAMERA);
        if (!checkedPermissions) {
            requestReadContact();
            return;
        }
    }

}
