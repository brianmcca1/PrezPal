package com.prezpal.prezpal;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainMenu extends AppCompatActivity {

    static final int REQUEST_VIDEO_CAPTURE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
    }

    public void launchPresentationMode(View view) {
        /**
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if(takeVideoIntent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
         */
        Intent audioRecordingIntent = new Intent(this, AudioRecordingActivity.class);
        startActivity(audioRecordingIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        if(requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK){
            Uri videoUri = intent.getData();
            Intent resultsIntent = new Intent(this, ResultsSummaryActivity.class);
            resultsIntent.setData(videoUri);
            startActivity(intent);
        }
    }
    public void launchPresentationSettings(View view) {
        Intent intent = new Intent(this, PresentationSettings.class);
        startActivity(intent);
    }
    public void launchInterviewMode(View view) {
    }
    public void launchInterviewSettings(View view){
        Intent intent = new Intent(this, InterviewSettings.class);
        startActivity(intent);
    }
    public void launchHelpInstructions(View view){
        Intent intent = new Intent(this, HelpInstructions.class);
        startActivity(intent);
    }
}
