package com.example.project.emorec2;


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.provider.MediaStore;
import android.media.*;
import android.util.Base64;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.*;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.*;
import android.widget.Button;
import android.hardware.Camera.*;
import android.widget.ImageView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AppConfig;
import androidx.camera.core.CameraX;
import androidx.camera.core.VideoCapture;
import androidx.camera.core.VideoCaptureConfig;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
//import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.Body;
import retrofit2.http.Multipart;

public class SurfaceActivity extends AppCompatActivity {
    private static final SparseIntArray ORIENTATION_MAP = new SparseIntArray();

    //    private Camera mCamera;
//        private SurfaceView mSurfaceView;
    Button btnCapture;
    Camera camera1;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    AudioRecord myAudioRecord;
    ImageView imgTest, imgEmo;
    public static boolean previewing = false;
    ProgressDialog progressDialog;
//    String mediaPath;
//    String[] mediaColumns = {MediaStore.Video.Media._ID};
//
    ServiceEmo serviceEmo = new ServiceEmo();

//    AudioRecord myAudioRecorder = new AudioRecord();


    static {
        ORIENTATION_MAP.put(Surface.ROTATION_0, 0);
        ORIENTATION_MAP.put(Surface.ROTATION_90, 90);
        ORIENTATION_MAP.put(Surface.ROTATION_180, 180);
        ORIENTATION_MAP.put(Surface.ROTATION_270, 270);
    }

    VideoCapture videoCapture;

    public SurfaceActivity() {
    }

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surface);
        progressDialog = new ProgressDialog(this);

//        mSurfaceView = findViewById(R.id.surface_view);
//
//        initView();

        VideoCaptureConfig videoCaptureConfig = new VideoCaptureConfig
                .Builder()
                .setLensFacing(CameraX.LensFacing.FRONT)
