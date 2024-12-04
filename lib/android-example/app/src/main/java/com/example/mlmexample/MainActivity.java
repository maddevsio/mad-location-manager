package com.example.mlmexample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.example.mlmexample.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("mlm_filter");
    }

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Example of a call to a native method
        String txt = stringFromJNI();
        double lat = processGPS(36.5519514, 31.9801362);
        double lon = processAcc(0., 0., 0., 0.1);
        txt += "\n" + Double.toString(lat);
        txt += "\n" + Double.toString(lon);
        TextView tv = binding.sampleText;
        tv.setText(txt);
    }

    /**
     * A native method that is implemented by the 'mlmexample' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
    public native double processAcc(double x, double y, double z, double ts);
    public native double processGPS(double latitude, double longitude);
}