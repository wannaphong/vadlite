package ethz.ch.vadlite;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.util.LinkedList;

import static ethz.ch.vadlite.ConfigVAD.FREQUENCY;
import static ethz.ch.vadlite.ConfigVAD.NO_OF_SECONDS;

/**
 * @author Third Party Code. Edited by George Boateng
 *
 *         <p/>
 *         This class represents a Thread that once started continuously collects audio.
 *         MicrophoneListeners can register to get audio buffers when they become available
 *         This class currently notifies listeners when it have 1s worth of data.
 *         <p/>
 *
 */
public class MicrophoneRecorder extends Thread {
    private static final String TAG = "VoiceService";
    public static int frequency = FREQUENCY;
    public static int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
    public static int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

    public static MicrophoneRecorder instance = null;

    private LinkedList<MicrophoneListener> listeners = new LinkedList<MicrophoneListener>();
    private boolean isRecording = false;

    public static MicrophoneRecorder getInstance() {
        if (instance == null) {
            instance = new MicrophoneRecorder();
        }
        return instance;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public void registerListener(MicrophoneListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void unregisterListener(MicrophoneListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    public void stopRecording() {
        if (isRecording) {
            isRecording = false;
            instance = null;
        }
    }

    public void startRecording() {
        if (!isRecording) {
            isRecording = true;
            instance.start();
        }
    }

    public void run() {
        try {
            // Create a new AudioRecord object to record the audio.
            int bufferSize = frequency * NO_OF_SECONDS;//AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
            //int bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
            AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency,
                    channelConfiguration, audioEncoding, bufferSize);

            short[] buffer = new short[bufferSize];
            Log.d(TAG, "VoiceService:MR: audioRecord.startRecording()");
            audioRecord.startRecording();
            int offset = 0;
            int bufferReadResult = 0;
            while (isRecording) {
                bufferReadResult += audioRecord.read(buffer, offset, bufferSize - bufferReadResult);
                offset += bufferReadResult;
                if (bufferReadResult == bufferSize) {
                    for (MicrophoneListener listener : listeners) {
                        listener.microphoneBuffer(buffer, bufferReadResult);
                    }
                    offset = 0;
                    bufferReadResult = 0;
                }
                // maybe sleep to save battery
            }
            audioRecord.stop();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "VoiceService:MR: Recording Failed");
        }
    }

    private final float mRecordingGain = 10;
    private void adjustGain(short[] audioData) {
        for(int i = 0; i < audioData.length; i++){
            audioData[i] = (short)Math.min((int)(audioData[i] * mRecordingGain), (int)Short.MAX_VALUE);
        }
    }

    public static interface MicrophoneListener {
        public void microphoneBuffer(short[] buffer, int window_size);
    }
}