//                .setTargetAspectRatio(screenAspectRatio)
//                .setTargetRotation(viewFinder.display.rotation)
                .build();

        videoCapture = new VideoCapture(videoCaptureConfig);

        CameraX.bindToLifecycle(this, videoCapture);

        videoCapture.startRecording(new File(""), new VideoCapture.OnVideoSavedListener() {
            public void onVideoSaved(File file) {
                Log.i("", "Video File : $file");
            }

            public void onError(VideoCapture.UseCaseError useCaseError, String message, Throwable cause) {
                Log.i("", "Video Error: $message");
            }

        });




        getWindow().setFormat(PixelFormat.UNKNOWN);
        surfaceView = findViewById(R.id.surface_view);//new SurfaceView(this);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(new Callback() {
            public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                       int height) {
                // TODO Auto-generated method stub
                if (previewing) {
                    camera1.stopPreview();
                    previewing = false;
                }

                if (camera1 != null) {
                    try {
                        camera1.setPreviewDisplay(surfaceHolder);
                        camera1.startPreview();
                        previewing = true;
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }

            public void surfaceCreated(SurfaceHolder holder) {
                // TODO Auto-generated method stub

            }

            public void surfaceDestroyed(SurfaceHolder holder) {
                // TODO Auto-generated method stub

                releaseCamera();
                previewing = false;


            }

            private void releaseCamera(){
                    if (camera1 != null) {
                        camera1.setPreviewCallback(null);
                        camera1.stopPreview();
                        camera1.release();

                        camera1 = null;
                    }
            }

        });
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        btnCapture = findViewById(R.id.btnCamera);

        imgTest = findViewById(R.id.ivTest);
        imgEmo = findViewById(R.id.ivEmo);


        //surfaceView.setBackgroundResource(R.drawable.your_background_image);

        if (!previewing) {
            obtainCamera();
        }



        btnCapture.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub

                if (camera1 != null) {
                    camera1.takePicture(myShutterCallback, myPictureCallback_RAW, myPictureCallback_JPG);

                }
            }

            ShutterCallback myShutterCallback = new ShutterCallback() {

                public void onShutter() {
                    // TODO Auto-generated method stub
                }
            };
            PictureCallback myPictureCallback_RAW = new PictureCallback() {

                public void onPictureTaken(byte[] arg0, Camera arg1) {
                    // TODO Auto-generated method stub
                }
            };
            PictureCallback myPictureCallback_JPG = new PictureCallback() {

                public void onPictureTaken(byte[] arg0, Camera arg1) {
                    // TODO Auto-generated method stub
                    Bitmap bitmapPicture = BitmapFactory.decodeByteArray(arg0, 0, arg0.length);
                    Bitmap correctBmp = Bitmap.createBitmap(bitmapPicture, 0, 0, bitmapPicture.getWidth(), bitmapPicture.getHeight(), null, true);


//                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//                    bitmapPicture.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
//                    byte[] bytes = byteArrayOutputStream.toByteArray();
//                    int len = 100;
//                    for (int i = 0; i < base64.length(); i += len) {
//                        Log.i("AAA", base64.substring(i, i + len));
//                    }

                    bitmapPicture = Bitmap.createScaledBitmap(bitmapPicture, 256, 256, false);
                    String base64 = convertBitmapToString(bitmapPicture);

                    byte[] bytes2 = Base64.decode(base64, Base64.NO_WRAP);
                    Bitmap b = BitmapFactory.decodeByteArray(bytes2, 0, bytes2.length);
                    imgTest.setImageBitmap(b);

                    serviceEmo.classify(new PostData("test4", base64), new retrofit2.Callback<Result>() {
                        @Override
                        public void onResponse(Call<Result> call, Response<Result> response) {
                            Toast.makeText(SurfaceActivity.this, "emo = " + response.toString(), Toast.LENGTH_LONG).show();

                            if(response.body().result.equals("FE")){

                                imgEmo.setImageResource(R.drawable.fear);
                            } else if(response.body().result.equals("HA")){
                                imgEmo.setImageResource(R.drawable.happiness);
                            } else if(response.body().result.equals("SA")){
                                imgEmo.setImageResource(R.drawable.sad);
                            } else if(response.body().result.equals("SU")){
                                imgEmo.setImageResource(R.drawable.surprised);
                            } else if(response.body().result.equals("NE")){
                                imgEmo.setImageResource(R.drawable.neutral);
                            } else if(response.body().result.equals("AN")){
                                imgEmo.setImageResource(R.drawable.angry);
                            } else{
                                imgEmo.setImageResource(R.drawable.disgust);
                            }
                        }

                        @Override
                        public void onFailure(Call<Result> call, Throwable t) {
                            t.printStackTrace();
                        }
                    });
                }

                private String convertBitmapToString(Bitmap bitmap) {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP);
                }

            };
        });

        startCamera(surfaceHolder);
    }

    private void startCamera(SurfaceHolder surfaceHolder) {
    }

    private void obtainCamera() {
        if (camera1 == null) {
//            mCamera = Camera.open(0);
//                mCamera = openFrontFacingCameraGingerbread();
//            camera1 = openFrontFacingCamera();
            camera1 = openFrontFacingCamera();//Camera.open();
            if (camera1 != null) {
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
    }

    private Camera openFrontFacingCamera() {
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

    private void releaseCamera() {
        if (camera1 != null) {
            camera1.setPreviewCallback(null);
            camera1.stopPreview();
            camera1.release();

            camera1 = null;
        }
    }

    private void uploadFile(String filepath){
        progressDialog.show();
        //Map is used to multipart the file using okhttp3RequestBody
        Retrofit retrofit = GetRetrofit.getRetrofitClient(this);
        Api Api = retrofit.create(Api.class);

        File file = new File(filepath);

        //Parsing any Media type file
        RequestBody fileRequestBody = RequestBody.create(MediaType.parse("*/*"),file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("emo",file.getName(),fileRequestBody);

        Call call = Api.emoRec(part);
        //       Call<Result> call = getResponse.upload("token",map);
//              serviceEmo.classify(nnew retrofit2.Callback<Result>(){
        call.enqueue(new retrofit2.Callback<Result>() {

           public void onResponse(Call<Result> call, Response<Result> response) {
               Result result = response.body();
               if (result != null) {
                   Toast.makeText(SurfaceActivity.this, "emo = " + response.toString(), Toast.LENGTH_LONG).show();

                   if (response.body().result.equals("FE")) {

                       imgEmo.setImageResource(R.drawable.fear);
                   } else if (response.body().result.equals("HA")) {
                       imgEmo.setImageResource(R.drawable.happiness);
                   } else if (response.body().result.equals("SA")) {
                       imgEmo.setImageResource(R.drawable.sad);
                   } else if (response.body().result.equals("SU")) {
                       imgEmo.setImageResource(R.drawable.surprised);
                   } else if (response.body().result.equals("NE")) {
                       imgEmo.setImageResource(R.drawable.neutral);
                   } else if (response.body().result.equals("AN")) {
                       imgEmo.setImageResource(R.drawable.angry);
                   } else {
                       imgEmo.setImageResource(R.drawable.disgust);
                   }
               } else {
                   Log.v("Response", result.toString());
               }

           }

           public void onFailure(Call <Result> call, Throwable t){

           }
       });

   }


//    private void initView() {
//        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
//            @Override
//            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//            }
//
//            @Override
//            public void surfaceCreated(SurfaceHolder holder) {
//                startCamera(holder);
//            }
//
//            @Override
//            public void surfaceDestroyed(SurfaceHolder holder) {
//            }
//
//        });
//    }



}


