package com.example.project.emorec2;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServiceEmo {

    public void classify(PostData postData, Callback<Result> callback) {

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        Api gerritAPI = retrofit.create(Api.class);

        Call<Result> call = gerritAPI.emoClassify(postData);
        call.enqueue(callback);
    }
}
