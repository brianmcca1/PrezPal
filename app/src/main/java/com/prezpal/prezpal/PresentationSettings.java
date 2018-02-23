package com.prezpal.prezpal;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class PresentationSettings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_presentation_settings);
        Button backbutton = (Button) findViewById(R.id.backButton);
        final EditText duration = (EditText) findViewById(R.id.duration);

        backbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                Intent intent = new Intent(getApplicationContext(), MainMenu.class);
                if(duration.getText().length() != 0){
                    intent.putExtra("DURATION", Integer.parseInt(duration.getText().toString()));
                }
                startActivity(intent);
            }
        });
    }

    //TODO:Functionality for the Timer Button goes here
}
