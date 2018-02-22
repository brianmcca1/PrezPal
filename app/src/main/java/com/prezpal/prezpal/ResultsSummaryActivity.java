package com.prezpal.prezpal;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.util.List;

public class ResultsSummaryActivity extends AppCompatActivity {

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



        Intent i = getIntent();
        Uri videoUri = i.getData();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results_summary);
        String audioPath = videoUri.getPath().replace("mp4", "mp3");
        String[] cmd = new String[5];
        cmd[0] = "-i " +  videoUri.getPath();
        cmd[1] = "-ab 128k";
        cmd[2] = "-ac 2";
        cmd[3] = "-ar 44100";
        cmd[4] = "-vn " + audioPath;
        Uri audioUri = Uri.parse(audioPath);
        try {
            // to execute "ffmpeg -version" command you just need to pass "-version"
            ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {

                @Override
                public void onStart() {}

                @Override
                public void onProgress(String message) {}

                @Override
                public void onFailure(String message) {}

                @Override
                public void onSuccess(String message) {}

                @Override
                public void onFinish() {}
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            // Handle if FFmpeg is already running
        }
        // Perform Analysis here
        List<AnalysisItem> analysisItems = AudioAnalysis.audioAnalysis(audioUri);
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
