package com.example.lezh1k.sensordatacollector.database;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Looper;
import android.widget.Toast;

import com.example.lezh1k.sensordatacollector.R;
import com.example.lezh1k.sensordatacollector.database.model.Accelerometer;
import com.example.lezh1k.sensordatacollector.database.model.Gyroscope;
import com.example.lezh1k.sensordatacollector.database.model.Magnetometer;
import com.example.lezh1k.sensordatacollector.database.model.Tracking;
import com.example.lezh1k.sensordatacollector.database.repository.AccelerometerRepository;
import com.example.lezh1k.sensordatacollector.database.repository.GyroscopeRepository;
import com.example.lezh1k.sensordatacollector.database.repository.MagnetometerRepository;
import com.example.lezh1k.sensordatacollector.database.repository.TrackingRepository;

public abstract class AsyncRequest {

    public static class SetupDatabase extends AsyncTask<Context, Void, AppDatabase> {

        @Override
        protected AppDatabase doInBackground(Context... context) {
            if (context.length > 0) {
                return AppDatabase.getAppDatabase(context[0]);
            }

            return null;
        }
    }

    public static class SaveTrackings extends AsyncTask<Tracking, Void, Void> {

        private final TrackingRepository REPOSITORY;

        public SaveTrackings(Context context) {
            REPOSITORY = new TrackingRepository(context);
        }

        @Override
        protected Void doInBackground(Tracking... trackings) {
            REPOSITORY.saveTracking(trackings);

            return null;
        }
    }

    public static class SaveGyroscope extends AsyncTask<Gyroscope, Void, Void> {

        private final GyroscopeRepository REPOSITORY;

        public SaveGyroscope(Context context) {
            REPOSITORY = new GyroscopeRepository(context);
        }

        @Override
        protected Void doInBackground(Gyroscope... gyroscopes) {
            REPOSITORY.saveGyroscope(gyroscopes);

            return null;
        }

    }

    public static class SaveAccelerometer extends AsyncTask<Accelerometer, Void, Void> {

        private final AccelerometerRepository REPOSITORY;

        public SaveAccelerometer(Context context) {
            REPOSITORY = new AccelerometerRepository(context);
        }

        @Override
        protected Void doInBackground(Accelerometer... accelerometers) {
            REPOSITORY.saveAccelerometer(accelerometers);

            return null;
        }

    }

    public static class SaveMagnetometer extends AsyncTask<Magnetometer, Void, Void> {

        private final MagnetometerRepository REPOSITORY;

        public SaveMagnetometer(Context context) {
            REPOSITORY = new MagnetometerRepository(context);
        }

        @Override
        protected Void doInBackground(Magnetometer... magnetometers) {
            REPOSITORY.saveMagnetometer(magnetometers);

            return null;
        }

    }

    public static class ClearDatabase extends AsyncTask<Context, Void, Context> {

        private final AppDatabase appDatabase;
        private boolean execute = true;

        public ClearDatabase(Context context) {
            appDatabase = AppDatabase.getAppDatabase(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Context doInBackground(Context... contexts) {

            if (execute && contexts != null && contexts.length > 0) {
                appDatabase.clearAllTables();
                return contexts[0];

            }
            return null;

        }

        @Override
        protected void onPostExecute(Context context) {
            super.onPostExecute(context);

            if (execute && context != null) {
                Toast.makeText(context, "Tracking data successfully cleared.", Toast.LENGTH_SHORT).show();
            }

        }

    }
}
