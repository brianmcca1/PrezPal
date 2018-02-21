package com.prezpal.prezpal;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainMenu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
    }

    //The function called when the presentation button is pressed
    public void launchPresentationMode(View view) {
    }

    //The function called when the presentation settings button (the wrench) is is pressed
    public void launchPresentationSettings(View view) {
        Intent intent = new Intent(this, PresentationSettings.class);
        startActivity(intent);
    }

    //The function called when the interview button is pressed
    public void launchInterviewMode(View view) {
    }

    //The function called when the interview settings button (the other wrench) is pressed
    public void launchInterviewSettings(View view){
        Intent intent = new Intent(this, InterviewSettings.class);
        startActivity(intent);
    }

    //The function called when the How to Use... button is pressed
    public void launchHelpInstructions(View view){
        Intent intent = new Intent(this, HelpInstructions.class);
        startActivity(intent);
    }
}
