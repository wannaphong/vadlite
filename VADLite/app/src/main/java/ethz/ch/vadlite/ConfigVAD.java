package ethz.ch.vadlite;

public class ConfigVAD {

    //VAD Constants
    public static final double DEVICE_NOISE_LEVEL = 0.01; //the level of noise by device
    public static final double RMS_THRESHOLD = 0.012; //If rms is above this threshold, then the sample is no silence
    public static int FIRST_N_SAMPLES_DISCARDED = 9000; //(int)(0.1*WINDOW_SIZE); //discard the first N samples of values during the energy calculation because of blip when microphone starts

    public static final int FREQUENCY = 8000; //8kHz
    public static final int NO_OF_SECONDS = 1; //number of seconds
    public static final int MILLI = 1000;
    public static final int CLASSIFICATION_DURATION_MS = NO_OF_SECONDS * MILLI; //1000 ms for 1 seconds, 5000 ms for 5 seconds
    public static final int FRAME_SIZE_MS = 25; // each frame is 25ms
    public static final int SAMPLES_PER_FRAME = (FRAME_SIZE_MS * FREQUENCY)/MILLI; //(8000*25)/100 or 8*25 = 200 samples
    public static final int WINDOW_SIZE = FREQUENCY * NO_OF_SECONDS; //8000*5 = 40,000
    public static final int NO_OF_WINDOWS_PER_DURATION = CLASSIFICATION_DURATION_MS / FRAME_SIZE_MS; //40 for 1 sec, 200 for 5 sec
    public static int VOICE_THRESHOLD = NO_OF_WINDOWS_PER_DURATION/2; //40 for 1 sec, 100 for 5 sec; //threshold for deciding classification of window is half the total count
    public static boolean DEBUG_MODE = true;

    //Speech Classification Constants
    static boolean shouldNormalize = true;

    static double MEAN[] = {2.334649258003022,2.9855268412584843,0.8007077050032018,1.6121238341478126,2.5420119663666485,3.9760475563809337,4.505005459298363,3.1940493141144564,4.744776155719241,3.3487813323515097,4.346105980509734,3.1497282080307016};
    static double SCALER[] = {3.65992716548593,5.084732870471104,4.0378754604764255,4.962723442860491,4.539172546926978,4.681704214247609,4.691733947847009,4.4584555890755295,4.578011601436243,4.135433185126071,4.31499521462829,4.552401860839401};
    static double COEFFICIENTS[] ={0.3251518362067988,0.43279679341491056,0.037554050856834914,-0.028409472836628983,0.035941544718749455,0.2154260391857937,0.3471027571020563,0.12620432322739827,0.00012992747646832107,0.10651790931452468,-0.046240337104146725,0.2089594743788787};
    static double INTERCEPT = 0.0182169;

//    static double MEAN[] = {};
//    static double SCALER[] = {};
//    static double COEFFICIENTS[] ={};
//    static double INTERCEPT = ;

}
