package com.prezpal.prezpal;

import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_recording);
        final Button startRecordingButton = (Button) findViewById(R.id.startRecordingButton);
        final Button stopRecordingButton = (Button) findViewById(R.id.stopRecordingButton);

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
        if(mRecorder == null){
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile("/res/videos");
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
        if(mRecorder != null){
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
    }
}
