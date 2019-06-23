package com.example.project.emorec2;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface Api {

    @POST("emo/")
    Call<Result> emoClassify(@Body PostData postData);
}