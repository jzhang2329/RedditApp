package com.example.redditapp;


import com.example.redditapp.model.Feed;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface FeedAPI {

    String BASE_URL = "https://www.reddit.com/r/";

    //Non-Static feed name
    @GET("{feed_name}/.rss")
    Call<Feed> getFeed(@Path("feed_name") String feed_name);
//  static feed name
//    @GET("earthporn/.rss")
//    Call<Feed> getFeed();
}
