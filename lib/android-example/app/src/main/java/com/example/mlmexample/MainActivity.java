package com.example.mlmexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.mlmexample.databinding.ActivityMainBinding;
import com.example.mlmexample.loggers.AbsAccelerometerLogger;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("mlm_filter");
    }

    private AbsAccelerometerLogger m_acc_logger;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Example of a call to a native method
        String txt = stringFromJNI();
//        double lat = processGPS(36.5519514, 31.9801362);
//        double lon = processAcc(0., 0., 0., 0.1);
//        txt += "\n" + Double.toString(lat);
//        txt += "\n" + Double.toString(lon);
//        TextView tv = (TextView) findViewById(R.id.lbl_sample_text);
        TextView tv = binding.lblSampleText;
        tv.setText(txt);

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        m_acc_logger = new AbsAccelerometerLogger(sensorManager, windowManager);
        m_acc_logger.start();

        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String txt = "North: " + m_acc_logger.AccWorld()[0] + "\n";
                            txt += "East: " + m_acc_logger.AccWorld()[1] + "\n";
                            tv.setText(txt);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, 100);
    }

    /**
     * A native method that is implemented by the 'mlmexample' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
    public native double processAcc(double x, double y, double z, double ts);
    public native double processGPS(double latitude, double longitude);



}