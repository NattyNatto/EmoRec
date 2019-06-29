package com.example.project.emorec2;

import android.content.Intent;
import android.util.Log;

import java.io.File;
import java.util.List;
import java.util.Map;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.Header;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
//
//
import retrofit2.http.Query;

public interface Api {

//    @POST("emo/")
//    Call<Result> emoClassify(@Body PostData postData);

    @Multipart
    @POST("emo/")
    Call<Result> emoRec(@Part MultipartBody.Part file);

    @POST("emo/")
    Call<Result> emoClassify(@Body PostData postData);


//


}

