package com.example.lab2_20220270;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface CatApiService {
    
    @GET("cat?type=square")
    Call<ResponseBody> getRandomCat();
    
    @GET("cat/says/{text}?type=square")
    Call<ResponseBody> getCatWithText(@Path("text") String text);
}