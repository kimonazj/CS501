package com.example.researchproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.session.MediaSession;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.*;
import com.loopj.android.http.*;

import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.message.BasicHeader;

public class MainActivity2 extends AppCompatActivity {

    private static final String CLIENT_ID = "795863a23c73431496c269b1e0124dd5";
    private static final String CLIENT_SECRET = "1a04a3bc3fcf4992ba2045c83e1f6756";
    private static final String REDIRECT_URI = "http://com.example.researchproject/callback";
    private String encodedCredentials = "Basic " +  new String(Base64.encode((CLIENT_ID + ":" + CLIENT_SECRET).getBytes(), Base64.NO_WRAP));

    private SpotifyAppRemote mSpotifyAppRemote;

    private static String access_token;
    private static String text_recognition_output;
    private static String album_id;
    private static String first_song_url;
    private RequestQueue requestQueue;

    TextView tv;
    Button btnGetSongs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);


        tv = (TextView) findViewById(R.id.textView);
        btnGetSongs = (Button) findViewById(R.id.btnGetSongs);

        btnGetSongs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity2.this, TextRecognitionActivity.class);
                startActivity(intent);
            }
        });

        requestQueue = Volley.newRequestQueue(this);

        getAccessToken();

        Log.d("MainActivity2", "Token is " + access_token + "! Happy API Search!");
    }

    @Override
    protected void onStart() {
        super.onStart();


        ConnectionParams connectionParams = new ConnectionParams.Builder(CLIENT_ID)
                .setRedirectUri(REDIRECT_URI)
                .showAuthView(true)
                .build();


        SpotifyAppRemote.connect(this, connectionParams,
                new Connector.ConnectionListener() {

                    @Override
                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        Log.d("MainActivity2", "Connected! Yay!");

                        // Now you can start interacting with App Remote
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.e("MainActivity2", throwable.getMessage(), throwable);
                    }

                });


        if (text_recognition_output != null) {

            volleyGet("Search Album", text_recognition_output);
            volleyGet("First Song of Album", album_id);
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public static void setAccessToken(String token) {
        access_token = token;
    }

    public static void setAlbumID (String id) {
        album_id = id;
    }

    public static void setSongURL(String url) {
        first_song_url = url;
    }

    public static void setTextRecognitionOutput(String name) {
        text_recognition_output = name;
    }

    private void getAccessToken(){
        TokenRequest tokenRequest = new TokenRequest();
        tokenRequest.setResultListener(new TokenRequest.ResultListener() {
            @Override
            public void onResult(JSONObject result) {
                try {
                    access_token = result.getString("access_token");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        tokenRequest.getAccessToken(this, encodedCredentials);
    }

    private void volleyGet(String whatToGet, String input) {
        GetRequest getRequest = new GetRequest();

        getRequest.setResultListener(result -> {
            try {
                JSONArray jsonArray = result.getJSONArray("items");
                if (whatToGet.equals("Search Album")) {
                    JSONObject first_album = jsonArray.getJSONObject(0);
                    album_id = first_album.getString("id");
                } else {
                    JSONObject first_track = jsonArray.getJSONObject(0);
                    first_song_url = first_track.getString("preview_url");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        });

        try {
            getRequest.volleyGet(this, whatToGet, input, access_token);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


}
