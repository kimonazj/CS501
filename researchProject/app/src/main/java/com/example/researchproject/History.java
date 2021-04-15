package com.example.researchproject;

import android.content.Intent;
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

import com.example.researchproject.database.AppDatabase;
import com.example.researchproject.database.HistoryWithAlbums;

import java.util.ArrayList;
import java.util.List;

public class History extends AppCompatActivity {
    Button playbtn;
    Button change;
    Button back;
    ArrayList<String> songhistory;
    Boolean artistbase; // default is false => genre
    TextView preferencetv;
    ListView listview;


    String selectedsong;

    AppDatabase db;
    // TODO: get userid
    private String USER_ID;
    private History history;
    private List<HistoryWithAlbums> historyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history);

        // initiate buttons
        playbtn = findViewById(R.id.playsong);
        change = findViewById(R.id.changebtn);
        back = findViewById(R.id.back);

        preferencetv = findViewById(R.id.recomtv);
        listview = findViewById(R.id.listview);

        // TODO: change this working with database
        songhistory = new ArrayList<String>();
        if(songhistory == null || songhistory.size() == 0){
            //if there is no history, display the default welcome song
            songhistory.add("Welcome to NewYork");
            songhistory.add("No More");
        }
        //display song history in the listview
        ArrayAdapter adapter= new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, songhistory);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedsong = String.valueOf(parent.getItemAtPosition(position));
            }
        });

        // create instance of database
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "project_db_v4").build();

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
                    intent.putExtra("textOutput", selectedsong);
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
    /*private static class retrieveHistory extends AsyncTask<Void,Void,List<Review>> {

        private WeakReference<History> activityReference;

        // only retain a weak reference to the activity
        retrieveReviews(History context) {
            activityReference = new WeakReference<>(context);
        }

        // doInBackground methods runs on a worker thread
        @Override
        protected List<History> doInBackground(Void... objs) {
            if (activityReference.get() != null) {
                return activityReference.get().db.historyDao().findByUserId(activityReference.get().USER_ID);
            }
            else {
                return null;
            }
        }

        // onPostExecute runs on main thread
        @Override
        protected void onPostExecute(List<History> histories) {
            // to store comments in string
            List<String> stringlist = new ArrayList<String>();

            // if the reviews is not null
            if (histories != null && histories.size() > 0) {
                // set review list
                activityReference.get().historyList = histories;

                // add author's name and review details to the string list
                for(int i = 0; i < reviews.size(); i++){
                    stringlist.add(reviews[i].getAuthor()+"\n"+reviews[i].getReviewDetails());
                }
            }

            else{
                // if the review is null, display no comment
                stringlist.add("No comment")
            }

            //display comments in the listview
            ArrayAdapter adapter= new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, stringlist);
            activityReference.get().reviewListView.setAdapter(adapter)
        }

    }*/
}
