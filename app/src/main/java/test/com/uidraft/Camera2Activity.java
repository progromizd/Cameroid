package test.com.uidraft;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.GradientDrawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.InputConfiguration;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.renderscript.Allocation;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.util.SizeF;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import test.com.uidraft.abstractus.DF;
import test.com.uidraft.ui.elements.FlashMode;
import test.com.uidraft.ui.elements.PVMode;
import test.com.uidraft.ui.elements.SwapButton;

//import test.com.sunray.Animations.Effects;
//import test.com.sunray.UI.Controls;

//import android.support.v4.content.PermissionChecker; TODO set maximum possile target sdk//
//TODO or request permission overrride
//time = 11:04

//TODO fix orientation on shoot moment
//TODO FIX focus lock + learn out logs to see whats going on

/**
 * Created by user on 30.04.16.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class Camera2Activity extends Activity implements FlashMode.onResult, PVMode.fromPVMode, SwapButton.fromSwapButton{

    private static boolean swapping = false;

    private boolean bCancelRecord = false;
    private boolean mIsFrontCamera = false;

//    private static String IMAGE_LOCATION_FOR_INTENT = "IMAGE_LOCATION";
  //  private static String VIDEO_LOCATION_FOR_INTENT = "video_path";

    private static String TEMPORARY_IMAGE_LOCATION = "TEMPORARY_IMAGE_LOCATION";
    private static String FINAL_IMAGE_LOCATION = "FINAL_IMAGE_LOCATION";
    private static String FINAL_VIDE_LOCATION = "FINAL_VIDE_LOCATION";

    private static int REQUEST_CAMERA_PERMISSION_RESULT = 0;
    private static int REQUEST_WRTIE_EXTERNAL_STORAGE_PERMISSION = 1;
    private static final int STATE_PREVIEW = 0;
    private static final int STAT_WAIT_LOCK = 1;
    private int mCaptureState = STATE_PREVIEW;

    private int mCurrentRotation = 0;

    private static SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }

    private int flashMode = 0;
    private static final int FLASH_DISBALED = 0;
    private static final int FLASH_ENABLED = 1;
    private static final int FLASH_AUTO = 2;
    private Context context;
    private FlashMode fm;
    private SwapButton sb;
    private PVMode pv;

    @Override
    public void onDFresult(DF.DFObject dfObject) {

    }

    @Override
    public void onRecordStart() {
        startRecord();
    }

    @Override
    public void onRecordStop() {
        stopRecordingVideo();
    }

    @Override
    public void onTakePhoto() {
        //captureStillImage();
        //new captureAsync().execute();
        //captureStillImageZeroShutter();
        captureStillImageNew();
        //makeNewSession();
    }

    @Override
    public void onSwapCamera() {
        if (swapping) return;
        swapping = true;
        if (mCameraDevice != null) {
            mCameraDevice.close();
        }
        if (mPreviewCaptureSession != null) {
            mPreviewCaptureSession.close();
            //mPreviewCaptureSession.stopRepeating();
        }

        mIsFrontCamera = !mIsFrontCamera;
        setupCamera(mTextureView.getWidth(), mTextureView.getHeight(), mIsFrontCamera);
        //Effects.fade(iv_fade, 0.0f, 2.0f);
        //Effects.rotate_swap(v);
        //Effects.blinkSwapDot(iv_swap_camera_hole, iv_swap_camera_dot, mIsFrontCamera, 20);
        connectCamera();
    }

    @Override
    public void onDeletePhotoClick() {
        //TODO delete file
        startPreview();
    }


    private static class CompareSizeByArea implements Comparator<Size>{

        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long)lhs.getWidth() * lhs.getHeight() /
                    (long)rhs.getWidth() * rhs.getHeight());
        }
    }

    static class DO {
        public static final int SWAP_CAMERA = 0;
        public static final int OR_RECORD_DOWN = 1;
    }

    //==================TODO VIEW ID SCOPE
    private ImageView iv_fade;
    private ImageView iv_swap_camera_dot;
    private ImageView iv_swap_camera_hole;

    private ImageView iv_fade_effect_prv;


    //==============================

    private Button mBqrotateTest;
    private Chronometer mChronometer;
    private CameraManager mCameraManager;
    private Size mPreviewSize;
    private Size mVideoSize;

    private Size mImageSize;
    private ImageReader mImageReader;

    private MediaRecorder mMediaRecorder;

    private CameraCaptureSession mCameraCaptureSession;
    private CameraCaptureSession mPreviewCaptureSession;
    private CameraCaptureSession.CaptureCallback mPreviewCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        private void process(CaptureResult captureResult){
            switch (mCaptureState){
                case STATE_PREVIEW:
                    Log.i("TEST", "mPreviewCaptureCallback STATE_PREVIEW");
                    //Toast.makeText(getApplicationContext(), "STATE_PREVIEW", Toast.LENGTH_SHORT).show();
                    break;
                case STAT_WAIT_LOCK:
                    Log.i("TEST", "mPreviewCaptureCallback STAT_WAIT_LOCK before get AF state");

                    Integer afState = captureResult.get(CaptureResult.CONTROL_AF_STATE);
                    if (afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED){

                        //unlockFocus();
                        //Toast.makeText(getApplicationContext(), "AF Locked!", Toast.LENGTH_SHORT).show();
                        Log.i("TEST", "mPreviewCaptureCallback FOCUSED_LOCKEDe");
                        Log.i("TEST", "mPreviewCaptureCallback captureStillImage();");
                        captureStillImage();
                        Log.i("TEST", "mPreviewCaptureCallback after captureStillImage();");
                    }
                    else {
                        Log.i("TEST", "mPreviewCaptureCallback STAT_WAIT_LOCK but af state = " + afState.toString());
                    }

                    break;
            }
        }
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            Log.i("TEST", "onCaptureCompleted before process(result);");
            process(result);
            Log.i("TEST", "onCaptureCompleted after process(result);");
        }
        @Override
        public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request, CaptureFailure failure){
            super.onCaptureFailed(session, request, failure);
            Log.i("TEST", "onCaptureFailed");
            //Toast.makeText(getApplicationContext(), "Focus lock failed!", Toast.LENGTH_SHORT).show();
        }

    };

    private int mTotalRotation;
    private CaptureRequest.Builder mCaptureRequestBuilder;
    private String mCamerId;
    private String mFrontCamerId;
    private CameraDevice mCameraDevice;
    private HandlerThread mBackgroundHandlerThread;
    private Handler mBackgroundHandler;
    private Button mStillImageButton;
    private ImageButton mRecordImageButton;

    private boolean mIsRecording = false;

    private static String mImageFileName;
    private static String mTempImageFileName;
    private static File mImageFile;
    private static File mImageFolder;
    //private ImageReader mImageReader;
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener =
            new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader imageReader) {
                    mBackgroundHandler.post(new ImageSaver((imageReader.acquireNextImage())));
                }
            };

    private static class ImageSaver implements Runnable{

        private final Image mImage;

        private ImageSaver(Image image){
            mImage = image;
        }

        @Override
        public void run() {
            boolean skip = true;;
            if (!skip) {
                long start = System.currentTimeMillis();
                ByteBuffer byteBuffer = mImage.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[byteBuffer.remaining()];
                byteBuffer.get(bytes);

                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = new FileOutputStream(mImageFile);
                    fileOutputStream.write(bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    mImage.close();
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                long dif = System.currentTimeMillis() - start;
                Log.i("q2", "run: time = " + dif);
            }
        }
    }


    private File mGalleryFolder;


    private File mVideoFolder;
    private String mVideoFileName;

    private CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onClosed(CameraDevice camera) {
            super.onClosed(camera);
            Log.i("TAG", "onClosed: CLOSED!!!!");
        }

        @Override
        public void onOpened(CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            if (mIsRecording){
                try {
                    createVideoFileName();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                startRecord();
                mMediaRecorder.start();

            }else{
                startPreview();
                //startPreviewNew();
            }
            //startPreview();

            //Toast.makeText(getApplicationContext(), "Camera connected", Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            mCameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(CameraDevice cameraDevice, int i) {
            if (mCameraDevice!= null) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
        }
    };
    private TextureView mTextureView;
    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {

            Log.i("LOG", "onSurfaceTextureAvailable: w = " + width + " H = "+ height);
            //TODO check sizes
            setupCamera(width, height, mIsFrontCamera);
            //setupCamera(720, 1230, mIsFrontCamera);

//            setupCamera(Math.min(MainActivity.sh, MainActivity.sw),
//                    Math.max(MainActivity.sh, MainActivity.sw), mIsFrontCamera);
            connectCamera();
            //Toast.makeText(getApplicationContext(), "texture is available", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
            //setupCamera(i,
//                    i1, mIsFrontCamera);
            //connectCamera();
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };


    private void unlockFocus(){
        mCaptureState = STATE_PREVIEW;
        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                CaptureRequest.CONTROL_AF_TRIGGER_CANCEL);
        try {
            Log.i("TEST", "try to capture in 'unlockFocus()'");
            mPreviewCaptureSession.capture(mCaptureRequestBuilder.build(), mPreviewCaptureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            Log.i("TEST", "ERROR trying to capture in 'unlockFocus()'");
        }
        catch (IllegalStateException e){
            //TODO show camera busy dialog
            //Log.i("")
        }
    }

    private void setOrientation(){
        OrientationEventListener oel = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int orientation) {
                //lastor = currentOrientation;
//            if (orientation == ORIENTATION_UNKNOWN) return;
//            android.hardware.Camera.CameraInfo info =
//                    new android.hardware.Camera.CameraInfo();
//            android.hardware.Camera.getCameraInfo(0, info);
//
//            orientation = (orientation + 45) / 90 * 90;
//            int rotation = 0;
//            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
//                rotation = (info.orientation - orientation + 360) % 360;
//            } else {  // back-facing camera
//                rotation = (info.orientation + orientation) % 360;
//            }
                //mBqrotateTest.setRotation(-orientation);
                //mBqrotateTest.setText("Angle = " + orientation);
                //preview.camera20Minus.getCamera().getParameters().setRotation(rotation);
                mCurrentRotation = orientation;
            }
        };
        oel.enable();//TODO disable when not needed
    }
    private int getDeviceRotation(){
        //TODO check in most possible order to speedup;
        int orientation = mCurrentRotation;
        if (orientation > 315 && orientation <= 360 || orientation >= 0 && orientation <= 45){
            return 90;//0
        }
        else {
            if (orientation > 45 && orientation <= 135){
                return 180;//90;
            }else {
                if (orientation > 135 && orientation <= 225){
                    return 270;//180;
                }else {
                    return 0;//270;
                }
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.ui_main);
        findViewById(R.id.fl_frame).setVisibility(View.GONE);
        createVideoFolder();
        createImageFolder();

        iv_fade = null;// ((ImageView) findViewById(R.id.iv_fade_effect_prv));

        mMediaRecorder = new MediaRecorder();

        mBqrotateTest = null;//((Button) findViewById(R.id.b_orient));
        setOrientation();

        mChronometer = ((Chronometer) findViewById(R.id.c_timer));
        mTextureView = ((TextureView) findViewById(R.id.tv_texture));
        //setContentView(R.layout.activity_main);
//        mStillImageButton = ((Button) findViewById(R.id.b_make_photo));
//        mStillImageButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Log.i("TEST", "onClick before lock focus");
//                //lockFocus();
//                captureStillImage();
//                Log.i("TEST", "onClick after lock focus");
//            }
//        });
//        mRecordImageButton = ((ImageButton) findViewById(R.id.imageButton));
//        mRecordImageButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (mIsRecording){
//                    //mIsRecording = false;
//                    //mRecordImageButton.setImageResource(R.mipmap.rotate);
//                    //stopBackgroundThread();
//                    //mMediaRecorder.stop();
//                    //mMediaRecorder.reset();
//                    //startPreview();
//                    stopRecordingVideo();
//                    mChronometer.stop();
//                    mChronometer.setVisibility(View.INVISIBLE);
//                    //TODO open player instead preview
//                    //startPreview();
//                    //setContentView(R.layout.video_player);
//                    runVideoPlayer();
//                }else{
//                    checkWritePermission();
//                }
//            }
//        });
    }



    @Override
    protected void onPause(){
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION_RESULT){
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(getApplicationContext(),
                        "Application will not run without camera services", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == REQUEST_WRTIE_EXTERNAL_STORAGE_PERMISSION){
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED){
                mIsRecording = true;
                //mRecordImageButton.setImageResource(R.mipmap.switch_camera);
                try {
                    createVideoFileName();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Toast.makeText(this, "Permission successfully granted!", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this,
                        "App needs to save video to run", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    protected void onResume(){
        super.onResume();
        startBackgroundThread();
        if (mTextureView == null)return;
        if (mTextureView.isAvailable()){
            setupCamera(mTextureView.getWidth(), mTextureView.getHeight(), mIsFrontCamera);
            connectCamera();
        }else{
            //mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);

        }
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus){
        super.onWindowFocusChanged(hasFocus);
        View decorView = getWindow().getDecorView();
        if (hasFocus){
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }

        //TODO check camera mode inside
        if (hasFocus){

            fm = new FlashMode(context);
            sb = new SwapButton(context);
            //photoVideoMode = new PhotoVideoMode(context);
            if (pv == null) pv = new PVMode((Activity) context);

        }
    }


    private void delayedRecord(){
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (!bCancelRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    checkWritePermission();
                }
            });
        }
    }


    private void setTouchListeners(final View v, final int actionId){
        v.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        //onDown(v, actionId);
                        break;
                    case MotionEvent.ACTION_UP:
                        doAction(v, actionId);
                        break;
                    case MotionEvent.ACTION_MOVE:

                        break;
                    case MotionEvent.ACTION_CANCEL:

                        break;
                }
                return true;
            }
        });
    }

    class TListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // TODO Auto-generated method stub
            return false;
        }

    }

    private class RecordOnHold extends AsyncTask<Void, Void, Void>{

        public volatile boolean cancel = false;
        @Override
        protected Void doInBackground(Void... voids) {
            for (int i = 0; i < 20; i ++){
                try {
                    Thread.sleep(10);
                    Log.i("LOCK_TEST", "waiting" + i);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            ///super.onPostExecute(aVoid);
            if (!cancel)checkWritePermission();
        }


    }
    private void doAction(View v, int id){
        switch (id){
            case DO.SWAP_CAMERA://qswap
                if (swapping) return;
                swapping = true;
                if (mCameraDevice != null) {
                    mCameraDevice.close();
                }
                if (mPreviewCaptureSession != null) {
                    mPreviewCaptureSession.close();
                    //mPreviewCaptureSession.stopRepeating();
                }

                mIsFrontCamera = !mIsFrontCamera;
                setupCamera(mTextureView.getWidth(), mTextureView.getHeight(), mIsFrontCamera);
                //Effects.fade(iv_fade, 0.0f, 2.0f);
                //Effects.rotate_swap(v);
                //Effects.blinkSwapDot(iv_swap_camera_hole, iv_swap_camera_dot, mIsFrontCamera, 20);
                connectCamera();
                //TODO test 1
                break;
            case DO.OR_RECORD_DOWN:

                break;
        }
    }
    private Size getPreferedPreviewSize(Size[] mapSizes, int width, int height){
        List<Size> allSizes = new ArrayList<>();
        for(Size option : mapSizes){
            if(width > height){
                if (option.getWidth() > width &&
                        option.getHeight() > height){
                    allSizes.add(option);
                }
            }else{
                if (option.getWidth() > height &&
                        option.getHeight() > width){
                    allSizes.add(option);
                }
            }
        }
        if (allSizes.size() > 0){
            return Collections.min(allSizes, new Comparator<Size>() {
                @Override
                public int compare(Size one, Size two) {
                    return Long.signum(one.getWidth() * one.getHeight() - two.getWidth() * two.getHeight());
                }
            });
        }
        return mapSizes[0];
    }

    public void  setupCamera(int width, int height, boolean frontFacing){
        int lastline = 0;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        Statica.sw = displayMetrics.widthPixels;
        Statica.sh = displayMetrics.heightPixels;

        try {
            mCameraManager = ((CameraManager) getSystemService(Context.CAMERA_SERVICE));
            for (String cameraId:mCameraManager.getCameraIdList()){
                CameraCharacteristics cc = mCameraManager.getCameraCharacteristics(cameraId);
                if (!frontFacing) {
                    if (cc.get(CameraCharacteristics.LENS_FACING) ==
                            CameraCharacteristics.LENS_FACING_FRONT) {
                        continue;
                    }
                }else {
                    if (cc.get(CameraCharacteristics.LENS_FACING) !=
                            CameraCharacteristics.LENS_FACING_FRONT) {
                        continue;
                    }
                }
                //
                // We only use a camera that supports RAW in this sample.

                //
                Object c = cc.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES);
                List list = cc.getAvailableCaptureRequestKeys();
                ArrayList descr = new ArrayList();
                for (int i = 0; i < list.size(); i++) {
                    descr.add(((android.hardware.camera2.CaptureRequest.Key) list.get(i)).getName());
                }
                //TODO swap rotation before record !!!!!!!!1

                int deviceOrientation = getWindowManager().getDefaultDisplay().getRotation();
                mTotalRotation = sensorToDeviceRotation(cc, deviceOrientation);
                boolean swapRotation = mTotalRotation == 90 || mTotalRotation == 270;
                int rotatedWidth = Statica.sh;// width;
                int rotatedHeight = Statica.sw;// height;
                if (swapRotation){
                    rotatedWidth = Statica.sw;//height;
                    rotatedHeight = Statica.sh;//width;
                }
                StreamConfigurationMap map = cc.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                Size s[] =  map.getOutputSizes(ImageFormat.YUV_420_888);
                Size largestSize = Collections.max(
                        Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                                new Comparator<Size>(){

                                    @Override
                                    public int compare(Size size, Size t1) {
                                        Log.i("PREVIEW_Q", "getOutputSizes: " + size.getWidth() +" x "+ size.getHeight());
                                        return Long.signum(size.getWidth() * size.getHeight() -
                                        t1.getWidth() * t1.getHeight());
                                    }
                                }
                        );

lastline = 735;
                Log.i("LOG", "setupCamera: preview q = " + largestSize.getWidth() + " x " + largestSize.getHeight());
                mPreviewSize = selectOptimalSize(map.getOutputSizes(SurfaceTexture.class), rotatedWidth, rotatedHeight);
                mVideoSize = selectOptimalSize(map.getOutputSizes(MediaRecorder.class), rotatedWidth, rotatedHeight);
                //TODO set for image capture
                lastline = 740;
                boolean swap = true;
                if (swap){
                    //Size s = new Size(100,50);
                    //mPreviewSize = s;
                    //mTextureView.getLayoutParams().height = mPreviewSize.getHeight();
                    mTextureView.getLayoutParams().width = Statica.sw;// 720;//mPreviewSize.getWidth();
                    float aspect = (float) mPreviewSize.getWidth() / mPreviewSize.getHeight();
                    int newH = Statica.sh;// (int) (720 * aspect);
                    mTextureView.getLayoutParams().height = newH;

                    mTextureView.requestLayout();
                    //mTextureView.setX(10);
                    //mTextureView.setY(100);
                }
                mImageReader = ImageReader.newInstance(largestSize.getWidth(),
                        largestSize.getHeight(), ImageFormat.JPEG, 1);

                mImageReader.setOnImageAvailableListener(mOnImageAvailableListener,
                        mBackgroundHandler);

                //mImageSize = selectOptimalSize(map.getOutputSizes(ImageFormat.JPEG), rotatedWidth, rotatedHeight);
                //mImageReader = ImageReader.newInstance(mImageSize.getWidth(), mImageSize.getHeight(), ImageFormat.JPEG, 1);
                //mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);
                mCamerId = cameraId;
                return;
            }
        }catch (CameraAccessException e){
            Log.e("SHIT", "setupCamera: CameraAccessException" + e.getMessage());
            e.printStackTrace();
        }
        catch (Exception z){
            z.printStackTrace();
            Log.e("SHIT", "setupCamera: Exception z W = "  + width + " H = " + height + "." + z.getMessage());

            Log.e("SHIT", "setupCamera: last line =" + lastline);
        }
    }

    private void y2j()
    {

    }

    private void connectCamera(){
        CameraManager cameraManager = ((CameraManager) getSystemService(Context.CAMERA_SERVICE));
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                //if (ContextCompat.)
                //TODO check for marshmallow
                //ContextCompat.checkSelfPermission
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED){
                    cameraManager.openCamera(mCamerId, mCameraDeviceStateCallback, mBackgroundHandler);
                }else{
                    //TODO check for permission
                    if (shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)){
                        Toast.makeText(this, "This app required access to CAMERA", Toast.LENGTH_LONG).show();
                    }
                    requestPermissions(new String[] {android.Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION_RESULT);
                }
            }
            else {
                cameraManager.openCamera(mCamerId, mCameraDeviceStateCallback, mBackgroundHandler);//TODO Id was null
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private void startRecord(){
        try {
            setupMediaRecorder();
            SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            Surface previewSurface = new Surface(surfaceTexture);
            Surface recordSurface = mMediaRecorder.getSurface();
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            mCaptureRequestBuilder.addTarget(previewSurface);
            mCaptureRequestBuilder.addTarget(recordSurface);

            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, recordSurface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession session) {
                            try {
                                mCameraCaptureSession = session;
                                session.setRepeatingRequest(
                                        mCaptureRequestBuilder.build(), null, null
                                );
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                            catch (IllegalStateException e){
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession session) {

                        }
                    }, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void runEditor(){
//        Intent editFileIntent = new Intent(this, ImageEditorActivity.class);
//        editFileIntent.putExtra(TEMPORARY_IMAGE_LOCATION, mTempImageFileName);
//        editFileIntent.putExtra(FINAL_IMAGE_LOCATION, mImageFileName);//TODO sett fina destinatino
//        startActivity(editFileIntent);
        //((ImageView) findViewById(R.id.iv_dest)).setBackgroundColor(Color.GREEN);
        //Bitmap b = BitmapFactory.decodeFile(mTempImageFileName);
        //((ImageView) findViewById(R.id.iv_dest)).setImageBitmap(b);
    }

    private void runVideoPlayer(){
//        Intent editFileIntent = new Intent(this, VideoPlayerActivity.class);
//        editFileIntent.putExtra(FINAL_VIDE_LOCATION, mVideoFileName);
//        startActivity(editFileIntent);
    }

    private void makeNewSession(){
//        try {
//            mPreviewCaptureSession.stopRepeating();//new
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }

        //TODO stop preveie and make new sesion
        Surface prev= new Surface(mTextureView.getSurfaceTexture());
        try {
            mCameraDevice.createCaptureSession(Arrays.asList(prev, mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                            mPreviewCaptureSession = cameraCaptureSession;
                            try {
                                //TODO fix illegal state EXCEPTION HERE COZ cameraCaptureSession can be closed
                                //cameraCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(), null, mBackgroundHandler);
                                captureStillImageNew();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            //                    if (iv_fade.getVisibility() == View.VISIBLE) {
                            //                        //Effects.fade(iv_fade, 1.0f, 0.0f);
                            //                    }
                            swapping = false;
                            //TODO preallocate buffer as attempt to speed up;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                try {
                                    mPreviewCaptureSession.prepare(mImageReader.getSurface());
                                } catch (CameraAccessException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                            //Toast.makeText(getApplicationContext(), "Unable to setup preview", Toast.LENGTH_LONG).show();

                        }
                    }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void captureStillImageNew(){

        //TODO ask permission if nessesaary
        final long start = System.currentTimeMillis();
        try {
            //Log.i("TEST", "createImageFileName()");
            mImageFile = createImageFileName();
        } catch (IOException e) {
            e.printStackTrace();
            //Log.i("TEST", "ERROR in createImageFileName()");
            return;
        }
        long newFile = System.currentTimeMillis() - start;
        //
        CaptureRequest.Builder stillBuilder = null;
        try {
            //TODO zero shutter new fi4a
            //stillBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_ZERO_SHUTTER_LAG);
            stillBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            stillBuilder.addTarget(mImageReader.getSurface());
            //int rotation = getWindowManager().getDefaultDisplay().getRotation();

            //TODO get rotation from sensor
            int rotation = getDeviceRotation();// mCurrentRotation; //getWindowManager().getDefaultDisplay().getRotation();
            //

            if (!mIsFrontCamera) {
                //TODO switch torch mode
                switch (flashMode) {
                    case FLASH_AUTO:
                        stillBuilder.set(CaptureRequest.FLASH_MODE,
                                CaptureRequest.FLASH_MODE_SINGLE);
                        break;
                    case FLASH_DISBALED:
                        stillBuilder.set(CaptureRequest.FLASH_MODE,
                                CaptureRequest.FLASH_MODE_OFF);
                        break;
                    case FLASH_ENABLED:
                        stillBuilder.set(CaptureRequest.FLASH_MODE,
                                CaptureRequest.FLASH_MODE_TORCH);
                        break;

                    //
                }
            }else {
                //TODO check
                stillBuilder.set(CaptureRequest.FLASH_MODE,
                        CaptureRequest.FLASH_MODE_OFF);
            }

            //TODO speed up
            boolean fast = true;
            if (fast) {
                stillBuilder.set(CaptureRequest.EDGE_MODE,
                        CaptureRequest.EDGE_MODE_OFF);
                stillBuilder.set(
                        CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE,
                        CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_ON);
                stillBuilder.set(
                        CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE,
                        CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE_OFF);
                stillBuilder.set(CaptureRequest.NOISE_REDUCTION_MODE,
                        CaptureRequest.NOISE_REDUCTION_MODE_OFF);
                stillBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                        CaptureRequest.CONTROL_AF_TRIGGER_CANCEL);

                stillBuilder.set(CaptureRequest.CONTROL_AE_LOCK, true);
                stillBuilder.set(CaptureRequest.CONTROL_AWB_LOCK, true);
            }
            //
            boolean maxQ = true;
            if (maxQ){
                stillBuilder.set(CaptureRequest.JPEG_QUALITY, (byte)90);
            }

            //stillBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

            if (mIsFrontCamera) {
                stillBuilder.set(CaptureRequest.JPEG_ORIENTATION, rotation);
            }else {
                //TODO change values
                stillBuilder.set(CaptureRequest.JPEG_ORIENTATION, rotation);
            }

            CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult) {
                    super.onCaptureProgressed(session, request, partialResult);
                    Log.i("Q1", "onCaptureProgressed: +");
                }

                @Override
                public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
                    super.onCaptureStarted(session, request, timestamp, frameNumber);
                    Log.i("Q1", "onCaptureStarted: +");
                }

                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    Log.i("Q1", "onCaptureCompleted: +");
                    //Log.i("TEST", "Before unlockFocus()");
                    //unlockFocus();
                    //mTextureView.setRotation(45.0f);
                    //mTextureView.setRotation(75.0f);
                    //Log.i("TEST", "After unlockFocus()");
                    long diff = System.currentTimeMillis() - start;
                    Log.i("PROF", "captureStillImage time = :" + diff + mImageFileName);
                    Toast.makeText(getApplicationContext(), "Image captured time = " + diff, Toast.LENGTH_SHORT).show();
                    runEditor();
                }
            };
            //TODO uncomment to use sync
            //mPreviewCaptureSession.stopRepeating();//new
            //CaptureRequest cr = stillBuilder.build();
            long beforeRq = System.currentTimeMillis() - start - newFile;
            Log.i("Q1", "before send req: +");
            //TODO replace null with handler to separate threDS!!!!!!!!!
            //mPreviewCaptureSession.capture(stillBuilder.build(), captureCallback, null);
            Log.i("Q1", "after send req: +");
            //mCameraCaptureSession.capture(stillBuilder.build(), captureCallback, null);
            mPreviewCaptureSession.capture(stillBuilder.build(), captureCallback, mBackgroundHandler);


        } catch (CameraAccessException e) {
            e.printStackTrace();
            Log.i("Q1", "CameraAccessException in captureStillImage()");
        }


    }

    private void startPreviewNew(){
        long start = System.currentTimeMillis();

        SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        Surface previewSurface = new Surface(surfaceTexture);
        try {
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            //List<Surface> ls = new ArrayList<>();
            //ls.add(previewSurface);
            //mCaptureRequestBuilder.addTarget(ls);
            mCaptureRequestBuilder.addTarget(previewSurface);

            if (!mIsFrontCamera) {
                //TODO switch toruch mode
                switch (flashMode) {
                    case FLASH_AUTO:
                        mCaptureRequestBuilder.set(CaptureRequest.FLASH_MODE,
                                CaptureRequest.FLASH_MODE_SINGLE);
                        break;
                    case FLASH_DISBALED:
                        mCaptureRequestBuilder.set(CaptureRequest.FLASH_MODE,
                                CaptureRequest.FLASH_MODE_OFF);
                        break;
                    case FLASH_ENABLED:
                        mCaptureRequestBuilder.set(CaptureRequest.FLASH_MODE,
                                CaptureRequest.FLASH_MODE_TORCH);
                        break;

                    //
                }
            }
            else {
                //TODO check
                mCaptureRequestBuilder.set(CaptureRequest.FLASH_MODE,
                        CaptureRequest.FLASH_MODE_OFF);
            }

            //mCameraDevice.createReprocessableCaptureSession(new InputConfiguration(100, 100, For));

            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                            mPreviewCaptureSession = cameraCaptureSession;
                            try {
                                //TODO fix illegal state EXCEPTION HERE COZ cameraCaptureSession can be closed
                                cameraCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(), null, mBackgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                            catch (IllegalStateException e){
                                e.printStackTrace();
                            }
//                    if (iv_fade.getVisibility() == View.VISIBLE) {
//                        //Effects.fade(iv_fade, 1.0f, 0.0f);
//                    }
                            swapping = false;
                            //TODO preallocate buffer as attempt to speed up;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                try {
                                    mPreviewCaptureSession.prepare(mImageReader.getSurface());
                                } catch (CameraAccessException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                            //Toast.makeText(getApplicationContext(), "Unable to setup preview", Toast.LENGTH_LONG).show();

                        }
                    }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void startPreview(){
        long start = System.currentTimeMillis();

        SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        Surface previewSurface = new Surface(surfaceTexture);
        try {
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            //List<Surface> ls = new ArrayList<>();
            //ls.add(previewSurface);
            //mCaptureRequestBuilder.addTarget(ls);
            mCaptureRequestBuilder.addTarget(previewSurface);

            if (!mIsFrontCamera) {
                //TODO switch toruch mode
                switch (flashMode) {
                    case FLASH_AUTO:
                        mCaptureRequestBuilder.set(CaptureRequest.FLASH_MODE,
                                CaptureRequest.FLASH_MODE_SINGLE);
                        break;
                    case FLASH_DISBALED:
                        mCaptureRequestBuilder.set(CaptureRequest.FLASH_MODE,
                                CaptureRequest.FLASH_MODE_OFF);
                        break;
                    case FLASH_ENABLED:
                        mCaptureRequestBuilder.set(CaptureRequest.FLASH_MODE,
                                CaptureRequest.FLASH_MODE_TORCH);
                        break;

                    //
                }

                mCaptureRequestBuilder.set(CaptureRequest.JPEG_QUALITY, (byte)80);
                //todo set iso
                mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);
                //mCaptureRequestBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, 100L);

                mCaptureRequestBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, 80);
                //mCaptureRequestBuilder.set(CaptureRequest.SENSOR_FRAME_DURATION, 767309312L);


                CameraCharacteristics cc = mCameraManager.getCameraCharacteristics(mCameraDevice.getId());
                Range<Long> map = cc.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE);

                String backCameraId = mCameraManager.getCameraIdList()[0];
                CameraCharacteristics backCameraInfo = mCameraManager.getCameraCharacteristics(backCameraId);
                Long level = backCameraInfo.get(CameraCharacteristics.SENSOR_INFO_MAX_FRAME_DURATION);

                if (map != null) {

                }
                map= null;

            }
            else {
                //TODO check
                mCaptureRequestBuilder.set(CaptureRequest.FLASH_MODE,
                        CaptureRequest.FLASH_MODE_OFF);
            }

            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                    mPreviewCaptureSession = cameraCaptureSession;
                    try {
                        //TODO fix illegal state EXCEPTION HERE COZ cameraCaptureSession can be closed
                        cameraCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(), null, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                    catch (IllegalStateException e){
                        e.printStackTrace();
                    }
//                    if (iv_fade.getVisibility() == View.VISIBLE) {
//                        //Effects.fade(iv_fade, 1.0f, 0.0f);
//                    }
                    swapping = false;
                    //TODO preallocate buffer as attempt to speed up;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        try {
                            mPreviewCaptureSession.prepare(mImageReader.getSurface());
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                    //Toast.makeText(getApplicationContext(), "Unable to setup preview", Toast.LENGTH_LONG).show();

                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public class captureAsync extends AsyncTask<Void, Integer, Void> {
        private DF.DFObject object;
        private int[] resValue;

        @Override
        protected Void doInBackground(Void... arg0) {
            captureStillImage();

            return null;
        }
        @Override
        protected void onPostExecute(Void adapter) {
            //mPreviewCaptureSession.capture(stillBuilder.build(), captureCallback, null);
        }
    }

    private void captureStillImageZeroShutter(){
        //TODO ask permission if nessesaary
        //CameraCharacteristics cc = mCameraManager.getCameraCharacteristics(cameraId);
        //StreamConfigurationMap map = cc.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

        final long start = System.currentTimeMillis();
        try {
            //Log.i("TEST", "createImageFileName()");
            mImageFile = createImageFileName();
        } catch (IOException e) {
            e.printStackTrace();
            //Log.i("TEST", "ERROR in createImageFileName()");
            return;
        }
        //
        CaptureRequest.Builder stillBuilder = null;
        try {
            //TODO zero shutter new fi4a
            stillBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_ZERO_SHUTTER_LAG);
            //stillBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            stillBuilder.addTarget(mImageReader.getSurface());
            //int rotation = getWindowManager().getDefaultDisplay().getRotation();

            //TODO get rotation from sensor
            int rotation = getDeviceRotation();// mCurrentRotation; //getWindowManager().getDefaultDisplay().getRotation();
            //

            if (!mIsFrontCamera) {
                //TODO switch torch mode
                switch (flashMode) {
                    case FLASH_AUTO:
                        stillBuilder.set(CaptureRequest.FLASH_MODE,
                                CaptureRequest.FLASH_MODE_SINGLE);
                        break;
                    case FLASH_DISBALED:
                        stillBuilder.set(CaptureRequest.FLASH_MODE,
                                CaptureRequest.FLASH_MODE_OFF);
                        break;
                    case FLASH_ENABLED:
                        stillBuilder.set(CaptureRequest.FLASH_MODE,
                                CaptureRequest.FLASH_MODE_TORCH);
                        break;

                    //
                }
            }else {
                //TODO check
                stillBuilder.set(CaptureRequest.FLASH_MODE,
                        CaptureRequest.FLASH_MODE_OFF);
            }




            if (mIsFrontCamera) {
                stillBuilder.set(CaptureRequest.JPEG_ORIENTATION, rotation);
            }else {
                //TODO change values
                stillBuilder.set(CaptureRequest.JPEG_ORIENTATION, rotation);
            }

            CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    //Log.i("TEST", "Before unlockFocus()");
                    //unlockFocus();
                    //Log.i("TEST", "After unlockFocus()");
                    long diff = System.currentTimeMillis() - start;
                    //mTextureView.setRotation(45.0f);
                    //mTextureView.requestLayout();
                    Log.i("PROF", "captureStillImage time = :" + diff + mImageFileName);
                    Toast.makeText(getApplicationContext(), "Image captured time = " + diff, Toast.LENGTH_SHORT).show();
                    runEditor();
                }
            };
            //TODO uncomment to use sync
            mPreviewCaptureSession.stopRepeating();//new
            //mPreviewCaptureSession.capture(stillBuilder.build(), captureCallback, null);
            //mCameraCaptureSession.capture(stillBuilder.build(), captureCallback, null);
            List captureRequestList = new ArrayList<CaptureRequest>();
            captureRequestList.add(stillBuilder.build());

            mPreviewCaptureSession.captureBurst(captureRequestList, captureCallback, null);
            //mPreviewCaptureSession.captureBurst(captureRequestList, captureCallback, null);
            //mPreviewCaptureSession.captureBurst(captureRequestList, captureCallback, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
            Log.i("TEST", "CameraAccessException in captureStillImage()");
        }


    }

    private void captureStillImage(){
//        try {
//            mPreviewCaptureSession.stopRepeating();//new
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
        //TODO ask permission if nessesaary
        final long start = System.currentTimeMillis();
        try {
            //Log.i("TEST", "createImageFileName()");
            mImageFile = createImageFileName();
        } catch (IOException e) {
            e.printStackTrace();
            //Log.i("TEST", "ERROR in createImageFileName()");
            return;
        }
        long newFile = System.currentTimeMillis() - start;
        //
        CaptureRequest.Builder stillBuilder = null;
        try {
            //TODO zero shutter new fi4a
            //stillBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_ZERO_SHUTTER_LAG);
            stillBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            stillBuilder.addTarget(mImageReader.getSurface());
            //int rotation = getWindowManager().getDefaultDisplay().getRotation();

            //TODO get rotation from sensor
            int rotation = getDeviceRotation();// mCurrentRotation; //getWindowManager().getDefaultDisplay().getRotation();
            //

            if (!mIsFrontCamera) {
                //TODO switch torch mode
                switch (flashMode) {
                    case FLASH_AUTO:
                        stillBuilder.set(CaptureRequest.FLASH_MODE,
                                CaptureRequest.FLASH_MODE_SINGLE);
                        break;
                    case FLASH_DISBALED:
                        stillBuilder.set(CaptureRequest.FLASH_MODE,
                                CaptureRequest.FLASH_MODE_OFF);
                        break;
                    case FLASH_ENABLED:
                        stillBuilder.set(CaptureRequest.FLASH_MODE,
                                CaptureRequest.FLASH_MODE_TORCH);
                        break;

                    //
                }
            }else {
                //TODO check
                stillBuilder.set(CaptureRequest.FLASH_MODE,
                        CaptureRequest.FLASH_MODE_OFF);
            }

            //TODO speed up
            boolean fast = true;
            if (fast) {
                stillBuilder.set(CaptureRequest.EDGE_MODE,
                        CaptureRequest.EDGE_MODE_OFF);
                stillBuilder.set(
                        CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE,
                        CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_ON);
                stillBuilder.set(
                        CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE,
                        CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE_OFF);
                stillBuilder.set(CaptureRequest.NOISE_REDUCTION_MODE,
                        CaptureRequest.NOISE_REDUCTION_MODE_OFF);
                stillBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                        CaptureRequest.CONTROL_AF_TRIGGER_CANCEL);

                stillBuilder.set(CaptureRequest.CONTROL_AE_LOCK, true);
                stillBuilder.set(CaptureRequest.CONTROL_AWB_LOCK, true);
            }
            //
            boolean maxQ = true;
            if (maxQ){
                stillBuilder.set(CaptureRequest.JPEG_QUALITY, (byte)90);
            }

            //stillBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

            if (mIsFrontCamera) {
                stillBuilder.set(CaptureRequest.JPEG_ORIENTATION, rotation);
            }else {
                //TODO change values
                stillBuilder.set(CaptureRequest.JPEG_ORIENTATION, rotation);
            }

            CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult) {
                    super.onCaptureProgressed(session, request, partialResult);
                    Log.i("Q1", "onCaptureProgressed: +");
                }

                @Override
                public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
                    super.onCaptureStarted(session, request, timestamp, frameNumber);
                    Log.i("Q1", "onCaptureStarted: +");
                }

                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    Log.i("Q1", "onCaptureCompleted: +");
                    //Log.i("TEST", "Before unlockFocus()");
                    //unlockFocus();
                    //mTextureView.setRotation(45.0f);
                    //mTextureView.setRotation(75.0f);
                    //Log.i("TEST", "After unlockFocus()");
                    long diff = System.currentTimeMillis() - start;
                    Log.i("PROF", "captureStillImage time = :" + diff + mImageFileName);
                    Toast.makeText(getApplicationContext(), "Image captured time = " + diff, Toast.LENGTH_SHORT).show();
                    runEditor();
                }
            };
            //TODO uncomment to use sync
            //mPreviewCaptureSession.stopRepeating();//new
            //CaptureRequest cr = stillBuilder.build();
            long beforeRq = System.currentTimeMillis() - start - newFile;
            Log.i("Q1", "before send req: +");
            //TODO replace null with handler to separate threDS!!!!!!!!!
            //mPreviewCaptureSession.capture(stillBuilder.build(), captureCallback, null);
            Log.i("Q1", "after send req: +");
            //mCameraCaptureSession.capture(stillBuilder.build(), captureCallback, null);
            mPreviewCaptureSession.capture(stillBuilder.build(), captureCallback, null);


        } catch (CameraAccessException e) {
            e.printStackTrace();
            Log.i("Q1", "CameraAccessException in captureStillImage()");
        }


    }

    private void closeCamera(){
        if (mCameraCaptureSession != null){
            mCameraCaptureSession.close();
            mCameraCaptureSession = null;
        }
        if (mCameraDevice != null){
            mCameraDevice.close();
            mCameraDevice = null;
        }
        if (mImageReader != null){
            mImageReader.close();
            mImageReader = null;
        }
    }

    private void toggleFlash(){
        if (this.flashMode != 2)flashMode ++; else flashMode = 0;
        //TODO change image property
        //mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE,
          //      CaptureRequest.FLASH_MODE_TORCH);

        //startPreview();

        try {

            //TODO switch toruch mode
            switch (flashMode) {
                case FLASH_AUTO:
                    mCaptureRequestBuilder.set(CaptureRequest.FLASH_MODE,
                            CaptureRequest.FLASH_MODE_SINGLE);
                    break;
                case FLASH_DISBALED:
                    mCaptureRequestBuilder.set(CaptureRequest.FLASH_MODE,
                            CaptureRequest.FLASH_MODE_OFF);
                    break;
                case FLASH_ENABLED:
                    mCaptureRequestBuilder.set(CaptureRequest.FLASH_MODE,
                            CaptureRequest.FLASH_MODE_TORCH);
                    break;

                //
            }

            // Change some capture settings
            //mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
            // Build new request (we can't just edit existing one, as it is immutable)
            CaptureRequest mPreviewRequest = mCaptureRequestBuilder.build();
            // Set new repeating request with our changed one
            mPreviewCaptureSession.setRepeatingRequest(mPreviewRequest, null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private void startBackgroundThread(){
        mBackgroundHandlerThread = new HandlerThread("Camera2");
        mBackgroundHandlerThread.start();
        mBackgroundHandler = new Handler(mBackgroundHandlerThread.getLooper());
    }
    private void stopBackgroundThread(){
        mBackgroundHandlerThread.quitSafely();
        try{
            mBackgroundHandlerThread.join();
            mBackgroundHandlerThread = null;
            mBackgroundHandler = null;
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    private static int sensorToDeviceRotation(CameraCharacteristics ccs, int deviceOrientation){
        int sensorOrientation = ccs.get(CameraCharacteristics.SENSOR_ORIENTATION);
        deviceOrientation = ORIENTATIONS.get(deviceOrientation);
        return (sensorOrientation + deviceOrientation + 360) % 360;
    }

    private static Size selectOptimalSize(Size[] variants, int w, int h){
        Log.e("SHIT", "selectOptimalSize() called with: " + "variants = [" + variants + "], w = [" + w + "], h = [" + h + "]");
        List<Size> looksGood = new ArrayList<>();
        for(Size option : variants){
            if (option.getHeight() == option.getWidth() * h / w &&
                    option.getWidth() >= w && option.getHeight() >= h){
                looksGood.add(option);
            }
        }
        if (looksGood.size() > 0){
            return Collections.min(looksGood, new CompareSizeByArea());
        }else {
            return variants[1];//TODO set to 0 as was
        }
    }


    private void createVideoFolder(){
        File moveFile =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        mVideoFolder = new File(moveFile, "SunRay_video");
        if (!mVideoFolder.exists()){
            mVideoFolder.mkdirs();
        }
    }

    private File createVideoFileName() throws IOException{
        String timestamp = new SimpleDateFormat("yyyMMdd_HHmmss").format(new Date());
        String prepend = "VIDEO_" + timestamp + "_";
        File videoFile = File.createTempFile(prepend, ".mp4", mVideoFolder);
        mVideoFileName = videoFile.getAbsolutePath();
        return videoFile;
    }

    private void createImageFolder(){
        mImageFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        //File tempDir = Environment.getExternalStoragePublicDirectory(Environment.);
        //mImageFolder = new File(imgFile, "camera2_photo_test");
        //if (!mImageFolder.exists()){
          //  mImageFolder.mkdirs();
        //}
    }

    private File createImageFileName() throws IOException{
        //TODO IOException 	if an error occurs when writing the file.
        String timestamp = new SimpleDateFormat("yyyMMdd_HHmmss").format(new Date());
        String prepend = "PHOTO_" + timestamp + "_";
        File imageFile = File.createTempFile(prepend, ".JPEG", mImageFolder);
        mImageFileName = imageFile.getAbsolutePath();
        //
        String prepend2 = "PHOTO_" + timestamp + "temp_";
        File imageFile2 = File.createTempFile(prepend2, ".JPEG", mImageFolder);
        mTempImageFileName = imageFile2.getAbsolutePath();

        return imageFile2;//return temporary file location
    }

    //TODO split byte photo and video recording action
    private void checkWritePermission(){
        int marshmallow;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED){
                mIsRecording = true;
                //mRecordImageButton.setImageResource(R.mipmap.switch_camera);
                try {
                    createVideoFileName();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                startRecord();
                mMediaRecorder.start();
                mChronometer.setBase(SystemClock.elapsedRealtime());
                //Effects.chromo_rolling(mChronometer);
                //mChronometer.setVisibility(View.VISIBLE);
                mChronometer.start();
                //TODO animate chrono appearing
            }
            else {
                if (shouldShowRequestPermissionRationale(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                    Toast.makeText(this, "app need access to external storage to be able to save video",
                            Toast.LENGTH_LONG).show();
                }
                requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRTIE_EXTERNAL_STORAGE_PERMISSION);
            }
        }else{
            mIsRecording = true;
            //mRecordImageButton.setImageResource(R.mipmap.switch_camera);
            try {
                createVideoFileName();
            } catch (IOException e) {
                e.printStackTrace();
            }
            startRecord();
            mMediaRecorder.start();
            mChronometer.setBase(SystemClock.elapsedRealtime());
            //mChronometer.setVisibility(View.VISIBLE);
            //Effects.chromo_rolling(mChronometer);
            mChronometer.start();
        }
    }
    private void setupMediaRecorder() throws IOException{
        //TODO profile this
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setOutputFile(mVideoFileName);
        mMediaRecorder.setVideoEncodingBitRate(1000000);
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        //mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        int rotation = getDeviceRotation();
        mMediaRecorder.setOrientationHint(rotation);

        // Audio Settings
        //mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        //mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        //TODO setup audio support
        mMediaRecorder.prepare();
    }

    private void lockFocus(){
        mCaptureState = STAT_WAIT_LOCK;
        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_START);
        try {
            mPreviewCaptureSession.capture(mCaptureRequestBuilder.build(), mPreviewCaptureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            Log.i("TEST", "lockFocus CameraAccessException");
        }
    }

    private void stopRecordingVideo() {
        mIsRecording = false;
        try {
            mCameraCaptureSession.stopRepeating();
            mCameraCaptureSession.abortCaptures();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        catch (IllegalStateException e){
            e.printStackTrace();
        }
        try {
            mMediaRecorder.stop();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        mMediaRecorder.reset();

    }

    private static boolean contains(int[] modes, int mode) {
        if (modes == null) {
            return false;
        }
        for (int i : modes) {
            if (i == mode) {
                return true;
            }
        }
        return false;
        //TODO set jpeg quality
        //Set the JPEG quality here like so
        //captureBuilder.set(CaptureRequest.JPEG_QUALITY, (byte)90);
    }
}

