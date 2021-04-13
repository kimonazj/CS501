package com.example.researchproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class History extends AppCompatActivity {
    Button playbtn;
    Button change;
    Button back;
    ArrayList<String> songhistory;
    Boolean artistbase; // default is false => genre
    TextView preferencetv;
    ListView listview;

    String selectedsong;

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

        // get intent extras
        Bundle extras = getIntent().getExtras();
        if (extras == null){
            return;
        }
        // TODO: maybe change these to database
        artistbase = extras.getBoolean("ifartist");
        songhistory = extras.getStringArrayList("songhistory");
        if(songhistory.size() == 0){
            //if there is no history, display the default welcome song
            songhistory.add("Welcome to NewYork - By Taylor Swift");
        }

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
                    intent.putExtra("ifartist",artistbase);
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
                welcome.putExtra("ifartist", artistbase);
                welcome.putExtra("songhistory", songhistory);
                startActivity(welcome);
            }
        });

        // TODO: initialize the listview
    }
}
