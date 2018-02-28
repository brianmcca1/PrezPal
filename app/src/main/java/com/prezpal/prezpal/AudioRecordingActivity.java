package com.prezpal.prezpal;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.os.Handler;

import com.google.auth.Credentials;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.speech.v1.RecognitionAudio;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.RecognizeRequest;
import com.google.cloud.speech.v1.RecognizeResponse;
import com.google.cloud.speech.v1.SpeechGrpc;
import com.google.cloud.speech.v1.SpeechRecognitionResult;
import com.google.protobuf.InvalidProtocolBufferException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ClientInterceptors;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.internal.DnsNameResolverProvider;
import io.grpc.okhttp.OkHttpChannelProvider;


public class AudioRecordingActivity extends AppCompatActivity {

    // Media Recorder object
    private MediaRecorder mRecorder = null;
    // The max amplitudes recorded every 50ms
    private List<Integer> maxAmplitudes = new ArrayList<Integer>();
    // The list of all analyzed analysis items
    private List<AnalysisItem> analysisItems = new ArrayList<AnalysisItem>();
    // Title for the output File, listed as the timestamp of when recording started
    private String outputTitle = "";
    // Service for speech recognition
    public static final List<String> SCOPE =
            Collections.singletonList("https://www.googleapis.com/auth/cloud-platform");

    private SpeechGrpc.SpeechBlockingStub mApi = null;

    private static final String PREFS = "SpeechService";
    private static final String PREF_ACCESS_TOKEN_VALUE = "access_token_value";
    private static final String PREF_ACCESS_TOKEN_EXPIRATION_TIME = "access_token_expiration_time";
    /**
     * We reuse an access token if its expiration time is longer than this.
     */
    private static final int ACCESS_TOKEN_EXPIRATION_TOLERANCE = 30 * 60 * 1000; // thirty minutes
    /**
     * We refresh the current access token before it expires.
     */
    private static final int ACCESS_TOKEN_FETCH_MARGIN = 60 * 1000; // one minute
    private volatile AccessTokenTask mAccessTokenTask;
    private static Handler mHandler;
    // Time markers to determine duration of recording
    private long startTime, endTime;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_recording);
        //bindService(new Intent(this, SpeechService.class), mServiceConnection, BIND_AUTO_CREATE);
        final Button startRecordingButton = (Button) findViewById(R.id.startRecordingButton);
        final Button stopRecordingButton = (Button) findViewById(R.id.stopRecordingButton);
        fetchAccessToken();
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
                // Analyze the Max Amplitudes
                analysisItems.add(AudioAnalysis.analyzeAmplitudes(maxAmplitudes));

                // TODO: This is where the rest of the analysis should go
                Long actualDuration = (endTime - startTime) / 1000000000; // Calculate the duration in seconds
                analysisItems.add(AudioAnalysis.analyzeDuration(expectedDuration * 60, actualDuration));
                byte[] data = null;
                try {
                    File outputFile = new File(getExternalCacheDir().getAbsolutePath() + "/" + outputTitle);
                    FileInputStream fis = new FileInputStream(outputFile);
                    data = new byte[(int) outputFile.length()];
                    fis.read(data);
                    fis.close();
                } catch(IOException e){
                    e.printStackTrace();
                }
                RecognizeResponse response = null;
                Log.i("WAITING", "Waiting for response from speech recognition");
                try {
                    while(mApi == null) {}
                    RecognitionAudio audio = RecognitionAudio.parseFrom(data);
                    RecognitionConfig config = RecognitionConfig.newBuilder().setEncoding(RecognitionConfig.AudioEncoding.AMR_WB).setLanguageCode("en-US").setSampleRateHertz(16000).build();
                    RecognizeRequest request = RecognizeRequest.newBuilder().setAudio(audio).setConfig(config).build();
                    response = mApi.recognize(request);

                } catch(InvalidProtocolBufferException e){
                    e.printStackTrace();
                }



