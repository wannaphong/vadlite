package ethz.ch.vadlite;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends WearableActivity {

    private TextView mTextView;
    private MyBroadcastReceiver myReceiver;
    private Button recordButton;
    private Intent mServiceIntent;
    private TextView classification;
    private boolean isRecording;
    private static final int REQUEST_ALL_PERMISSION = 200;
    private boolean permissionToAllAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BODY_SENSORS, Manifest.permission.BLUETOOTH};

    private String TAG = "MAIN_ACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.text);

        //Register BroadcastReceive to receive events from the service
        myReceiver = new MyBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(VADService.BROADCAST_CLASSIFICATION);
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver, intentFilter);

        ActivityCompat.requestPermissions(this, permissions, REQUEST_ALL_PERMISSION); //Non blocking return
        boolean check = true;
        while(check) {
            //Log.i(LOG_TAG, "check");
            boolean allPermissionsAccepted = true;
            for(String permission: permissions){
                if(!(ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED)){
                    allPermissionsAccepted = false;
                }
            }

            if(allPermissionsAccepted){
                check = false;
            }
        }

        //Create intent for the service
        mServiceIntent = new Intent(this, VADService.class);

        //Set the button and the text accordingly
        recordButton = (Button) findViewById(R.id.RecordButton);
        classification = (TextView) findViewById(R.id.text);

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Stop service if button is pressed while recording, else start service
                if (isRecording) {
                    isRecording = false;
                    stopService(mServiceIntent);
                    recordButton.setText(R.string.record);
                } else {
                    isRecording = true;
                    startService(mServiceIntent);
                    recordButton.setText(R.string.stop);
                }
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //Log.i(LOG_TAG, "Length of grant Results:" + grantResults.length);
        switch (requestCode){
            case REQUEST_ALL_PERMISSION:
                if(grantResults.length > 0)
                    permissionToAllAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToAllAccepted ) finish();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        // Stop the service
        stopService(mServiceIntent);
    }

    //Receive classification and display result
    private class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            String classification_res = "";
            int param = intent.getIntExtra("param", 0);
            Log.d("Param",""+param);

            if (param == 1){
                classification_res = "SPEAKING";
                classification.setText("SPEAKING");
            }else if (param == 0){
                classification_res = "NOISE";
                classification.setText("NOISE");
            }else{
                classification_res = "SILENCE";
                classification.setText("SILENCE");
            }

            final Toast toast = Toast.makeText(context, classification_res, Toast.LENGTH_SHORT);
            toast.show();

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    toast.cancel();
                }
            }, 900); //specify delay here that is shorter than Toast.LENGTH_SHORT



        }
    };

}
