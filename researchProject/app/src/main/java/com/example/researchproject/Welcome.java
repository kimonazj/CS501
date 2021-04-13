package com.example.researchproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

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
    Boolean artistbase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);

        // Instantiate UI Components
        playrecom = (Button)findViewById(R.id.playrecom);
        history = (Button)findViewById(R.id.history);
        newalbum = (Button)findViewById(R.id.get_new_album);
        recomsong = (TextView)findViewById(R.id.recomsong);

        // get bundle extras
//        Bundle extras = getIntent().getExtras();
//        if (extras == null){
//            return;
//        }
//        artistbase = extras.getBoolean("ifartist");

        //search a new album
        newalbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newAlbum = new Intent(Welcome.this, TextRecognitionActivity.class);
                startActivity(newAlbum);
            }
        });

        // play the recommended song
        playrecom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent play = new Intent(Welcome.this, MainActivity2.class);
                play.putExtra("songname",recomstring);
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
                history.putExtra("songhistory",songhistory);
                history.putExtra("ifartist", artistbase);
                startActivity(history);
            }
        });
    }
}
