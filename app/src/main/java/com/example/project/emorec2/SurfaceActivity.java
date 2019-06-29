package com.example.project.emorec2;


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;
import android.media.*;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Base64;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.TextureView;
import android.view.View;
import android.view.View.*;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.camera.core.VideoCapture;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
//import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SurfaceActivity extends AppCompatActivity {
    private static final SparseIntArray ORIENTATION_MAP = new SparseIntArray();

    //    private Camera mCamera;
//        private SurfaceView mSurfaceView;
    Button btnCapture;
    Camera camera1;
    TextureView viewFinder;
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

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surface);

        viewFinder = findViewById(R.id.surface_view);
        imgTest = findViewById(R.id.ivTest);

        viewFinder.post(new Runnable() {
            @Override
            public void run() {
                startCamera();
            }
        });

        viewFinder.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                updateTransform();
            }
        });
    }

    private void startCamera() {

        // Create configuration object for the viewfinder use case
        PreviewConfig previewConfig = new PreviewConfig.Builder()
                .setTargetAspectRatio(new Rational(1, 1))
                .setTargetResolution(new Size(640, 640))
                .build();

        // Build the viewfinder use case
        Preview preview = new Preview(previewConfig);

        // Every time the viewfinder is updated, recompute layout
        preview.setOnPreviewOutputUpdateListener(new Preview.OnPreviewOutputUpdateListener() {
            @Override
            public void onUpdated(Preview.PreviewOutput output) {

                // To update the SurfaceTexture, we have to remove it and re-add it
                ViewGroup parent = (ViewGroup) viewFinder.getParent();
                parent.removeView(viewFinder);
                parent.addView(viewFinder, 0);

                viewFinder.setSurfaceTexture(output.getSurfaceTexture());
                updateTransform();
            }
        });

        HandlerThread analyzerThread = new HandlerThread("LuminosityAnalysis");
        analyzerThread.start();

        ImageAnalysisConfig analyzerConfig = new ImageAnalysisConfig.Builder()
                // Use a worker thread for image analysis to prevent glitches
                .setCallbackHandler(new Handler(analyzerThread.getLooper()))
                // In our analysis, we care more about the latest image than
                // analyzing *every* image
                .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
                .build();

        // Build the image analysis use case and instantiate our analyzer
        ImageAnalysis analyzerUseCase = new ImageAnalysis(analyzerConfig);
        analyzerUseCase.setAnalyzer(new ImageFrameAnalyzer());

        // Bind use cases to lifecycle
        // If Android Studio complains about "this" being not a LifecycleOwner
        // try rebuilding the project or updating the appcompat dependency to
        // version 1.1.0 or higher.
        CameraX.bindToLifecycle(this, preview, analyzerUseCase);
    }

    private void updateTransform() {
        Matrix matrix = new Matrix();

        // Compute the center of the view finder
        float centerX = viewFinder.getWidth() / 2f;
        float centerY = viewFinder.getHeight() / 2f;

        // Correct preview output to account for display rotation
        float rotationDegrees = 0;
        switch (viewFinder.getDisplay().getRotation()) {
            case Surface.ROTATION_0:
                rotationDegrees = 0f;
                break;
            case Surface.ROTATION_90:
                rotationDegrees = 90f;
                break;
            case Surface.ROTATION_180:
                rotationDegrees = 180f;
                break;
            case Surface.ROTATION_270:
                rotationDegrees = 270f;
                break;
        }

        rotationDegrees = rotationDegrees + (90 * 3) % 360;

        matrix.postRotate(-rotationDegrees, centerX, centerY);

        // Finally, apply transformations to our TextureView
        viewFinder.setTransform(matrix);
    }

    boolean isImageClassificationInProgress = false;
    Bitmap b;

    //    private void callApiImageClassification(byte[] arg0) {
    private void callApiImageClassification(Bitmap bitmapPicture) {

        if (isImageClassificationInProgress) return;
        isImageClassificationInProgress = true;

//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;
//
//        Log.i("aaa", "arg0" + arg0.length);
//        for (int i = 1000; i < 1000 + 10; i++) {
//            Log.i("aaa", "arg0: " + arg0[i]);
//        }
//
//        Bitmap bitmapPicture = BitmapFactory.decodeByteArray(arg0, 0, arg0.length, options);
        //Bitmap correctBmp = Bitmap.createBitmap(bitmapPicture, 0, 0, bitmapPicture.getWidth(), bitmapPicture.getHeight(), null, true);
        Log.i("aaa", "bitmapPicture: " + bitmapPicture.toString());

        bitmapPicture = Bitmap.createScaledBitmap(bitmapPicture, 256, 256, false);
        String base64 = convertBitmapToString(bitmapPicture);

        byte[] bytes2 = Base64.decode(base64, Base64.NO_WRAP);
        b = BitmapFactory.decodeByteArray(bytes2, 0, bytes2.length);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imgTest.setImageBitmap(b);
            }
        });

        Log.i("aaa", "base64: " + base64);

        serviceEmo.classify(new PostData("test4", base64), new retrofit2.Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
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
                isImageClassificationInProgress = false;
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                t.printStackTrace();
                isImageClassificationInProgress = false;
            }
        });
    }

    private String convertBitmapToString(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP);
    }


    private void uploadFile(String filepath) {
        progressDialog.show();
        //Map is used to multipart the file using okhttp3RequestBody
        Retrofit retrofit = GetRetrofit.getRetrofitClient(this);
        Api Api = retrofit.create(Api.class);

        File file = new File(filepath);

        //Parsing any Media type file
        RequestBody fileRequestBody = RequestBody.create(MediaType.parse("*/*"), file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("emo", file.getName(), fileRequestBody);

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

            public void onFailure(Call<Result> call, Throwable t) {

            }
        });

    }


    private class ImageFrameAnalyzer implements ImageAnalysis.Analyzer {
        private long lastAnalyzedTimestamp = 0L;

        /**
         * Helper extension function used to extract a byte array from an
         * image plane buffer
         */
        private byte[] toByteArray(ByteBuffer b) {
            b.rewind();   // Rewind the buffer to zero
            byte[] data = new byte[b.remaining()];
            b.get(data);   // Copy the buffer into a byte array
            return data; // Return the byte array
        }

        public void analyze(ImageProxy image, int rotationDegrees) {
            long currentTimestamp = System.currentTimeMillis();
            // Calculate the average luma no more often than every second
            if (currentTimestamp - lastAnalyzedTimestamp >= TimeUnit.SECONDS.toMillis(10000)) {
                // Since format in ImageAnalysis is YUV, image.planes[0]
                // contains the Y (luminance) plane
//                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
//                // Extract image data from callback object
//                byte[] data = toByteArray(buffer);
//                // Convert the data into an array of pixel values
//                //val pixels = data.map { it.toInt() and 0xFF }
//                byte[] pixels = new byte[data.length];
////                for (int i = 0; i < data.length; i++) {
////                    pixels[i] = (byte) (((int) data[i]) & 0xFF);
////                }
//                for (int i = 0; i < data.length; i++) {
//                    pixels[i] = (byte) (data[i] & (byte) 0xFF);
//                }

                callApiImageClassification(toBitmap(image.getImage()));

                lastAnalyzedTimestamp = currentTimestamp;
            }
        }
    }

    Bitmap toBitmap(Image im) {
        ByteBuffer yBuffer = im.getPlanes()[0].getBuffer(); // Y
        ByteBuffer uBuffer = im.getPlanes()[1].getBuffer(); // U
        ByteBuffer vBuffer = im.getPlanes()[2].getBuffer(); // V

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        byte[] nv21 = new byte[ySize + uSize + vSize];

        //U and V are swapped
        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, im.getWidth(), im.getHeight(), null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 50, out);
        byte[] imageBytes = out.toByteArray();
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }


}