//                float confidenceSum = 0;
//                for (float confidence : confidenceList) {
//                    confidenceSum += confidence;
//                }
//                float averageConfidence = confidenceSum / confidenceList.size();
                if(response.getResultsCount() > 0) {
                    List<SpeechRecognitionResult> resultList = response.getResultsList();
                    for (SpeechRecognitionResult result : resultList) {
                        analysisItems.add(AudioAnalysis.analyzeRecognition(result.getAlternatives(0).getConfidence()));
                    }
                } else {
                    Log.e("AudioRecordingActivity", "NO RESULTS FROM AUDIO RECOGNITION");
                }
                // Launch intent for the ResultsSummaryActivity

                Intent resultsIntent = new Intent(getApplicationContext(), ResultsSummaryActivity.class);
                ArrayList<Parcelable> parcelableList = new ArrayList<Parcelable>();
                for (AnalysisItem item : analysisItems) {
                    parcelableList.add((Parcelable) item);
                }

                resultsIntent.putParcelableArrayListExtra("ANALYSIS_ITEMS", parcelableList);
                startActivity(resultsIntent);

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
            outputTitle = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()) + ".aac";
            mRecorder = new MediaRecorder();
            mRecorder.reset();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
            mRecorder.setOutputFile(getExternalCacheDir().getAbsolutePath() + "/" + outputTitle);
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

