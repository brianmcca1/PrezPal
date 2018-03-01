package com.prezpal.prezpal;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.os.Handler;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.StorageScopes;
import com.google.api.services.storage.model.StorageObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import android.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

import static android.os.Environment.getExternalStorageDirectory;
import static java.lang.Thread.sleep;


public class AudioRecordingActivity extends AppCompatActivity {

    // Media Recorder object
    private MediaRecorder mRecorder = null;
    // The max amplitudes recorded every 50ms
    private List<Integer> maxAmplitudes = new ArrayList<Integer>();
    // The list of all analyzed analysis items
    private List<AnalysisItem> analysisItems = new ArrayList<AnalysisItem>();
    // Title for the output File, listed as the timestamp of when recording started
    private String outputTitle = "";
    private static String API_URL = "https://speech.googleapis.com/v1/speech:recognize?key=AIzaSyDUw1wJVXSi_DYHngyMqcA68kKMoNqjlpM";

    private final Object lock = new Object();

    // Time markers to determine duration of recording
    private long startTime, endTime;

    private volatile Boolean receivedResponse = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_recording);
        //bindService(new Intent(this, SpeechService.class), mServiceConnection, BIND_AUTO_CREATE);
        final Button startRecordingButton = (Button) findViewById(R.id.startRecordingButton);
        final Button stopRecordingButton = (Button) findViewById(R.id.stopRecordingButton);
        Intent intent = getIntent();
        final Integer expectedDuration = intent.getIntExtra("DURATION", 5);


        stopRecordingButton.setVisibility(View.INVISIBLE);

        startRecordingButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                startRecordingButton.setVisibility(View.INVISIBLE);
                stopRecordingButton.setVisibility(View.VISIBLE);
                try {
                    startRecording();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        stopRecordingButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopRecording();

                String fileName = getExternalStorageDirectory().getAbsolutePath() + "/" + outputTitle;
                // Upload the file to Google Cloud Storage
//                Uri storageUri = Uri.parse("gs://prezpal_audio_files/" + fileName);
//                try {
//                    uploadFile("prezpal_audio_files", fileName);
//                } catch(Exception e){
//                    e.printStackTrace();
               // }
                byte[] data = null;
                final String encodedData;
                try {
                    while(mRecorder != null){}
                    File outputFile = new File(fileName);
                    if(!outputFile.exists()){
                        throw new IOException("FILE DOES NOT EXIST");
                    }
                    FileInputStream fis = new FileInputStream(outputFile);
                    data = new byte[(int) outputFile.length()];
                    Log.i("DATA", "Data length: " + data.length);
                    Log.i("DATA", fileName);
                    int bytesRead = 1;
                    while(bytesRead > 0){
                        bytesRead = fis.read(data);
                        Log.i("READING", "READ " + bytesRead + " BYTES FROM AUDIO FILE!!");
                    }
                    fis.close();
                } catch(IOException e){
                    e.printStackTrace();
                }
                Log.i("Encoding", "Encoding data");
                encodedData = Base64.encodeToString(data, Base64.NO_WRAP);
                AsyncHttpClient client = new AsyncHttpClient();
                StringEntity postBody = null;
                try{
                    JSONObject params = new JSONObject();
                    JSONObject config = new JSONObject();
                    JSONObject audio = new JSONObject();
                    config.put("encoding", "AMR_WB");
                    config.put("sampleRateHertz", 16000);
                    config.put("languageCode", "en-US");

                    audio.put("content", encodedData);

                    params.put("config", config);
                    params.put("audio", audio);
                    postBody = new StringEntity(params.toString());

                } catch (Exception e){
                    e.printStackTrace();
                }

                final List<Float> confidenceList = new ArrayList<Float>();
                Log.i("post", "Sending POST request now");

                client.post(getApplicationContext(), API_URL, postBody, "application/json", new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                // If the response is JSONObject instead of expected JSONArray
                                String transcript = "";
                                try {
                                    Log.i("TAG", "Successful response: " + response.toString());
                                    JSONArray results = (JSONArray) response.get("results");
                                    for(int i = 0; i<results.length(); i++){
                                        JSONObject result = (JSONObject) results.get(i);

                                        JSONArray alternatives = (JSONArray) result.get("alternatives");
                                        for(int j = 0; j < alternatives.length(); j++){
                                            JSONObject alternative = (JSONObject) alternatives.get(j);
                                            Float confidence = Float.parseFloat(alternative.get("confidence").toString());
                                            String newTranscript = alternative.getString("transcript");
                                            transcript += newTranscript;
                                            Log.i("TAG", "GOT CONFIDENCE RESPONSE");
                                            Log.i("TAG", "GOT CONFIDENCE RESPONSE");
                                            confidenceList.add(confidence);




                                        }
                                    }
                                    float confidenceSum = 0;
                                    for(Float confidence : confidenceList){
                                        confidenceSum += confidence;
                                    }

                                    analysisItems.add(AudioAnalysis.analyzeRecognition(confidenceSum / confidenceList.size()));
                                    // Analyze the Max Amplitudes
                                    analysisItems.add(AudioAnalysis.analyzeAmplitudes(maxAmplitudes));

                                    Long actualDuration = (endTime - startTime) / 1000000000; // Calculate the duration in seconds
                                    String[] words = transcript.split(" ");
                                    Integer wordCount = words.length;
                                    analysisItems.add(AudioAnalysis.analyzeSpeakingRate(wordCount, actualDuration));
                                    analysisItems.add(AudioAnalysis.analyzeDuration(expectedDuration * 60, actualDuration));
                                    Intent resultsIntent = new Intent(getApplicationContext(), ResultsSummaryActivity.class);
                                    ArrayList<Parcelable> parcelableList = new ArrayList<Parcelable>();
                                    for (AnalysisItem item : analysisItems) {
                                        parcelableList.add((Parcelable) item);
                                    }

                                    Log.i("LOG", "About to launch intent");
                                    resultsIntent.putParcelableArrayListExtra("ANALYSIS_ITEMS", parcelableList);
                                    startActivity(resultsIntent);
                                } catch (JSONException e){
                                    e.printStackTrace();
                                }

                                receivedResponse = true;

                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response){
                                e.printStackTrace();
                                Log.e("ERR", "STATUS CODE " + statusCode);
                                Log.e("ERR", response.toString());
                                Log.e("ERR", "Data: " + encodedData);
                                receivedResponse = true;

                            }
                        });
                    stopRecordingButton.setVisibility(View.INVISIBLE);
                    // Maybe include a loading thing here?

            }
        });

    }

    private void startRecording() throws IOException {
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
        if (mRecorder == null) {
            outputTitle = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
            mRecorder = new MediaRecorder();
            mRecorder.reset();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_WB);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
            mRecorder.setOutputFile(getExternalStorageDirectory().getAbsolutePath() + "/" + outputTitle);
            mRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
                @Override
                public void onError(MediaRecorder recorder, int what, int extra) {
                    Log.d("MainActivity", "WHAT: " + what + " EXTRA: " + extra);

                }
            });
            mRecorder.prepare();
            mRecorder.start();


            Runnable checkAmplitude = new Runnable() {
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
        if (mRecorder != null) {
            mRecorder.stop();

            mRecorder.release();
            mRecorder = null;
        }

    }


    static Storage storage = null;
    public void uploadFile(String bucketName, String filePath)throws Exception {

        Storage storage = getStorage();
        StorageObject object = new StorageObject();
        object.setBucket(bucketName);
        File file = new File(filePath);

        InputStream stream = new FileInputStream(file);

        try {
            String contentType = URLConnection.guessContentTypeFromStream(stream);
            InputStreamContent content = new InputStreamContent(contentType,stream);

            Storage.Objects.Insert insert = storage.objects().insert(bucketName, null, content);
            insert.setName(file.getName());
            insert.execute();


        } finally {
            stream.close();
        }
    }

    private Storage getStorage() throws Exception {

        if (storage == null) {
            HttpTransport httpTransport = new NetHttpTransport();
            JsonFactory jsonFactory = new JacksonFactory();
            List<String> scopes = new ArrayList<String>();
            scopes.add(StorageScopes.DEVSTORAGE_FULL_CONTROL);

            Credential credential = new GoogleCredential.Builder()
                    .setTransport(httpTransport)
                    .setJsonFactory(jsonFactory)
                    .setServiceAccountId("brianmcca1@gmail.com") //Email
                    .setServiceAccountPrivateKeyFromP12File(getTempPkc12File())
                    .setServiceAccountScopes(scopes).build();

            storage = new Storage.Builder(httpTransport, jsonFactory,
                    credential).setApplicationName("PrezPal")
                    .build();
        }

        return storage;
    }

    private File getTempPkc12File() throws IOException {
        // xxx.p12 export from google API console
        InputStream pkc12Stream = getResources().openRawResource(R.raw.prezpalkey);
        File tempPkc12File = File.createTempFile("temp_pkc12_file", "p12");
        OutputStream tempFileStream = new FileOutputStream(tempPkc12File);

        int read = 0;
        byte[] bytes = new byte[1024];
        while ((read = pkc12Stream.read(bytes)) != -1) {
            tempFileStream.write(bytes, 0, read);
        }
        return tempPkc12File;
    }

}