package com.example.project.emorec2;


import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

public class SurfaceActivity extends AppCompatActivity {
    private static final SparseIntArray ORIENTATION_MAP = new SparseIntArray();

    private Camera      mCamera;
    private SurfaceView mSurfaceView;

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
    }

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