//        try {
//
//            speechService.startRecognizing(44100);
//            File outputFile = new File(getExternalCacheDir().getAbsolutePath() + "/" + outputTitle);
//            FileInputStream fis = new FileInputStream(outputFile);
//            byte[] data = new byte[(int) outputFile.length()];
//            fis.read(data);
//            fis.close();
//            speechService.recognize(data, data.length);
//            speechService.finishRecognizing();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


        //Paths.get(getExternalCacheDir().getAbsolutePath() + "/" + outputTitle);

    }

    private void fetchAccessToken() {
        if (mAccessTokenTask != null) {
            return;
        }
        mAccessTokenTask = new AccessTokenTask();
        mAccessTokenTask.execute();
    }

    private class AccessTokenTask extends AsyncTask<Void, Void, AccessToken> {

        @Override
        protected AccessToken doInBackground(Void... voids) {
            final SharedPreferences prefs =
                    getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            String tokenValue = prefs.getString(PREF_ACCESS_TOKEN_VALUE, null);
            long expirationTime = prefs.getLong(PREF_ACCESS_TOKEN_EXPIRATION_TIME, -1);

            // Check if the current token is still valid for a while
            if (tokenValue != null && expirationTime > 0) {
                if (expirationTime
                        > System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION_TOLERANCE) {
                    return new AccessToken(tokenValue, new Date(expirationTime));
                }
            }

            // ***** WARNING *****
            // In this sample, we load the credential from a JSON file stored in a raw resource
            // folder of this client app. You should never do this in your app. Instead, store
            // the file in your server and obtain an access token from there.
            // *******************
            final InputStream stream = getResources().openRawResource(R.raw.key);
            try {
                final GoogleCredentials credentials = GoogleCredentials.fromStream(stream)
                        .createScoped(SCOPE);
                final AccessToken token = credentials.refreshAccessToken();
                prefs.edit()
                        .putString(PREF_ACCESS_TOKEN_VALUE, token.getTokenValue())
                        .putLong(PREF_ACCESS_TOKEN_EXPIRATION_TIME,
                                token.getExpirationTime().getTime())
                        .apply();
                return token;
            } catch (IOException e) {
                Log.e("AudioRecordingActivity", "Failed to obtain access token.", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(AccessToken accessToken) {
            mAccessTokenTask = null;
            final ManagedChannel channel = new OkHttpChannelProvider()
                    .builderForAddress("speech.googleapis.com", 443)
                    .nameResolverFactory(new DnsNameResolverProvider())
                    .intercept(new GoogleCredentialsInterceptor(new GoogleCredentials(accessToken)
                            .createScoped(SCOPE)))
                    .build();
            mApi = SpeechGrpc.newBlockingStub(channel);

            // Schedule access token refresh before it expires
            if (mHandler != null) {
                mHandler.postDelayed(mFetchAccessTokenRunnable,
                        Math.max(accessToken.getExpirationTime().getTime()
                                - System.currentTimeMillis()
                                - ACCESS_TOKEN_FETCH_MARGIN, ACCESS_TOKEN_EXPIRATION_TOLERANCE));
            }
        }
    }

    private final Runnable mFetchAccessTokenRunnable = new Runnable() {
        @Override
        public void run() {
            fetchAccessToken();
        }
    };

    /**
     * Authenticates the gRPC channel using the specified {@link GoogleCredentials}.
     */
    private static class GoogleCredentialsInterceptor implements ClientInterceptor {

        private final Credentials mCredentials;

        private Metadata mCached;

        private Map<String, List<String>> mLastMetadata;

        GoogleCredentialsInterceptor(Credentials credentials) {
            mCredentials = credentials;
        }

        @Override
        public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
                final MethodDescriptor<ReqT, RespT> method, CallOptions callOptions,
                final Channel next) {
            return new ClientInterceptors.CheckedForwardingClientCall<ReqT, RespT>(
                    next.newCall(method, callOptions)) {
                @Override
                protected void checkedStart(Listener<RespT> responseListener, Metadata headers)
                        throws StatusException {
                    Metadata cachedSaved;
                    URI uri = serviceUri(next, method);
                    synchronized (this) {
                        Map<String, List<String>> latestMetadata = getRequestMetadata(uri);
                        if (mLastMetadata == null || mLastMetadata != latestMetadata) {
                            mLastMetadata = latestMetadata;
                            mCached = toHeaders(mLastMetadata);
                        }
                        cachedSaved = mCached;
                    }
                    headers.merge(cachedSaved);
                    delegate().start(responseListener, headers);
                }
            };
        }

        /**
         * Generate a JWT-specific service URI. The URI is simply an identifier with enough
         * information for a service to know that the JWT was intended for it. The URI will
         * commonly be verified with a simple string equality check.
         */
        private URI serviceUri(Channel channel, MethodDescriptor<?, ?> method)
                throws StatusException {
            String authority = channel.authority();
            if (authority == null) {
                throw Status.UNAUTHENTICATED
                        .withDescription("Channel has no authority")
                        .asException();
            }
            // Always use HTTPS, by definition.
            final String scheme = "https";
            final int defaultPort = 443;
            String path = "/" + MethodDescriptor.extractFullServiceName(method.getFullMethodName());
            URI uri;
            try {
                uri = new URI(scheme, authority, path, null, null);
            } catch (URISyntaxException e) {
                throw Status.UNAUTHENTICATED
                        .withDescription("Unable to construct service URI for auth")
                        .withCause(e).asException();
            }
            // The default port must not be present. Alternative ports should be present.
            if (uri.getPort() == defaultPort) {
                uri = removePort(uri);
            }
            return uri;
        }

        private URI removePort(URI uri) throws StatusException {
            try {
                return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), -1 /* port */,
                        uri.getPath(), uri.getQuery(), uri.getFragment());
            } catch (URISyntaxException e) {
                throw Status.UNAUTHENTICATED
                        .withDescription("Unable to construct service URI after removing port")
                        .withCause(e).asException();
            }
        }

        private Map<String, List<String>> getRequestMetadata(URI uri) throws StatusException {
            try {
                return mCredentials.getRequestMetadata(uri);
            } catch (IOException e) {
                throw Status.UNAUTHENTICATED.withCause(e).asException();
            }
        }

        private static Metadata toHeaders(Map<String, List<String>> metadata) {
            Metadata headers = new Metadata();
            if (metadata != null) {
                for (String key : metadata.keySet()) {
                    Metadata.Key<String> headerKey = Metadata.Key.of(
                            key, Metadata.ASCII_STRING_MARSHALLER);
                    for (String value : metadata.get(key)) {
                        headers.put(headerKey, value);
                    }
                }
            }
            return headers;
        }
    }
}