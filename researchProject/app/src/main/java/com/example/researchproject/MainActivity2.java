package com.example.researchproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.example.researchproject.database.HistoryAlbumCrossRef;
import com.example.researchproject.database.HistoryWithAlbums;
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
    private String album_output; //album output
    private String artist_output;
    private String history_uri_output;
    private String history_album_output;
    private String history_song_output;
    private String history_artist_output;
    private String SONG_URI;
    private String SONG_NAME;
    private String ALBUM_ARTIST;
    private String ALBUM_NAME;

    // integer variables to distinguish API calls
    private int GET_ALBUM_ID = 0;
    private int GET_ALBUM_TRACK = 1;
    private int GET_ARTIST_ID = 2;
    private int GET_ARTIST_TRACK = 3;

    // UI Components
    private TextView showAlbumName;
    private EditText newReview;
    private Button btnPlaySong;
    private Button searchnew;
    private Button back;
    private Button addReview;

    // a list review component
    private ListView reviewListView;

    // get variables for db
    private List<Review> reviewList;

    // to store comments in string
    List<String> stringlist = new ArrayList<String>();

    // media player variables
    private boolean FIRST_PLAY;

    private Handler handler;

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

        handler = new Handler(Looper.getMainLooper());

        // create instance of database
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "project_db_v5").allowMainThreadQueries().build();


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
                //Review review = new Review("example", account.getDisplayName(), newReview.getText().toString());
                new registerReview(MainActivity2.this, review).execute();

                // set reviewListView
                // add author's name and review details to the string list
                stringlist.add(review.getAuthor()+"\n"+ review.getReviewDetails());

                //display comments in the listview
                reviewListView.setAdapter(new ArrayAdapter<String>(MainActivity2.this, android.R.layout.simple_list_item_1, stringlist));

                newReview.setText("");

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
        setSearchInput();

        if(album_output != null){

            // begin our sequence of API calls, starting with our call for an access token.
            startAlbumSearch();

        }else if(artist_output != null){

            // search based on artist
            startRecommendedArtistTrackSearch();

        }else if(history_uri_output != null){

            // replay a song from the user's history.
            SONG_URI = history_uri_output;
            ALBUM_ARTIST = history_artist_output;
            ALBUM_NAME = history_album_output;
            SONG_NAME = history_song_output;

            // display the album, song and artist names
            showAlbumName.setText(SONG_NAME + " from " + ALBUM_NAME + "\n by " + ALBUM_ARTIST);

            // get reviews
            new retrieveReviews(this).execute();

        }else{

            // if no output is given from previous activity, display error in Log.
            Log.d("MainActivity2", "No search inputs given from previous activities.");

        }

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

    private void setSearchInput() {
        // Using the intent that activated this activity, store the search input
        // into our global variable
        Intent intent = getIntent();

        album_output = intent.getStringExtra("album");
        artist_output = intent.getStringExtra("artist");

        history_uri_output = intent.getStringExtra("songUri");
        history_album_output = intent.getStringExtra("historyAlbum");
        history_song_output = intent.getStringExtra("historySong");
        history_artist_output = intent.getStringExtra("historyArtist");
    }

    private void startAlbumSearch() {
        getAccessToken(GET_ALBUM_ID, album_output);
    }

    private void startRecommendedArtistTrackSearch() {
        getAccessToken(GET_ARTIST_ID, artist_output);
    }

    private void setReviewListView(List<Review> reviewList) {

        this.reviewList = reviewList;

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
    }

    private void getAccessToken(int api_call, String api_input) {
        // use the appropriate URL to find our access token
        String postUrl = "https://accounts.spotify.com/api/token";
        // create a new queue to run our request in
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        // use a StringRequest object to find our access token
        StringRequest stringRequest = new StringRequest(Request.Method.POST, postUrl, response -> {

            Log.d("MainActivity2", "access_token from the post request is: " + response);
            // once we have a response, extract the access token and store as a global variable
            // so we can reference it in later API calls, then immediately begin/resume our current API call
            try {
                JSONObject jsonObject = new JSONObject(response);
                String received_token = jsonObject.getString("access_token");

                access_token = received_token;

                // find out next API call with the api_call variable,
                // then begin API call with api_input as parameter.
                if (api_call == GET_ALBUM_ID) {

                    volleyGetAlbumID(api_input);

                } else if (api_call == GET_ALBUM_TRACK) {

                    volleyGetAlbumSong(api_input);

                } else if (api_call == GET_ARTIST_ID) {

                    volleyGetArtistID(api_input);

                } else if (api_call == GET_ARTIST_TRACK) {

                    volleyGetRandomArtistTrack(api_input);

                } else {

                    // for all other values in api_call, report the error in the log.
                    Log.d("MainActivity2", "Access Token found, but no valid API calls requested.");

                }
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
                ALBUM_ARTIST = album_data.getJSONArray("artists").getJSONObject(0).getString("name");
                ALBUM_NAME = album_data.getString("name");

                String album_id = album_data.getString("id");
                volleyGetAlbumSong(album_id);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> {
            // if the error is from an expired access token,
            // make an api call to refresh the access token first
            if (error.networkResponse.statusCode == 401) {
                getAccessToken(GET_ALBUM_ID, input);
            } else {
                // irrecoverable errors. log the error for debugging.
                Log.d("GetRequest", error.toString());
            }
        })
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

            // once we have a response, filter the first
            // from the first track on the album, then store it in a global variable.
            try {
                JSONArray track_list = response.getJSONArray("items");
                JSONObject first_track = track_list.getJSONObject(0);

                // get song name and uri
                String received_uri = first_track.getString("uri");
                String song_name = first_track.getString("name");
                SONG_NAME = song_name;
                SONG_URI = received_uri;

                // set our textView to display the current song.
                showAlbumName.setText(SONG_NAME + " from " + ALBUM_NAME + "\n by " + ALBUM_ARTIST);

                // add this new album to our database
                //Album album = new Album("example", "exampleagain", "wowexample");
                Album album = new Album(SONG_URI, ALBUM_NAME, SONG_NAME, ALBUM_ARTIST);
                new registerAlbum(MainActivity2.this, album).execute();


                // add album to history
                // registerAlbumToHistory
                new MainActivity2.registerAlbumToHistory(MainActivity2.this, account.getEmail()).execute();

                // get reviews
                new retrieveReviews(this).execute();

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> {
            // if the error is from an expired access token,
            // make an api call to refresh the access token first
            if (error.networkResponse.statusCode == 401) {
                getAccessToken(GET_ALBUM_TRACK, input);
            } else {
                // irrecoverable errors. log the error for debugging.
                Log.d("GetRequest", error.toString());
            }
        })
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

    public void volleyGetArtistID(String input) {

        // find the url to run our album search
        String url = "";
        String encodedText = "";
        try {
            // encode our search input so it fits UTF-8 format
            encodedText = URLEncoder.encode(input, "UTF-8").replace("+", "%20");

            // put together our url
            url = "https://api.spotify.com/v1/search?type=artist&q=" + encodedText;
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
                JSONObject artists = (JSONObject) response.get("artists");
                JSONObject first_artist_data = artists.getJSONArray("items").getJSONObject(0);

                // get album name and album artist
                ALBUM_ARTIST = first_artist_data.getString("name");

                String artist_id = first_artist_data.getString("id");
                volleyGetRandomArtistTrack(artist_id);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> {
            // if the error is from an expired access token,
            // make an api call to refresh the access token first
            if (error.networkResponse.statusCode == 401) {
                getAccessToken(GET_ARTIST_ID, input);
            } else {
                // irrecoverable errors. log the error for debugging.
                Log.d("GetRequest", error.toString());
            }
        })
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

    public void volleyGetRandomArtistTrack(String input) {
        // put together our url using the artist id from the previous call
        String url = "https://api.spotify.com/v1/artists/" + input + "/top-tracks?market=US";

        // instantiate queue for our request
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            Log.d("GetRequest", response.toString());

            // once we have a response, find a random track to play
            // from the top tracks of the artist
            try {
                int index = (int) (Math.random() * 10);
                JSONArray top_tracks = response.getJSONArray("tracks");
                JSONObject random_track = top_tracks.getJSONObject(index);

                // get song name and uri
                String received_album_name = random_track.getJSONObject("album").getString("name");
                String received_uri = random_track.getString("uri");
                String song_name = random_track.getString("name");

                ALBUM_NAME = received_album_name;
                SONG_NAME = song_name;
                SONG_URI = received_uri;

                // set our textView to display the current song.
                showAlbumName.setText(SONG_NAME + " from " + ALBUM_NAME + "\n by " + ALBUM_ARTIST);

                // add this new album to our database
                //Album album = new Album("example", "exampleagain", "wowexample");
                Album album = new Album(SONG_URI, ALBUM_NAME, SONG_NAME, ALBUM_ARTIST);
                new registerAlbum(MainActivity2.this, album).execute();


                // add album to history
                // registerAlbumToHistory
                new MainActivity2.registerAlbumToHistory(MainActivity2.this, account.getEmail()).execute();

                // get reviews
                new retrieveReviews(this).execute();

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> {
            // if the error is from an expired access token,
            // make an api call to refresh the access token first
            if (error.networkResponse.statusCode == 401) {
                getAccessToken(GET_ALBUM_TRACK, input);
            } else {
                // irrecoverable errors. log the error for debugging.
                Log.d("GetRequest", error.toString());
            }
        })
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
                activityReference.get().db.reviewDao().insert(review);
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

    private static class registerAlbumToHistory extends AsyncTask<Void,Void,Boolean> {

        private WeakReference<MainActivity2> activityReference;
        private String userId;

        // only retain a weak reference to the activity
        registerAlbumToHistory(MainActivity2 context, String userId) {
            activityReference = new WeakReference<>(context);
            this.userId = userId;
        }

        // doInBackground methods runs on a worker thread
        @Override
        protected Boolean doInBackground(Void... objs) {

            String songUri = activityReference.get().SONG_URI;

            History history = activityReference.get().db.historyDao().findByUserId(userId);
//            activityReference.get().db.historyWithAlbumsDao().insert(new HistoryAlbumCrossRef(history.historyId, songUri));

            return true;
        }

        // onPostExecute runs on main thread
        @Override
        protected void onPostExecute(Boolean bool) {
        }

    }

    private static class retrieveReviews extends AsyncTask<Void,Void,List<Review>> {

        private WeakReference<MainActivity2> activityReference;

        // only retain a weak reference to the activity
        retrieveReviews(MainActivity2 context) {
            activityReference = new WeakReference<>(context);
        }

        // doInBackground methods runs on a worker thread
        @Override
        protected List<Review> doInBackground(Void... objs) {
            try {
                if (activityReference.get() != null) {
                    Log.d("MainActivity2", "doInBackground for retrieveReview");

                    List<Review> retrievedReviews = activityReference.get().db.reviewDao().findBySongUri(activityReference.get().SONG_URI);

                    activityReference.get().handler.post(new Runnable() {
                        @Override
                        public void run() {
                            activityReference.get().setReviewListView(retrievedReviews);
                        }
                    });

                    return retrievedReviews;
                }
                else {
                    //Toast.makeText(context, "Review is Null", Toast.LENGTH_SHORT);
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
