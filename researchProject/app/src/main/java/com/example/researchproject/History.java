package com.example.researchproject;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
    ArrayList<String> songhistory;
    Boolean artistbase; // default is false => genre
    TextView preferencetv;
    ListView listview;

    String selectedsong;
    String selectedsonguri;

    AppDatabase db;
    // get signedin user
    GoogleSignInAccount account;

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

        preferencetv = findViewById(R.id.recomtv);
        listview = findViewById(R.id.listview);

        songhistory = new ArrayList<String>();

        albumMap = new HashMap<>();

        new History.retrieveHistory(History.this, account.getEmail()).execute();

        if (historyWithAlbums == null) {
            // if there is no history, default song will be Taylor Swift's welcome to new york
            songhistory.add("1989"+"\n"+"Welcome to New York"+"\n"+"Taylor Swift");
            albumMap.put("1989","https://open.spotify.com/track/6qnM0XXPZOINWA778uNqQ9?si=f517332dcd7344f2");
        }
        else {
            for (Album album : historyWithAlbums.albums) {
                songhistory.add(album.getAlbumName()+"\n"+ album.getSongName() + "\n" + album.getArtistName());
                albumMap.put(album.getAlbumName(),album.getSongUri());
            }
        }

        //display song history in the listview
        ArrayAdapter adapter= new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, songhistory);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // split the string to get the album
                String songinfo[] = String.valueOf(parent.getItemAtPosition(position)).split("\\n");
                selectedsonguri = albumMap.get(songinfo[0]);
            }
        });

        // change recommendation preference
        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                artistbase = !artistbase;
                if(artistbase){
                    preferencetv.setText("Recommended tracks based on: artists");
                }
                else{
                    preferencetv.setText("Recommended tracks based on: genre");
                }
            }
        });

        // play the selected song
        playbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedsong!=null){
                    Intent intent = new Intent(History.this, MainActivity2.class);
                    intent.putExtra("songuri", selectedsonguri);
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
            HistoryWithAlbums historyWithAlbums = activityReference.get().db.historyWithAlbumsDao().getHistoryWithAlbums(userId);
            return historyWithAlbums;
        }

        // onPostExecute runs on main thread
        @Override
        protected void onPostExecute(HistoryWithAlbums historyWithAlbums) {
            activityReference.get().historyWithAlbums = historyWithAlbums;
        }

    }


 
}
