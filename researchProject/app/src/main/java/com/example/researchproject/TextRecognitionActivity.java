package com.example.researchproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.nl.languageid.LanguageIdentification;
import com.google.mlkit.nl.languageid.LanguageIdentifier;
import android.widget.Toast;

public class TextRecognitionActivity extends AppCompatActivity {

    // helps you identify from which Intent you came back
    static final int REQUEST_IMAGE_CAPTURE = 1;

    // initialize image and final text output
    InputImage image;
    String search;

    TextView resultView;
    Button btnCamera;
    Button back;

    EditText input_album;
    Button input_search;
    // Boolean artistbase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_recognition);

        // Declare button to start process
        btnCamera = (Button) findViewById(R.id.btnCamera);
        resultView = (TextView) findViewById(R.id.searchResult);
        back = (Button)findViewById(R.id.back);
        input_search = (Button)findViewById(R.id.stringbtn);

        // the user can type the album's name if he knows the language
        input_album = (EditText)findViewById(R.id.albumstring);

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        // if the user wants to go back, he can go back to welcome class
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent welcome = new Intent(TextRecognitionActivity.this, Welcome.class);
                startActivity(welcome);
            }
        });

        input_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Pass input text to MainActivity2 (for Spotify API)
                Intent intent = new Intent(TextRecognitionActivity.this, MainActivity2.class);
                intent.putExtra("album", input_album.getText().toString());
                startActivity(intent);
            }
        });

    }

    // Receives the image from takePictureIntent and makes it into a InputImage
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            image = InputImage.fromBitmap(imageBitmap, 0);
        }

        recognizeText(image);
    }

    // Takes in InputImage and passes the extracted text to the next intent
    private void recognizeText(InputImage image) {

        // [START get_detector_default]
        // get instance of TextRecognizer
        TextRecognizer recognizer = TextRecognition.getClient();
        // [END get_detector_default]

        // [START run_detector]
        // pass the image to the process method
        Task<Text> result =
                recognizer.process(image)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text visionText) {
                                // process the text block
                                search = processTextBlock(visionText);

                                // tell the user what lanugage is on the album
                                LanguageIdentifier languageIdentifier = LanguageIdentification.getClient();
                                languageIdentifier.identifyLanguage(search)
                                        .addOnSuccessListener(
                                                new OnSuccessListener<String>() {
                                                    //@Override
                                                    public void onSuccess(@Nullable String languageCode) {
                                                        if (languageCode.equals("und")) {
                                                            Toast.makeText(getApplicationContext(),"can't identify language.",Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            Toast.makeText(getApplicationContext(),"Language: " + languageCode,Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                })
                                        .addOnFailureListener(
                                                new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        // Model couldn???t be loaded or other internal error.
                                                        // ...
                                                        Toast.makeText(getApplicationContext(),"on failure of recognizing the text's lanugage",Toast.LENGTH_SHORT).show();
                                                    }
                                                });

                                // set result of processing to resultView
                                resultView.setText(search);

                                // Pass text to MainActivity2 (for Spotify API)
                                Intent intent = new Intent(TextRecognitionActivity.this, MainActivity2.class);
                                intent.putExtra("album", search);
                                startActivity(intent);
                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("textrecognition", e.toString());
                                        // Task failed with an exception
                                        // ...
                                    }
                                });
        // [END run_detector]
    }

    // break down the Text result and extract text block by block, line by line, word by word
    private String processTextBlock(Text result) {
        String returnValue = "";
        for (Text.TextBlock block : result.getTextBlocks()) {
            for (Text.Line line : block.getLines()) {
                for (Text.Element element : line.getElements()) {
                    String elementText = element.getText();
                    returnValue += elementText + " ";
                }
            }
        }
        return returnValue;
    }

    // Calls the camera app to take a photo
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
            // display error state to the user
        }
    }
}