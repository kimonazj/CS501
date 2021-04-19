package com.example.researchproject;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.example.researchproject.database.Album;
import com.example.researchproject.database.AppDatabase;
import com.example.researchproject.database.HistoryWithAlbums;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Welcome extends AppCompatActivity {

    // UI components
    Button playrecom;
    Button history;
    Button newalbum;
    TextView recomsong;

    // recommended song's data
    String recomstring;

    // global variables for history and recommend system
    ArrayList<String> songhistory;

    // for history database
    AppDatabase db;

    // get signedin user
    GoogleSignInAccount account;

    // handler for some thread processing
    Handler handler;

    private HistoryWithAlbums historyWithAlbums;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);

        // Instantiate UI Components
        playrecom = (Button)findViewById(R.id.playrecom);
        history = (Button)findViewById(R.id.history);
        newalbum = (Button)findViewById(R.id.get_new_album);
        recomsong = (TextView)findViewById(R.id.recomsong);

        // create instance of database
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "project_db_v8").allowMainThreadQueries().build();

        // get signedin account
        account = GoogleSignIn.getLastSignedInAccount(this);

        // initialize songHistory 
        songhistory = new ArrayList<String>();

        handler = new Handler(Looper.getMainLooper());

        // get history
        new Welcome.retrieveHistory(Welcome.this, account.getEmail()).execute();

        //search a new album
        newalbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // redirect to TextRecognitionActivity
                Intent newAlbum = new Intent(Welcome.this, TextRecognitionActivity.class);
                startActivity(newAlbum);
            }
        });

        // play the recommended song
        playrecom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent play = new Intent(Welcome.this, MainActivity2.class);
                // pass recommended artist to MainActivity2
                play.putExtra("artist",recomstring);
                startActivity(play);
            }
        });

        //visit history
        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent history = new Intent(Welcome.this, History.class);
                if (songhistory == null) {
                    songhistory = new ArrayList<String>();
                }
                startActivity(history);
            }
        });
    }

    private void setRecommendedArtist(HistoryWithAlbums historyWithAlbums) {
        this.historyWithAlbums = historyWithAlbums;
        if (historyWithAlbums == null || historyWithAlbums.albums.size() == 0) {
            // if there is no history, default song will be Taylor Swift's welcome to new york
            recomstring = "Taylor Swift";
            recomsong.setText("Taylor Swift");
        }
        else {
            // iterate through the albums and add artists to songHistory
            for (Album album : historyWithAlbums.albums) {
                songhistory.add(album.getArtistName());
            }

            // get the most common artist
            recomstring = mostCommon(songhistory);

            // set artist as recommended
            recomsong.setText(recomstring);
        }
    }

    public static <T> T mostCommon(ArrayList<T> list) {
        Map<T, Integer> map = new HashMap<>();

        for (T t : list) {
            Integer val = map.get(t);
            map.put(t, val == null ? 1 : val + 1);
        }

        Map.Entry<T, Integer> max = null;

        for (Map.Entry<T, Integer> e : map.entrySet()) {
            if (max == null || e.getValue() > max.getValue())
                max = e;
        }

        return max.getKey();
    }

    private static class retrieveHistory extends AsyncTask<Void,Void, HistoryWithAlbums> {

        private WeakReference<Welcome> activityReference;
        String userId;

        // only retain a weak reference to the activity
        retrieveHistory(Welcome context, String userId) {
            activityReference = new WeakReference<>(context);
            this.userId = userId;
        }

        // doInBackground methods runs on a worker thread
        @Override
        protected HistoryWithAlbums doInBackground(Void... objs) {
            // get historywithalbums from userId
            HistoryWithAlbums historyWithAlbums = activityReference.get().db.historyWithAlbumsDao().getHistoryWithAlbums(userId);

            // use an extra thread to set historyWithAlbums in the activity UI
            activityReference.get().handler.post(new Runnable() {
                @Override
                public void run() {
                    activityReference.get().setRecommendedArtist(historyWithAlbums);
                }
            });

            return historyWithAlbums;
        }

        // onPostExecute runs on main thread
        @Override
        protected void onPostExecute(HistoryWithAlbums historyWithAlbums) {
        }

    }
}
