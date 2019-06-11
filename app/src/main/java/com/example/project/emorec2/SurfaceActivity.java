package com.example.project.emorec2;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.*;
import android.widget.Button;
import android.hardware.Camera.*;



import java.io.IOException;
import java.util.List;

public class SurfaceActivity extends AppCompatActivity {
    private static final SparseIntArray ORIENTATION_MAP = new SparseIntArray();

    private Camera      mCamera;
    private SurfaceView mSurfaceView;
    Button btn_capture;
    Camera camera1;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    public static boolean previewing = false;

    static
    {
        ORIENTATION_MAP.put(Surface.ROTATION_0, 0);
        ORIENTATION_MAP.put(Surface.ROTATION_90, 90);
        ORIENTATION_MAP.put(Surface.ROTATION_180, 180);
        ORIENTATION_MAP.put(Surface.ROTATION_270, 270);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surface);

        mSurfaceView = findViewById(R.id.surface_view);

        initView();



        getWindow().setFormat(PixelFormat.UNKNOWN);
        surfaceView = new SurfaceView(this);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        btn_capture = (Button) findViewById(R.id.button1);

        surfaceView.setBackgroundResource(R.drawable.your_background_image);

        if(!previewing){

            camera1 = Camera.open();
            if (camera1 != null){
                try {
                    camera1.setDisplayOrientation(90);
                    camera1.setPreviewDisplay(surfaceHolder);
                    camera1.startPreview();
                    previewing = true;
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        btn_capture.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub

                if(camera != null)
                {
                    camera1.takePicture(myShutterCallback, myPictureCallback_RAW, myPictureCallback_JPG);

                }
            }
        ShutterCallback myShutterCallback = new ShutterCallback(){

            public void onShutter() {
                // TODO Auto-generated method stub
            }};
        PictureCallback myPictureCallback_RAW = new PictureCallback(){

            public void onPictureTaken(byte[] arg0, Camera arg1) {
                // TODO Auto-generated method stub
            }};
        PictureCallback myPictureCallback_JPG = new PictureCallback(){

            public void onPictureTaken(byte[] arg0, Camera arg1) {
                // TODO Auto-generated method stub
                Bitmap bitmapPicture = BitmapFactory.decodeByteArray(arg0, 0, arg0.length);

                Bitmap correctBmp = Bitmap.createBitmap(bitmapPicture, 0, 0, bitmapPicture.getWidth(), bitmapPicture.getHeight(), null, true);

            }};



        });


    @Override
    protected void onResume() {
        super.onResume();

        obtainCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();

        releaseCamera();
    }

    private void initView() {
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                startCamera(holder);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }

        });
    }

    private void startCamera(SurfaceHolder holder) {
        Camera.Parameters camParameters = mCamera.getParameters();

        List<Camera.Size> sizes = camParameters.getSupportedPreviewSizes();
        // Usually the highest quality
        Camera.Size s = sizes.get(0);

        camParameters.setPreviewSize(s.width, s.height);
        camParameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
        if (camParameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
        {
            camParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }

        mCamera.setParameters(camParameters);

        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(0, info);

        int orientation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = (info.orientation - ORIENTATION_MAP.get(orientation) + 360) % 360;

        mCamera.setDisplayOrientation(degrees);

        try
        {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void obtainCamera() {
        if (mCamera == null)
        {
//            mCamera = Camera.open(0);
            mCamera = openFrontFacingCameraGingerbread();
        }
    }

    private Camera openFrontFacingCameraGingerbread() {
        int cameraCount = 0;
        Camera cam = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    cam = Camera.open(camIdx);
                } catch (RuntimeException e) {
                    Log.e("AAA", "Camera failed to open: " + e.getLocalizedMessage());
                }
            }
        }

        return cam;
    }

    private void releaseCamera() {
        if (mCamera != null)
        {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();

            mCamera = null;
        }
    }



}


