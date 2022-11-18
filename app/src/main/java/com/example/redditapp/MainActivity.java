package com.example.redditapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.example.redditapp.model.Feed;
import com.example.redditapp.model.entry.Entry;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final String BASE_URL = "https://www.reddit.com/r/";

    private Button btnRefreshFeed;
    private EditText mFeedName;
    private String currentFeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: starting.");
        btnRefreshFeed = (Button) findViewById(R.id.btnRefreshFeed);
        mFeedName = (EditText) findViewById(R.id.etFeedName);
        init();

        btnRefreshFeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String feedName = mFeedName.getText().toString();
                if(!feedName.equals("")){
                    currentFeed = feedName;
                    init();
                }else{
                    init();
                }
            }
        });


    }

    private void init(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .build();

        FeedAPI feedAPI = retrofit.create(FeedAPI.class);

        Call<Feed> call = feedAPI.getFeed(currentFeed);

        call.enqueue(new Callback<Feed>() {
            @Override
            public void onResponse(Call<Feed> call, Response<Feed> response) {
                //       Log.d(TAG, "onResponse: feed:" +response.body().toString());
                Log.d(TAG, "onResponse: Server Response: " + response.toString());


                List<Entry> entrys = response.body().getEntrys();

                Log.d(TAG, "onResponse: entrys:" +response.body().getEntrys());

//                Log.d(TAG, "onResponse: author:" +entrys.get(0).getAuthor());
//                Log.d(TAG, "onResponse: updated:" +entrys.get(0).getUpdates());
//                Log.d(TAG, "onResponse: title:" +entrys.get(0).getTitle());

                final ArrayList<Post> posts = new ArrayList<Post>();
                for(int i=0;i< entrys.size();i++){
                    ExtractXML extractXML1 = new ExtractXML(entrys.get(i).getContent(),"<a href=");
                    List<String> postContent=extractXML1.start();

                    ExtractXML extractXML2 = new ExtractXML(entrys.get(i).getContent(),"<img src=");

                    try {
                        postContent.add(extractXML2.start().get(0));
                    }catch(NullPointerException e){
                        postContent.add(null);
                        Log.e(TAG, "onResponse: NullPointerException(thumbnail1)" + e.getMessage() );
                    }catch(IndexOutOfBoundsException e){
                        postContent.add(null);
                        Log.e(TAG, "onResponse: IndexOutOfBoundsException(thumbnail2)" + e.getMessage() );
                    }
                    int lastPostion = postContent.size()-1;
                    try{
                        posts.add(new Post(
                                entrys.get(i).getTitle(),
                                entrys.get(i).getAuthor().getName(),
                                entrys.get(i).getUpdates(),
                                postContent.get(0),
                                postContent.get(lastPostion)
                        ));
                    }catch (NullPointerException e){
                        posts.add(new Post(
                                entrys.get(i).getTitle(),
                                "None",
                                entrys.get(i).getUpdates(),
                                postContent.get(0),
                                postContent.get(lastPostion)
                        ));
                        Log.e(TAG, "onResponse: NullPointerException" + e.getMessage() );
                    }

                }

                for(int j = 0; j< posts.size();j++){
                    Log.d(TAG, "onResponse: \n " +
                            "PostURL: " + posts.get(j).getPostURL() + "\n" +
                            "ThumbnailURL: " + posts.get(j).getThumbnailURL() + "\n" +
                            "Author: " + posts.get(j).getAuthor() + "\n" +
                            "updated: " + posts.get(j).getDate_updated() + "\n"
                    );
                }

                ListView listView = (ListView) findViewById(R.id.listView);
                CustomListAdapter customListAdapter = new CustomListAdapter(MainActivity.this,R.layout.card_layout_main, posts);
                listView.setAdapter(customListAdapter);

                listView.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Log.d(TAG, "onItemClick: Cliked: " + posts.get(i).toString());
                        Intent intent = new Intent(MainActivity.this, CommentsActivity.class);
                        intent.putExtra("@string/post_url", posts.get(i).getPostURL());
                        intent.putExtra("@string/post_thumbnails", posts.get(i).getThumbnailURL());
                        intent.putExtra("@string/post_title", posts.get(i).getTitle());
                        intent.putExtra("@string/post_author", posts.get(i).getAuthor());
                        intent.putExtra("@string/post_updated", posts.get(i).getDate_updated());
                        startActivity(intent);
                    }
                });


//                listView.setOnClickListener(new OnItemClickListener() {
//                    @Override
//                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//
//                    }
////                    @Override
////                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
////                        Log.d(TAG, "onItemClick: Cliked: " + posts.get(i).toString());
////                        Intent intent = new Intent(MainActivity.this, CommentsActivity.class);
////                        intent.putExtra("@string/post_url", posts.get(i).getPostURL());
////                        intent.putExtra("@string/post_thumbnails", posts.get(i).getThumbnailURL());
////                        intent.putExtra("@string/post_title", posts.get(i).getTitle());
////                        intent.putExtra("@string/post_author", posts.get(i).getAuthor());
////                        intent.putExtra("@string/post_updated", posts.get(i).getDate_updated());
////                        startActivity(intent);
////                    }
//                });
//
            }


            @Override
            public void onFailure(Call<Feed> call, Throwable t) {
                Log.e(TAG, "onFailure: Unable to retrieve RSS: " + t.getMessage());
                Toast.makeText(MainActivity.this, "An Error Occured", Toast.LENGTH_SHORT).show();
            }
        });
    }
}