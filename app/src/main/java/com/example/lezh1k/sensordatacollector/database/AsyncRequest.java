package com.example.lezh1k.sensordatacollector.database;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

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

        private final TrackingRepository TRACKING_REPOSITORY;

        public SaveTrackings(Context context){
            TRACKING_REPOSITORY = new TrackingRepository(context);
        }

        @Override
        protected Void doInBackground(Tracking... trackings) {
            TRACKING_REPOSITORY.saveTracking(trackings);

            return null;
        }
    }

    public static class ListTrackings extends AsyncTask<Void, Void, List<Tracking>> {

        private final TrackingRepository TRACKING_REPOSITORY;

        public ListTrackings(Context context){
            TRACKING_REPOSITORY = new TrackingRepository(context);
        }

        @Override
        protected List<Tracking> doInBackground(Void... voids) {
            List<Tracking> trackings = TRACKING_REPOSITORY.getAll(Tracking.Filter.V4);

            Log.i("DB", trackings.toString());

            return trackings;
        }
    }
}
