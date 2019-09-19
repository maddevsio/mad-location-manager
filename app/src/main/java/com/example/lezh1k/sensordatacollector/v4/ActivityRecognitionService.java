package com.example.lezh1k.sensordatacollector.v4;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

public class ActivityRecognitionService extends IntentService {

    private final String TAG = getClass().getName();

    public static final String UNKNOWN = "Unknown";
    public static final String IN_VEHICLE = "In Vehicle";
    public static final String ON_BICYCLE = "On Bicycle";
    public static final String ON_FOOT = "On Foot";
    public static final String STILL = "Still";
    public static final String TILTING = "Tilting";
    public static final String ACTIVITY_RECOGNITION_INTENT_FLAG = "ag.strider.scout.ACTIVITY_RECOGNITION_DATA";

    public ActivityRecognitionService() {
        super("Strider Scout Activity Recognition Service");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            Intent i = new Intent(ACTIVITY_RECOGNITION_INTENT_FLAG);
            i.putExtra("Activity", getType(result.getMostProbableActivity().getType()));
            i.putExtra("Confidence", result.getMostProbableActivity().getConfidence());
            sendBroadcast(i);
        }
    }

    private String getType(int type) {
        String returnType;

        switch (type) {
            case DetectedActivity.IN_VEHICLE:
                returnType = IN_VEHICLE;
                break;

            case DetectedActivity.ON_BICYCLE:
                returnType = ON_BICYCLE;
                break;

            case DetectedActivity.ON_FOOT:
                returnType = ON_FOOT;
                break;

            case DetectedActivity.STILL:
                returnType = STILL;
                break;

            case DetectedActivity.TILTING:
                returnType = TILTING;
                break;

            default:
                returnType = UNKNOWN;
        }

        return returnType;
    }
}
