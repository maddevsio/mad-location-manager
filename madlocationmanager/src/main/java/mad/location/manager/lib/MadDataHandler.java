package mad.location.manager.lib;

import android.hardware.GeomagneticField;
import android.location.Location;
import android.os.Handler;
import android.os.SystemClock;

import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

import mad.location.manager.lib.Commons.Coordinates;
import mad.location.manager.lib.Commons.GeoPoint;
import mad.location.manager.lib.Commons.SensorGpsDataItem;
import mad.location.manager.lib.Commons.Utils;
import mad.location.manager.lib.Filters.GPSAccKalmanFilter;
import mad.location.manager.lib.Interfaces.SimpleTempCallback;
import mad.location.manager.lib.logger.LogBuilder;
import mad.location.manager.lib.logger.Logger;
import mad.location.manager.lib.utils.Settings;

import static mad.location.manager.lib.MadLocationManager.MLM_PROVIDER;

class MadDataHandler {

    private GPSAccKalmanFilter kalmanFilter;
    private Queue<SensorGpsDataItem> sensorDataQueue;
    private Handler nextPositionHandler;
    private Runnable nextPositionRunnable;
    private SimpleTempCallback<Location> callback;

    private double accelerationDeviation;
    private double velFactor;
    private double posFactor;
    private int positionMinTime;

    private double magneticDeclination;


    MadDataHandler() {
        accelerationDeviation = Settings.DEFAULT_ACCELEROMETER_DEVIATION;
        velFactor = Settings.DEFAULT_VEL_FACTOR;
        posFactor = Settings.DEFAULT_POS_FACTOR;
        positionMinTime = Settings.DEFAULT_SENSOR_POSITION_MIN_TIME;

        magneticDeclination = 0.0;

        sensorDataQueue = new PriorityBlockingQueue<>();

        nextPositionHandler = new Handler();
        nextPositionRunnable = new Runnable() {
            @Override
            public void run() {
                getNextPosition();
            }
        };
    }

    MadDataHandler setCallback(SimpleTempCallback<Location> callback) {
        this.callback = callback;
        return this;
    }

    MadDataHandler setAccelerationDeviation(double accelerationDeviation) {
        this.accelerationDeviation = accelerationDeviation;
        return this;
    }

    MadDataHandler setVelFactor(double velFactor) {
        this.velFactor = velFactor;
        return this;
    }

    MadDataHandler setPosFactor(double posFactor) {
        this.posFactor = posFactor;
        return this;
    }

    MadDataHandler setPositionMinTime(int positionMinTime) {
        this.positionMinTime = positionMinTime;
        return this;
    }

    void start() {
        reset();
        nextPositionHandler.removeCallbacks(nextPositionRunnable);
        nextPositionHandler.postDelayed(nextPositionRunnable, positionMinTime);
    }

    void stop() {
        reset();
        nextPositionHandler.removeCallbacks(nextPositionRunnable);
    }

    private void reset() {
        kalmanFilter = null;
        sensorDataQueue.clear();
    }

    void locationChanged(Location location) {
        long timeStamp = Utils.nano2milli(location.getElapsedRealtimeNanos());
        double velErr = location.getAccuracy() * 0.1;

        Logger.write(LogBuilder.buildGpsData(timeStamp, location, velErr));

        GeomagneticField geomagneticField = new GeomagneticField(
                (float) location.getLatitude(),
                (float) location.getLongitude(),
                (float) location.getAltitude(),
                location.getTime()
        );

        magneticDeclination = geomagneticField.getDeclination();

        if (kalmanFilter == null) {
            Logger.write(LogBuilder.buildKalmanAlloc(timeStamp, location, accelerationDeviation));

            float speed = location.getSpeed();
            float course = location.getBearing();
            double xVel = speed * Math.cos(course);
            double yVel = speed * Math.sin(course);

            kalmanFilter = new GPSAccKalmanFilter(
                    false, //todo move to settings
                    Coordinates.longitudeToMeters(location.getLongitude()),
                    Coordinates.latitudeToMeters(location.getLatitude()),
                    xVel,
                    yVel,
                    accelerationDeviation,
                    location.getAccuracy(),
                    timeStamp,
                    velFactor,
                    posFactor);

        } else {
            sensorDataQueue.add(new SensorGpsDataItem(
                    timeStamp,
                    location.getLatitude(),
                    location.getLongitude(),
                    location.getAltitude(),
                    location.getSpeed(),
                    location.getBearing(),
                    location.getAccuracy(),
                    velErr,
                    magneticDeclination
            ));
        }
    }

    void absAccelerationChanged(float[] absAcceleration) {
        long timeStamp = Utils.nano2milli(SystemClock.elapsedRealtimeNanos());

        Logger.write(LogBuilder.buildAbsAccData(timeStamp, absAcceleration));

        if (kalmanFilter != null) {
            sensorDataQueue.add(new SensorGpsDataItem(
                    timeStamp,
                    absAcceleration[0]/* east */,
                    absAcceleration[1]/* north */,
                    absAcceleration[2]/* up */,
                    magneticDeclination
            ));
        }
    }

    private void getNextPosition() {
        SensorGpsDataItem item;

        double lastTimeStamp = 0.0;

        while ((item = sensorDataQueue.poll()) != null) {
            if (item.getTimestamp() < lastTimeStamp) {
                continue;
            }

            lastTimeStamp = item.getTimestamp();

            //warning!!!
            if (item.getDataType() == SensorGpsDataItem.ACCELERATION_DATA) {
                handlePredict(item);

            } else {
                handleUpdate(item);

                if (callback != null) {
                    callback.onCall(locationAfterUpdateStep(item));
                }
            }
        }

        nextPositionHandler.removeCallbacks(nextPositionRunnable);
        nextPositionHandler.postDelayed(nextPositionRunnable, positionMinTime);
    }

    private void handlePredict(SensorGpsDataItem item) {
        Logger.write(LogBuilder.buildKalmanPredict(item));
        kalmanFilter.predict(item.getTimestamp(), item.getAbsEastAcc(), item.getAbsNorthAcc());
    }

    private void handleUpdate(SensorGpsDataItem item) {
        double xVelocity = item.getSpeed() * Math.cos(item.getCourse());
        double yVelocity = item.getSpeed() * Math.sin(item.getCourse());

        Logger.write(LogBuilder.buildKalmanUpdate(item, xVelocity, yVelocity));

        kalmanFilter.update(
                item.getTimestamp(),
                Coordinates.longitudeToMeters(item.getGpsLon()),
                Coordinates.latitudeToMeters(item.getGpsLat()),
                xVelocity,
                yVelocity,
                item.getPosErr(),
                item.getVelErr()
        );
    }

    private Location locationAfterUpdateStep(SensorGpsDataItem item) {
        GeoPoint pp = Coordinates.metersToGeoPoint(
                kalmanFilter.getCurrentX(),
                kalmanFilter.getCurrentY()
        );

        double xVelocity = kalmanFilter.getCurrentXVel();
        double yVelocity = kalmanFilter.getCurrentYVel();
        double speed = Math.sqrt(xVelocity * xVelocity + yVelocity * yVelocity); //scalar speed without bearing

        Location location = new Location(MLM_PROVIDER);
        location.setLatitude(pp.Latitude);
        location.setLongitude(pp.Longitude);
        location.setAltitude(item.getGpsAlt());
        location.setBearing((float) item.getCourse());
        location.setSpeed((float) speed);
        location.setTime(System.currentTimeMillis());
        location.setElapsedRealtimeNanos(System.nanoTime());
        location.setAccuracy((float) item.getPosErr());

        return location;
    }
}
