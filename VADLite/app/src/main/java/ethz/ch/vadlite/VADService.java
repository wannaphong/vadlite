package ethz.ch.vadlite;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import static ethz.ch.vadlite.ConfigVAD.DEBUG_MODE;
import static ethz.ch.vadlite.ConfigVAD.SAMPLES_PER_FRAME;
import static ethz.ch.vadlite.ConfigVAD.VOICE_THRESHOLD;
import static ethz.ch.vadlite.ConfigVAD.voiceCount;
import static ethz.ch.vadlite.VAD.classifyFrame;
import static ethz.ch.vadlite.VAD.displayVADConfiguration;
import static ethz.ch.vadlite.VAD.isSilence;

/**
 * Third Party Code
 * Edited by George Boateng
 */
public class VADService extends Service implements MicrophoneRecorder.MicrophoneListener {

    public static final String BROADCAST_CLASSIFICATION = "com.speechapp.CLASSIFICATION";
    public static final String TAG = "Service";
    private MicrophoneRecorder recorder;
    private int speechCount = 0;
    private int noiseCount = 0;
    private int totalCount = 0;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        if (DEBUG_MODE){
            displayVADConfiguration();
        }

        recorder = MicrophoneRecorder.getInstance();
        recorder.registerListener(VADService.this);
        recorder.startRecording();

        // If we get killed, after returning from here, don't restart
        return START_NOT_STICKY;
    }

    @Override
    //Implementation of method that was declared in an interface in MicrophoneRecorder
    public void microphoneBuffer(short[] buffer, int window_size) {
//        Log.i(TAG, "About to classify total duration... " + System.currentTimeMillis());

        int classification = 2;
        boolean checkSilence = isSilence(buffer);

        if (!checkSilence) {
            if (DEBUG_MODE) {
                Log.d("Classify", "Buffer length " + buffer.length);
            }
            totalCount++;
            classification = classifyFrame(buffer, window_size);
//            Log.i(TAG, "Done classification of duration... " + System.currentTimeMillis());
        }else{
            voiceCount = -1; // set voice count to -1
        }



        if (DEBUG_MODE) {
            if (classification == 1) {
                Log.d(TAG, "Speaking");
                speechCount++;
            } else if (classification == 0) {
                Log.d(TAG, "Noise");
                noiseCount++;
            } else {
                Log.d(TAG, "Silence");
            }


            Log.i(TAG, "Speech Count: " + speechCount + " Perc: " + (100*speechCount)/totalCount + "%");
            Log.i(TAG, "Noise Count: " + noiseCount +  " Perc: " + (100*noiseCount)/totalCount + "%");


        }
        sendSpeechStatusToUI(classification);
    }


    //Sends classification result to UI to be displayed
    public void  sendSpeechStatusToUI(int classification) {
        Intent intent = new Intent(BROADCAST_CLASSIFICATION);
        intent.putExtra("param", classification);
        intent.putExtra("voice count", voiceCount);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onDestroy() {
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
        recorder.stopRecording(); //stop recording
        recorder.unregisterListener(this); //unregister
        super.onDestroy();
    }

}