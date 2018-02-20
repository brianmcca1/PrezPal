package com.example.david.prezpal;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainMenu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);
    }

    public void launchPresentationMode(View view) {
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
