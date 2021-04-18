package com.example.researchproject;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.example.researchproject.database.Album;
import com.example.researchproject.database.AppDatabase;
import com.example.researchproject.database.HistoryWithAlbums;
import com.example.researchproject.database.Review;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class History extends AppCompatActivity {
    Button playbtn;
    Button change;
    Button back;
    ArrayList<String> songHistory;
    Boolean artistBase; // default is false => genre
    TextView preferenceTV;
    ListView listview;

    String selectedSong;
    String selectedSongUri;
    String selectedSongArtist;
    String selectedAlbum;

    AppDatabase db;
    // get signedin user
    GoogleSignInAccount account;

    private Handler handler;

    private HistoryWithAlbums historyWithAlbums;

    Map<String, String> albumMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history);

        // create instance of database
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "project_db_v5").allowMainThreadQueries().build();

        account = GoogleSignIn.getLastSignedInAccount(this);

        // initiate buttons
        playbtn = findViewById(R.id.playsong);
        change = findViewById(R.id.changebtn);
        back = findViewById(R.id.back);

        preferenceTV = findViewById(R.id.recomtv);
        listview = findViewById(R.id.listview);

        handler = new Handler(Looper.getMainLooper());

        songHistory = new ArrayList<String>();

        albumMap = new HashMap<>();

        new History.retrieveHistory(History.this, account.getEmail()).execute();

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // split the string in the item to get relevant data
                String itemInfo[] = String.valueOf(parent.getItemAtPosition(position)).split("\\n");

                // the first index is a String written as "{song_name} by {album_artist}"
                String songAndArtist[] = itemInfo[0].split(" by ");

                selectedSong = songAndArtist[0];

                selectedSongArtist = songAndArtist[1];

                selectedAlbum = itemInfo[1].replace("Album: ", "");

                selectedSongUri = albumMap.get(selectedSong);
            }
        });

        // change recommendation preference
        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                artistBase = !artistBase;
                if(artistBase){
                    preferenceTV.setText("Recommended tracks based on: artists");
                }
                else{
                    preferenceTV.setText("Recommended tracks based on: genre");
                }
            }
        });

        // play the selected song
        playbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedSong!=null){
                    Intent intent = new Intent(History.this, MainActivity2.class);
                    intent.putExtra("historyAlbum", selectedAlbum);
                    intent.putExtra("historyArtist", selectedSongArtist);
                    intent.putExtra("historySong", selectedSong);
                    intent.putExtra("songUri", selectedSongUri);
                    startActivity(intent);
                }
                else{
                    Toast.makeText(getApplicationContext(),"Select a song first!",Toast.LENGTH_SHORT).show();
                }
            }
        });

        // back to welcome
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent welcome = new Intent(History.this, Welcome.class);
                startActivity(welcome);
            }
        });

    }

    private void setHistoryListview(HistoryWithAlbums historyWithAlbums) {
        this.historyWithAlbums = historyWithAlbums;

        if (historyWithAlbums == null) {
            // if there is no history, default song will be Taylor Swift's welcome to new york
            songHistory.add("1989"+"\n"+"Welcome to New York"+"\n"+"Taylor Swift");
            albumMap.put("1989","https://open.spotify.com/track/6qnM0XXPZOINWA778uNqQ9?si=f517332dcd7344f2");
        }
        else {
            for (Album album : historyWithAlbums.albums) {
                songHistory.add(album.getSongName() + " by " + album.getArtistName() + "\nAlbum: " + album.getAlbumName()+"\n");
                albumMap.put(album.getSongName(),album.getSongUri());
            }
        }

        //display song history in the listview
        ArrayAdapter adapter= new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, songHistory);
        listview.setAdapter(adapter);
    }

    // TODO: retrieveHistory
    private static class retrieveHistory extends AsyncTask<Void,Void,HistoryWithAlbums> {

        private WeakReference<History> activityReference;
        String userId;

        // only retain a weak reference to the activity
        retrieveHistory(History context, String userId) {
            activityReference = new WeakReference<>(context);
            this.userId = userId;
        }

        // doInBackground methods runs on a worker thread
        @Override
        protected HistoryWithAlbums doInBackground(Void... objs) {

            Log.d("History", "doInBackground for retrieveHistory");

            HistoryWithAlbums historyWithAlbums = activityReference.get().db.historyWithAlbumsDao().getHistoryWithAlbums(userId);

            activityReference.get().handler.post(new Runnable() {
                @Override
                public void run() {
                    activityReference.get().setHistoryListview(historyWithAlbums);
                }
            });

            return historyWithAlbums;
        }

        // onPostExecute runs on main thread
        @Override
        protected void onPostExecute(HistoryWithAlbums historyWithAlbums) {
            activityReference.get().historyWithAlbums = historyWithAlbums;
        }

    }


 
}
