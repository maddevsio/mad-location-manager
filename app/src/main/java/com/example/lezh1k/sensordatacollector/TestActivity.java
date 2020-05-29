package com.example.lezh1k.sensordatacollector;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.io.File;

import mad.location.manager.lib.MadLocationManagerService;
import mad.location.manager.lib.logger.Logger;
import mad.location.manager.lib.utils.ServiceHelper;

public class TestActivity extends AppCompatActivity {

    private static final String TAG = "TestActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_test);

        View start = findViewById(R.id.start);
        View stop = findViewById(R.id.stop);
        View enable_log = findViewById(R.id.enable_log);
        View disable_log = findViewById(R.id.disable_log);
        View share = findViewById(R.id.share);
        View delete_log = findViewById(R.id.delete_log);

        enable_log.setOnClickListener(view -> Logger.setEnabled(true));
        disable_log.setOnClickListener(view -> Logger.setEnabled(false));
        share.setOnClickListener(view -> {
            Logger.setEnabled(false);
            Intent intent = Logger.getShareIntent(TestActivity.this,
                    "com.example.lezh1k.sensordatacollector.provider");

            if (intent != null) {
                startActivity(intent);
            }
        });
        delete_log.setOnClickListener(view -> {
            File file = Logger.getFile();
            if (file != null && file.exists() && file.delete()) {
                Log.d(TAG, "File deleted");
            }
        });

        ServiceHelper.getService(this, MadLocationManagerService.class, value -> {
            start.setOnClickListener(view -> value.start());
            stop.setOnClickListener(view -> value.stop());
        });
    }
}
