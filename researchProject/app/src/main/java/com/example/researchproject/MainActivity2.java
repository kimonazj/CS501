package com.example.researchproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.researchproject.database.Album;
import com.example.researchproject.database.AppDatabase;
import com.example.researchproject.database.History;
import com.example.researchproject.database.Review;
import com.example.researchproject.database.ReviewDao;
import com.example.researchproject.database.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
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

    // Client Information for API Calls
    private static final String CLIENT_ID = "795863a23c73431496c269b1e0124dd5";
    private static final String CLIENT_SECRET = "1a04a3bc3fcf4992ba2045c83e1f6756";
    private static final String REDIRECT_URI = "http://com.example.researchproject/callback";
    private String encodedCredentials = "Basic " +  new String(Base64.encode((CLIENT_ID + ":" + CLIENT_SECRET).getBytes(), Base64.NO_WRAP));

    // connector to the remote Spotify app on the device
    // this also means a Spotify app *is* required for the app to work
    private SpotifyAppRemote mSpotifyAppRemote;

    // global variables for API-based processing
    private String access_token;
    private String text_recognition_output;
    private String SONG_URI;

    // UI Components
    private TextView showAlbumName;
    private EditText newReview;
    private Button btnPlaySong;
    private Button searchnew;
    private Button back;
    private Button addReview;
    private boolean FIRST_PLAY;

    // a list review component
    private ListView reviewListView;

    // get variables for db
    private String album_artist;
    private String album_name;
    private List<Review> reviewList;

    // to store comments in string
    List<String> stringlist = new ArrayList<String>();

    AppDatabase db;

    // get signedin user
    GoogleSignInAccount account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        // Instantiate UI Components
        showAlbumName = (TextView) findViewById(R.id.album_name);
        btnPlaySong = (Button) findViewById(R.id.btnPlaySong);
        searchnew = (Button) findViewById(R.id.get_new_album);
        back = (Button)findViewById(R.id.back);

        newReview = (EditText) findViewById(R.id.newReview);
        addReview = (Button) findViewById(R.id.addReview);

        reviewListView = (ListView)findViewById(R.id.reviewlist);

        account = GoogleSignIn.getLastSignedInAccount(this);

        // create instance of database
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "project_db_v4").build();

        // check if album object exists and add new if it doesn't
        Album album = new Album(SONG_URI, album_name, album_artist);
        new MainActivity2.registerAlbum(MainActivity2.this, album);

        // get reviews
        new retrieveReviews(this).execute();

        if (reviewList == null) {
            reviewList = new ArrayList<Review>();
        }

        // set reviewListView
        // add author's name and review details to the string list
        for(int i = 0; i < reviewList.size(); i++){
            stringlist.add(reviewList.get(i).getAuthor()+"\n"+ reviewList.get(i).getReviewDetails());
        }

        //display comments in the listview
        reviewListView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, stringlist));


        // disable button until the remote spotify api is connected
        btnPlaySong.setEnabled(false);

        // Set up our button to play song (after the API calls return of course)
        btnPlaySong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SONG_URI != null) {
                    Log.d("MainActivity2", "Current Song URI is: " + SONG_URI);
                    if (btnPlaySong.getText().toString().equals(getString(R.string.play_music)) && FIRST_PLAY == false){
                        mSpotifyAppRemote.getPlayerApi().play(SONG_URI);
                        FIRST_PLAY = true;
                        btnPlaySong.setText(R.string.pause_music);
                    } else if (btnPlaySong.getText().toString().equals(getString(R.string.play_music))) {
                        mSpotifyAppRemote.getPlayerApi().resume();
                        btnPlaySong.setText(R.string.pause_music);
                    } else {
                        mSpotifyAppRemote.getPlayerApi().pause();
                        btnPlaySong.setText(R.string.play_music);
                    }
                }
            }
        });

        addReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // create new review object
                Review review = new Review(SONG_URI, account.getDisplayName(), newReview.getText().toString());
                new MainActivity2.registerReview(MainActivity2.this, review).execute();

                // set list of reviews with new review
                new retrieveReviews(MainActivity2.this).execute();

                // set reviewListView
                // add author's name and review details to the string list
                stringlist.add(review.getAuthor()+"\n"+ review.getReviewDetails());

                //display comments in the listview
                reviewListView.setAdapter(new ArrayAdapter<String>(MainActivity2.this, android.R.layout.simple_list_item_1, stringlist));

            }
        });

        // if the user wants search for a new album, they can click this button to go to text recognition page
        searchnew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent newAlbum = new Intent(MainActivity2.this, TextRecognitionActivity.class);
                startActivity(newAlbum);
            }
        });

        // if the user wants quit player, they can click this button to go back to welcome page
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent welcome = new Intent(MainActivity2.this, Welcome.class);
                startActivity(welcome);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // extract the text we got from the Text Recognition to use for our API calls
        setTextRecognitionOutPut();

        // begin our sequence of API calls, starting with our call for an access token.
        getAccessToken();

        // create connection parameters to use when we connect to the SpotifyAppRemote object
        ConnectionParams connectionParams = new ConnectionParams.Builder(CLIENT_ID)
                .setRedirectUri(REDIRECT_URI)
                .showAuthView(true)
                .build();

        // connect to the SpotifyAppRemote object using the parameters
        SpotifyAppRemote.connect(this, connectionParams,
                new Connector.ConnectionListener() {

                    // once we're connected we can go through with our API Calls!
                    @Override
                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        Log.d("MainActivity2", "Connected! Yay!");

                        btnPlaySong.setEnabled(true);
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
        FIRST_PLAY = false;
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }

    private void setTextRecognitionOutPut() {
        // Using the intent that activated this activity, store the recognized text
        // into our global variable
        Intent intent = getIntent();
        text_recognition_output = intent.getStringExtra("textOutput");
    }

    private void getAccessToken() {
        // use the appropriate URL to find our access token
        String postUrl = "https://accounts.spotify.com/api/token";
        // create a new queue to run our request in
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        // use a StringRequest object to find our access token
        StringRequest stringRequest = new StringRequest(Request.Method.POST, postUrl, response -> {

            Log.d("MainActivity2", "access_token from the post request is: " + response);
            // once we have a response, extract the access token and store as a global variable
            // so we can reference it in later API calls, then immediately begin our next API call
            // to find an album ID
            try {
                JSONObject jsonObject = new JSONObject(response);
                String received_token = jsonObject.getString("access_token");

                access_token = received_token;

                volleyGetAlbumID(text_recognition_output);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            VolleyLog.d("MainActivity2", "Error: " + error.getMessage());
            error.printStackTrace();
        }) {
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();

                // required parameters for our POST API call
                params.put("grant_type", "client_credentials");
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();

                // required headers for our POST API call
                headers.put("Authorization", encodedCredentials);
                return headers;
            }
        };

        // Run request as soon as possible
        requestQueue.add(stringRequest);
    }

    public void volleyGetAlbumID(String input) {

        // find the url to run our album search
        String url = "";
        String encodedText = "";
        try {
            // encode our search input so it fits UTF-8 format
            encodedText = URLEncoder.encode(input, "UTF-8").replace("+", "%20");

            // put together our url
            url = "https://api.spotify.com/v1/search?type=album&q=" + encodedText;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // create another queue to run our request
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            Log.d("GetRequest", response.toString());

            // once we have a response, retrieve the first album on the search results,
            // then display the album name, and extract the album's ID on the Spotify app
            // for our final API call
            try {
                JSONObject first_album = (JSONObject) response.get("albums");
                JSONObject album_data = first_album.getJSONArray("items").getJSONObject(0);

                // get album name and album artist
                album_artist = album_data.getJSONArray("artists").getJSONObject(0).getString("name");
                album_name = album_data.getString("name");
                showAlbumName.setText(getString(R.string.now_playing) + album_name);

                String album_id = album_data.getString("id");
                volleyGetAlbumSong(album_id);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> error.printStackTrace())
        {
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                // Required Headers for API call
                String auth = "Bearer " + access_token;
                headers.put("Authorization", auth);
                return headers;
            }
        };

        // begin request asap
        requestQueue.add(jsonObjectRequest);
    }

    public void volleyGetAlbumSong(String input) {
        // put together our url using the album id from the previous call
        String url = "https://api.spotify.com/v1/albums/" + input + "/tracks";

        // instantiate queue for our request
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            Log.d("GetRequest", response.toString());

            // once we have a response, retrieve the url to a 30 second preview
            // from the first track on the album, then store it in a global variable.
            try {
                JSONArray track_list = response.getJSONArray("items");
                JSONObject first_track = track_list.getJSONObject(0);
                String received_uri = first_track.getString("uri");
                SONG_URI = received_uri;
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> error.printStackTrace())
        {
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();

                // required headers for API call
                String auth = "Bearer " + access_token;
                headers.put("Authorization", auth);
                return headers;
            }
        };

        // begin request asap
        requestQueue.add(jsonObjectRequest);
    }

    private static class registerAlbum extends AsyncTask<Void,Void,Boolean> {

        private WeakReference<MainActivity2> activityReference;
        private Album album;

        // only retain a weak reference to the activity
        registerAlbum(MainActivity2 context, Album album) {
            activityReference = new WeakReference<>(context);
            this.album = album;
        }

        // doInBackground methods runs on a worker thread
        @Override
        protected Boolean doInBackground(Void... objs) {
            // if album doesn't exist, create new album
            if (activityReference.get().db.albumDao().findBySongUri(album.getSongUri()) == null) {
                activityReference.get().db.albumDao().insert(album);
            }
            return true;
        }

        // onPostExecute runs on main thread
        @Override
        protected void onPostExecute(Boolean bool) {
        }

    }

    private static class registerReview extends AsyncTask<Void,Void,Boolean> {

        private WeakReference<MainActivity2> activityReference;
        private Review review;

        // only retain a weak reference to the activity
        registerReview(MainActivity2 context, Review review) {
            activityReference = new WeakReference<>(context);
            this.review = review;
        }

        // doInBackground methods runs on a worker thread
        @Override
        protected Boolean doInBackground(Void... objs) {
            try {
                ReviewDao reviewDao = activityReference.get().db.reviewDao();
                reviewDao.insert(review);
                return true;
            } catch (Exception e) {
                Log.d("MainActivity2", "Encountered following Error in registerView: " + e);
                return null;
            }
        }

        // onPostExecute runs on main thread
        @Override
        protected void onPostExecute(Boolean bool) {
        }

    }

    private static class retrieveReviews extends AsyncTask<Void,Void,List<Review>> {

        private WeakReference<MainActivity2> activityReference;
        private Context context;

        // only retain a weak reference to the activity
        retrieveReviews(MainActivity2 context) {
            activityReference = new WeakReference<>(context);
            this.context = context;
        }

        // doInBackground methods runs on a worker thread
        @Override
        protected List<Review> doInBackground(Void... objs) {
            try {
                if (activityReference.get() != null) {
                    Log.d("MainActivity2", "doInBackground for retrieveReview");

                    return activityReference.get().db.reviewDao().findBySongUri(activityReference.get().SONG_URI);
                }
                else {
                    Toast.makeText(context, "Review is Null", Toast.LENGTH_SHORT);
                    return null;
                }
            } catch (Exception e) {
                Log.d("MainActivity2", "Encountered following Error in retrieveReview: " + e);
                return null;
            }
        }

        // onPostExecute runs on main thread
        @Override
        protected void onPostExecute(List<Review> reviews) {

            // set review list
            Log.d("MainActivity2", "Review has been retrieved");
            activityReference.get().reviewList = reviews;
        }

    }


}