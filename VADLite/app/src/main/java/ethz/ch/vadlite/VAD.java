package ethz.ch.vadlite;

import android.util.Log;

import static ethz.ch.vadlite.ConfigVAD.CLASSIFICATION_DURATION_MS;
import static ethz.ch.vadlite.ConfigVAD.DEBUG_MODE;
import static ethz.ch.vadlite.ConfigVAD.DEVICE_NOISE_LEVEL;
import static ethz.ch.vadlite.ConfigVAD.FRAME_SIZE_MS;
import static ethz.ch.vadlite.ConfigVAD.FREQUENCY;
import static ethz.ch.vadlite.ConfigVAD.NO_OF_SECONDS;
import static ethz.ch.vadlite.ConfigVAD.NO_OF_WINDOWS_PER_DURATION;
import static ethz.ch.vadlite.ConfigVAD.RMS_THRESHOLD;
import static ethz.ch.vadlite.ConfigVAD.SAMPLES_PER_FRAME;
import static ethz.ch.vadlite.ConfigVAD.VOICE_THRESHOLD;

/**
 * This class implements a voice activity detection module. It is a 2-stage module
 * The first stage determines if there is some interesting activity in the sound signal aka nonsilence
 * If it's determined to be nonsilence, the module determines if the sound signal is speech.
 * If the sound is speech, data collection is started
 */
public class VAD {

    private static final String TAG = "VAD";

    public VAD(){

    }

    /**
     * Displays configuration of VAD
     */
    public static void displayVADConfiguration(){
        //Log VAD Parameters
        Log.d("VAD Parameters", "");
        Log.d("FRAME_SIZE_MS", ": "+FRAME_SIZE_MS);
        Log.d("NO_OF_SECONDS", ": "+ NO_OF_SECONDS);
        Log.d("FREQUENCY", ": "+FREQUENCY);
        Log.d("SAMPLES_PER_FRAME", ": "+SAMPLES_PER_FRAME);
        Log.d("CLASSIFIC_DURATION_MS", ": "+CLASSIFICATION_DURATION_MS);
        Log.d("NO_OF_WIN_PER_DURATION", ": "+NO_OF_WINDOWS_PER_DURATION);
        Log.d("VOICE_THRESHOLD", ": "+VOICE_THRESHOLD);
        Log.d("RMS_THRESHOLD", ": "+RMS_THRESHOLD);
    }

    /**
     * Classifies all frames
     * @param buffer
     * @param window_size
     * @return classification
     */

    public static int classifyFrame(short[] buffer, int window_size){
        int classification = 0;
        int voiced = 0; //counts number of speech classifications in window

        //Extract features and classify for each frame
        for(int k=0;k<window_size;k+=SAMPLES_PER_FRAME){
//            if (k == 0) {
//                Log.i(TAG, "About to extract and classify first frame... " + System.currentTimeMillis());
//            }
            double[] features = FeatureExtractor.ComputeFeaturesForFrame(buffer,SAMPLES_PER_FRAME,k);
            try {
                //Classify sample

                if (Classifier.Classify(features)== true){
                    voiced++;
                }
//
//                if (k == 0) {
//                    Log.i(TAG, "Done classifying first sample... " + System.currentTimeMillis());
//                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //Check if number of samples classified as voiced is greater than threshold
        if (voiced >= VOICE_THRESHOLD){
            classification = 1;
        }

        if (DEBUG_MODE) {
            Log.i("Voice Count", "" + voiced);
        }

        return classification;
    }

    /**
     *  Checks and returns if sound sample is silence
     * @param buffer
     * @return isSilence
     */
    public static boolean isSilence(short[] buffer){
        boolean isSilence = true;

        //Calculate energy
        double energy = calculateRMS(buffer);

        //Check if above threshold
        if (energy > RMS_THRESHOLD){
            isSilence = false;
        }

        Log.i( "RMS:", ""+energy);
        Log.d("Silence:", ""+isSilence);
        return isSilence;
    }


    /**
     * Estimate the RMS of the sound sample
     * @param buffer
     * @return
     */
    public static double calculateRMS(short[] buffer) {

        double min = 1;
        double minRaw = Short.MAX_VALUE;
        double max = -1;
        double meanAbsolute =0;
        double sumAbsolute =0;
        double energy = 0;
        double mappedSample;
        int minIndex = -1;
        int i = 0;
        int length = 0;

        for (short sample: buffer){
            mappedSample = (double) sample/ Math.abs(Short.MIN_VALUE);

            if(Math.abs(mappedSample) > DEVICE_NOISE_LEVEL){
                energy+=mappedSample*mappedSample;
                sumAbsolute+= Math.abs(mappedSample);

                if (mappedSample< min){
                    minIndex = i;
                }

                min = Math.min(min,mappedSample);
                max = Math.max(max,mappedSample);
                minRaw = Math.min(minRaw,sample);

                length++;
            }

            i++;
        }

        if (length == 0){
            return 0;
        }

        meanAbsolute = sumAbsolute/length;
        double rms = Math.sqrt(energy/length);

//        if (DEBUG_MODE) {
//            Log.d("No Of Samples:", "" + i);
//            Log.d("RMS:", "" + rms);
//            Log.d("Max:", "" + max);
//            Log.d("Min:", "" + min);
//            Log.d("Min Raw:", "" + minRaw);
//            Log.d("Min Index:", "" + minIndex);
//            Log.d("Mean Absolute:", "" + meanAbsolute);
//        }
        return rms;
    }
}
