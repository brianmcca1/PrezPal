package com.prezpal.prezpal;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AudioRecordingActivity extends AppCompatActivity {

    // Media Recorder object
    private MediaRecorder mRecorder = null;
    // The max amplitudes recorded every 50ms
    private List<Integer> maxAmplitudes = new ArrayList<Integer>();
    // The list of all analyzed analysis items
    private List<AnalysisItem> analysisItems = new ArrayList<AnalysisItem>();
    // Time markers to determine duration of recording
    private long startTime, endTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_recording);
        final Button startRecordingButton = (Button) findViewById(R.id.startRecordingButton);
        final Button stopRecordingButton = (Button) findViewById(R.id.stopRecordingButton);

        Intent intent = getIntent();
        final Integer expectedDuration = intent.getIntExtra("DURATION", 5);

        stopRecordingButton.setVisibility(View.INVISIBLE);

        startRecordingButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){

                startRecordingButton.setVisibility(View.INVISIBLE);
                stopRecordingButton.setVisibility(View.VISIBLE);
                try {
                    startRecording();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        });

        stopRecordingButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                stopRecording();
                // Analyze the Max Amplitudes
                analysisItems.add(AudioAnalysis.analyzeAmplitudes(maxAmplitudes));

                // TODO: This is where the rest of the analysis should go
                Long actualDuration = (endTime - startTime) * 1000000; // Calculate the duration in seconds
                analysisItems.add(AudioAnalysis.analyzeDuration(expectedDuration * 60, actualDuration));
                // Launch intent for the ResultsSummaryActivity
                Intent resultsIntent = new Intent(getApplicationContext(), ResultsSummaryActivity.class);
                ArrayList<Parcelable> parcelableList = new ArrayList<Parcelable>();
                for(AnalysisItem item : analysisItems){
                    parcelableList.add((Parcelable) item);
                }

                resultsIntent.putParcelableArrayListExtra("ANALYSIS_ITEMS", parcelableList);
                startActivity(resultsIntent);

            }
        });

    }

    private void startRecording() throws IOException{
        // Check for permissions

        int writePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        // If we don't have permissions, ask user for permissions
        if (writePermission != PackageManager.PERMISSION_GRANTED) {
            String[] PERMISSIONS_STORAGE = {
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
            int REQUEST_EXTERNAL_STORAGE = 1;

            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
        startTime = System.nanoTime();
        if(mRecorder == null){
            mRecorder = new MediaRecorder();
            mRecorder.reset();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile(getExternalCacheDir().getAbsolutePath() + "/testVideo");
            mRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
                @Override
                public void onError(MediaRecorder recorder, int what, int extra){
                    Log.d("MainActivity", "WHAT: " + what + " EXTRA: " + extra);

                }
            });
            mRecorder.prepare();
            mRecorder.start();


            Runnable checkAmplitude = new Runnable (){
                public void run() {
                    maxAmplitudes.add(mRecorder.getMaxAmplitude());
                }
            };

            ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
            executor.scheduleAtFixedRate(checkAmplitude, 0, 50, TimeUnit.MILLISECONDS);
        }
    }

    private void stopRecording() {
        endTime = System.nanoTime();
        if(mRecorder != null){
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
    }
}
