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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
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
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class GestureView extends FrameLayout implements LifecycleObserver {

    private static final String TAG = GestureView.class.getSimpleName();

    private static final String tag = "GeustureDebugTag";

    Context mContext;

    View rootView;

    public GestureView(Context context) {
        super(context);
        this.mContext = context;
        init();
    }

    public GestureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        init();
    }

    public GestureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
//        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.AiyigePlayerView);
//        ta.recycle();
        init();
    }

    private void init() {
        rootView = LayoutInflater.from(getContext()).inflate(R.layout.gesture_view, this, true);
        textureView = rootView.findViewById(R.id.texture);
        imageview_instance = (ImageView) rootView.findViewById(R.id.imageView);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void onActivityCreate() {
        System.out.println("@@@@@@@@MyObserver:ON_CREATE");
        handRecognition = new HandRecognition(this.getActivity().getAssets());
        startBackgroundThread();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onActivityResume() {
        System.out.println("@@@@@@@@MyObserver:ON_RESUME");
        startBackgroundThread();

        // When the screen is turned off and turned back on, the SurfaceTexture is already
        // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
        // a camera and start preview from here (otherwise, we wait until the surface is ready in
        // the SurfaceTextureListener).
        if (textureView.isAvailable()) {
            openCamera(textureView.getWidth(), textureView.getHeight());
        } else {
            textureView.setSurfaceTextureListener(surfaceTextureListener);
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onActivityPause() {
        System.out.println("@@@@@@@@MyObserver:ON_PAUSE");
        closeCamera();
        stopBackgroundThread();

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

    /**
     * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a {@link
     * TextureView}.
     */
    private final TextureView.SurfaceTextureListener surfaceTextureListener =
            new TextureView.SurfaceTextureListener() {

                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
                    openCamera(width, height);
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
                    configureTransform(width, height);
                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
                    return true;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture texture) {
                }
            };

    /** ID of the current {@link CameraDevice}. */
    private String cameraId;

    /** An {@link AutoFitTextureView} for camera preview. */
    private AutoFitTextureView textureView;

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

    /**
     * Resizes image.
     *
     * Attempting to use too large a preview size could  exceed the camera bus' bandwidth limitation,
     * resulting in gorgeous previews but the storage of garbage capture data.
     *
     * Given {@code choices} of {@code Size}s supported by a camera, choose the smallest one that is
     * at least as large as the respective texture view size, and that is at most as large as the
     * respective max size, and whose aspect ratio matches with the specified value. If such size
     * doesn't exist, choose the largest one that is at most as large as the respective max size, and
     * whose aspect ratio matches with the specified value.
     *
     * @param choices The list of sizes that the camera supports for the intended output class
     * @param textureViewWidth The width of the texture view relative to sensor coordinate
     * @param textureViewHeight The height of the texture view relative to sensor coordinate
     * @param maxWidth The maximum width that can be chosen
     * @param maxHeight The maximum height that can be chosen
     * @param aspectRatio The aspect ratio
     * @return The optimal {@code Size}, or an arbitrary one if none were big enough
     */
    private static Size chooseOptimalSize(
            Size[] choices,
            int textureViewWidth,
            int textureViewHeight,
            int maxWidth,
            int maxHeight,
            Size aspectRatio) {

        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        // Collect the supported resolutions that are smaller than the preview Surface
        List<Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getWidth() <= maxWidth
                    && option.getHeight() <= maxHeight
                    && option.getHeight() == option.getWidth() * h / w) {
                if (option.getWidth() >= textureViewWidth && option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }

        // Pick the smallest of those big enough. If there is no one big enough, pick the
        // largest of those not big enough.
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new CompareSizesByArea());
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    public static Camera2BasicFragment newInstance() {
        return new Camera2BasicFragment();
    }


    /**
     * Sets up member variables related to camera.
     *
     * @param width The width of available size for camera preview
     * @param height The height of available size for camera preview
     */
    private void setUpCameraOutputs(int width, int height) {
        Activity activity = getActivity();
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

                // We don't use a front facing camera in this sample.
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }

                StreamConfigurationMap map =
                        characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) {
                    continue;
                }

                // // For still image captures, we use the largest available size.
                Size largest =
                        Collections.max(
                                Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), new CompareSizesByArea());
                imageReader =
                        ImageReader.newInstance(
                                largest.getWidth(), largest.getHeight(), ImageFormat.JPEG, /*maxImages*/ 2);

                // Find out if we need to swap dimension to get the preview size relative to sensor
                // coordinate.
                int displayRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
                // noinspection ConstantConditions
                /* Orientation of the camera sensor */
                int sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                boolean swappedDimensions = false;
                switch (displayRotation) {
                    case Surface.ROTATION_0:
                    case Surface.ROTATION_180:
                        if (sensorOrientation == 90 || sensorOrientation == 270) {
                            swappedDimensions = true;
                        }
                        break;
                    case Surface.ROTATION_90:
                    case Surface.ROTATION_270:
                        if (sensorOrientation == 0 || sensorOrientation == 180) {
                            swappedDimensions = true;
                        }
                        break;
                    default:
                        Log.e(TAG, "Display rotation is invalid: " + displayRotation);
                }

                Point displaySize = new Point();
                activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
                int rotatedPreviewWidth = width;
                int rotatedPreviewHeight = height;
                int maxPreviewWidth = displaySize.x;
                int maxPreviewHeight = displaySize.y;

                if (swappedDimensions) {
                    rotatedPreviewWidth = height;
                    rotatedPreviewHeight = width;
                    maxPreviewWidth = displaySize.y;
                    maxPreviewHeight = displaySize.x;
                }

                if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                    maxPreviewWidth = MAX_PREVIEW_WIDTH;
                }

                if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                    maxPreviewHeight = MAX_PREVIEW_HEIGHT;
                }

                previewSize =
                        chooseOptimalSize(
                                map.getOutputSizes(SurfaceTexture.class),
                                rotatedPreviewWidth,
                                rotatedPreviewHeight,
                                maxPreviewWidth,
                                maxPreviewHeight,
                                largest);

                // We fit the aspect ratio of TextureView to the size of preview we picked.
                int orientation = getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    textureView.setAspectRatio(previewSize.getWidth(), previewSize.getHeight());
                } else {
                    textureView.setAspectRatio(previewSize.getHeight(), previewSize.getWidth());
                }

                this.cameraId = "1";
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
            //ErrorDialog.newInstance(getString(R.string.camera_error))
            //.show(getChildFragmentManager(), FRAGMENT_DIALOG);
        }
    }

    /** Opens the camera specified by {@link }. */
    private void openCamera(int width, int height) {
//    if (!checkedPermissions && !allPermissionsGranted()) {
//      FragmentCompat.requestPermissions(this, getRequiredPermissions(), PERMISSIONS_REQUEST_CODE);
//      return;
//    } else {
//      checkedPermissions = true;
//    }
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
        setUpCameraOutputs(width, height);
        configureTransform(width, height);
        Activity activity = getActivity();
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            manager.openCamera(cameraId, stateCallback, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
        }
    }


    /** Closes the current {@link CameraDevice}. */
    private void closeCamera() {
        try {
            cameraOpenCloseLock.acquire();
            if (null != captureSession) {
                captureSession.close();
                captureSession = null;
            }
            if (null != cameraDevice) {
                cameraDevice.close();
                cameraDevice = null;
            }
            if (null != imageReader) {
                imageReader.close();
                imageReader = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            cameraOpenCloseLock.release();
        }
    }

    /** Starts a background thread and its {@link Handler}. */
    private void startBackgroundThread() {
        backgroundThread = new HandlerThread(HANDLE_THREAD_NAME);
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
        synchronized (lock) {
            runClassifier = true;
        }
        backgroundHandler.post(periodicClassify);
    }

    /** Stops the background thread and its {@link Handler}. */
    private void stopBackgroundThread() {
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
            backgroundThread = null;
            backgroundHandler = null;
            synchronized (lock) {
                runClassifier = false;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /** Takes photos and classify them periodically. */
    private Runnable periodicClassify =
            new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (runClassifier) {
                            RunRecognition();
                        }
                    }
                    backgroundHandler.post(periodicClassify);
                }
            };

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

    /**
     * Configures the necessary {@link Matrix} transformation to `textureView`. This
     * method should be called after the camera preview size is determined in setUpCameraOutputs and
     * also the size of `textureView` is fixed.
     *
     * @param viewWidth The width of `textureView`
     * @param viewHeight The height of `textureView`
     */
    private void configureTransform(int viewWidth, int viewHeight) {
        Activity activity = getActivity();
        if (null == textureView || null == previewSize || null == activity) {
            return;
        }
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, previewSize.getHeight(), previewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale =
                    Math.max(
                            (float) viewHeight / previewSize.getHeight(),
                            (float) viewWidth / previewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        textureView.setTransform(matrix);
    }
    String Direction="";
    private void RunRecognition() {
        if (MyApp.getContext() == null && cameraDevice == null) {
            showToast("未初始化的分类器或无效的上下文.");
            return;
        }
        Bitmap bitmap = textureView.getBitmap();
        if (bitmap == null) {
            return;
        }

        final long startTime = SystemClock.uptimeMillis();

        Bitmap bitmaps = Bitmap.createBitmap(bitmap);
        final Canvas canvas = new Canvas(bitmaps);
        final Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10.0f);
        handRecognition.RunHandRecognition(bitmap);
        long lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;
        for(int i=0;i<handRecognition.Pensonlocation.size();i++){
            canvas.drawRect(handRecognition.Pensonlocation.get(i), paint);
        }
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
        if (imageview_instance != null) {
            imageview_instance.post(new Runnable() {
                @Override
                public void run() {
                    imageview_instance.setImageBitmap(bitmaps);
                }
            });

        }
        bitmap.recycle();
    }
    /** Compares two {@code Size}s based on their areas. */
    private static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum(
                    (long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
        }
    }

    /** Shows an error message dialog. */
    public static class ErrorDialog extends DialogFragment {

        private static final String ARG_MESSAGE = "message";

        public static Camera2BasicFragment.ErrorDialog newInstance(String message) {
            Camera2BasicFragment.ErrorDialog dialog = new Camera2BasicFragment.ErrorDialog();
            Bundle args = new Bundle();
            args.putString(ARG_MESSAGE, message);
            dialog.setArguments(args);
            return dialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Activity activity = getActivity();
            return new AlertDialog.Builder(activity)
                    .setMessage(getArguments().getString(ARG_MESSAGE))
                    .setPositiveButton(
                            android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    activity.finish();
                                }
                            })
                    .create();
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

}
