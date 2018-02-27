package com.prezpal.prezpal;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.util.ArrayList;
import java.util.List;

public class ResultsSummaryActivity extends AppCompatActivity implements AnalysisResultFragment.OnFragmentInteractionListener{

    private static final String ARG_SEVERITY = "SEVERITY";
    private static final String ARG_NAME = "NAME";
    private static final String ARG_DETAILS = "DETAILS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FFmpeg ffmpeg = FFmpeg.getInstance(this);
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {

                @Override
                public void onStart() {}

                @Override
                public void onFailure() {}

                @Override
                public void onSuccess() {}

                @Override
                public void onFinish() {}
            });
        } catch (FFmpegNotSupportedException e) {
            // Handle if FFmpeg is not supported by device
        }


        List<AnalysisItem> analysisItems = new ArrayList<AnalysisItem>();

        Intent i = getIntent();
        List<Parcelable> parcelableList = i.getParcelableArrayListExtra("ANALYSIS_ITEMS");
        for(Parcelable parcelable : parcelableList){
            analysisItems.add((AnalysisItem) parcelable);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results_summary);

        // Perform Analysis here

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
            transaction.commit();

        }
    }

    @Override
    public void onFragmentInteraction(Uri uri){

    }
}
