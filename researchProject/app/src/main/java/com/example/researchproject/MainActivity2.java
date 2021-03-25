package com.example.researchproject;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.ListItem;
import com.spotify.protocol.types.ListItems;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import retrofit2.Retrofit;

public class MainActivity2 extends AppCompatActivity {

    private static final String CLIENT_ID = "795863a23c73431496c269b1e0124dd5";
    private static final String CLIENT_SECRET = "1a04a3bc3fcf4992ba2045c83e1f6756";
    String encodedString = "Basic " +  new String(Base64.encode((CLIENT_ID + ":" + CLIENT_SECRET).getBytes(), Base64.NO_WRAP));

    private SpotifyAppRemote mSpotifyAppRemote;
    private static final String URL = "https://accounts.spotify.com/api/token";

    String access_token;

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
                CallResult<ListItems> songs = mSpotifyAppRemote.getContentApi().getRecommendedContentItems("IU");
                
            }
        });






        String access_token = "";
//
//        try {
//
//            URL url = new URL(URL);
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//            conn.setDoOutput(true);
//            conn.setDoInput(true);
//            conn.setRequestMethod("POST");
//            conn.setRequestProperty("Authorization", encodedString);
//            conn.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded");
//            conn.setRequestProperty( "Accept", "*/*" );
//
//            conn.connect();
//
//            OutputStream out = new BufferedOutputStream(conn.getOutputStream());
//            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
//            writer.write("grant_type=client_credentials");
//            writer.flush();
//            writer.close();
//
//            out.close();
//
//
//            InputStreamReader in = new InputStreamReader(conn.getInputStream());
//            BufferedReader br = new BufferedReader(in);
//            String output;
//            while ((output = br.readLine()) != null) {
//                access_token += (output);
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    }

    @Override
    protected void onStart() {
        super.onStart();


        ConnectionParams connectionParams = new ConnectionParams.Builder(CLIENT_ID)
                .setRedirectUri("http://com.example.researchproject/callback")
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
    }
    @Override
    protected void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }

}