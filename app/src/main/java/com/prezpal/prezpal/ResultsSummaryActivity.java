package com.prezpal.prezpal;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.List;

public class ResultsSummaryActivity extends AppCompatActivity {

    private static final String ARG_SEVERITY = "SEVERITY";
    private static final String ARG_NAME = "NAME";
    private static final String ARG_DETAILS = "DETAILS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent i = getIntent();
        Uri videoUri = i.getData();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results_summary);
        // Perform Analysis here
        List<AnalysisItem> analysisItems = AudioAnalysis.audioAnalysis(videoUri);
        analysisItems.addAll(VideoAnalysis.videoAnalysis(videoUri));
        // Create fragments
        FragmentManager manager = getFragmentManager();
        for(AnalysisItem item : analysisItems){
            AnalysisResultFragment fragment = new AnalysisResultFragment();
            FragmentTransaction transaction = manager.beginTransaction();

            Bundle args = new Bundle();
            args.putString(ARG_SEVERITY, item.getSeverity().toString());
            args.putString(ARG_NAME, item.getName());
            args.putString(ARG_DETAILS, item.getDetails());
            fragment.setArguments(args);
            transaction.add(android.R.id.content, fragment);

        }
    }
}
