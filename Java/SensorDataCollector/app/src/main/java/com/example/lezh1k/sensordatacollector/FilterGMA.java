package com.example.lezh1k.sensordatacollector;

import android.annotation.SuppressLint;
import android.hardware.GeomagneticField;
import android.hardware.SensorManager;
import android.opengl.*;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

/**
 * Created by lezh1k on 12/14/17.
 */

public class FilterGMA {
    public static int east = 0;
    public static int north = 1;
    public static int up = 2;

    private GPSAccKalmanFilter kfLat;
    private GPSAccKalmanFilter kfLon;
    private GPSAccKalmanFilter kfAlt;

    float[] R = new float[16];
    float[] RI = new float[16]; //inverse
    float[] I = new float[16]; //inclinations

    float[] geomagnetic = new float[4];
    float[] gyroscope = new float[4];

    float[] gravity = new float[4];
    float[] linAcc = new float[4];
    float[] rotation = new float[5];

    float[] accAxis = new float[4];
    float[] velAxis = new float[4];

    float declination = 0.0f;

    double gpsLat = 0.0;
    double gpsLon = 0.0;
    double gpsAlt = 0.0;
    double gpsHorizontalDop = 0.0;
    double gpsVerticalDop = 0.0;
    double gpsSpeed;
    double gpsCourse;

    double filteredLat = 0.0;
    double filteredLon = 0.0;
    double filteredAlt = 0.0;
    float[] filteredVel = new float[3];

    private long timeStamp;

    private DeviationCalculator m_accDeviationCalculator = null;
    private DeviationCalculator m_linAccDeviationCalculator = null;
    private DeviationCalculator m_gyrDeviationCalculator = null;
    private DeviationCalculator m_magDeviationCalculator = null;


    public FilterGMA(DeviationCalculator accDeviationCalculator,
                     DeviationCalculator linAccDeviationCalculator,
                     DeviationCalculator gyrDeviationCalculator,
                     DeviationCalculator magDeviationCalculator) {
        this.m_accDeviationCalculator = accDeviationCalculator;
        this.m_linAccDeviationCalculator = linAccDeviationCalculator;
        this.m_gyrDeviationCalculator = gyrDeviationCalculator;
        this.m_magDeviationCalculator = magDeviationCalculator;
    }

    class MadgwickTask extends AsyncTask {
        boolean needTerminate = false;
        long deltaT;
        MadgwickTask(long deltaTMs) {
            this.deltaT = deltaTMs;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            while (!needTerminate) {
                try {
                    Thread.sleep(deltaT);
//                    //todo use Madgwick here later
                    if (!SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
                        //todo log
                        continue;
                    }
                    android.opengl.Matrix.invertM(RI, 0, R, 0);
                    SensorManager.getOrientation(R, auxYPR);
                    yaw = (float) Math.toDegrees(auxYPR[0]) + declination;
                    pitch = (float) Math.toDegrees(auxYPR[1]);
                    roll = (float) Math.toDegrees(auxYPR[2]);

//                    m_ahrs.MadgwickAHRSupdate(gyroscope[0], gyroscope[1], gyroscope[2],
//                            gravity[0], gravity[1], gravity[2],
//                            geomagnetic[0], geomagnetic[1], geomagnetic[2]);
//                    R = m_ahrs.getRotationMatrix();
//                    android.opengl.Matrix.invertM(RI, 0, R, 0);
//                    yaw = m_ahrs.getYaw() + declination;
//                    pitch = m_ahrs.getPitch();
//                    roll = m_ahrs.getRoll();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }
    /*********************************************************/

    public void init(double[] linAccDeviations) {
        if (kfLon != null || kfLat != null || kfAlt != null)
            return; //already initalized
        if (gpsAlt == 0.0 && gpsLat == 0.0 && gpsLon == 0.0)
            return;

        timeStamp = System.currentTimeMillis();
        kfLon = new GPSAccKalmanFilter(Coordinates.LongitudeToMeters(gpsLon),
                velAxis[east],
                2.0,
                linAccDeviations[east],
                timeStamp);
        kfLat = new GPSAccKalmanFilter(Coordinates.LatitudeToMeters(gpsLat),
                velAxis[north],
                2.0,
                linAccDeviations[north],
                timeStamp);
        kfAlt = new GPSAccKalmanFilter(gpsAlt,
                velAxis[up],
                3.518522417151836,
                linAccDeviations[up],
                timeStamp);

        float[] fs = {(float) m_accDeviationCalculator.getFrequencyMean(),
                (float) m_gyrDeviationCalculator.getFrequencyMean(),
                (float) m_magDeviationCalculator.getFrequencyMean()};
        float mf = fs[0];
        for (float f : fs)
            mf = Math.max(mf, f);

        float[] gs = {(float) m_gyrDeviationCalculator.getSigmas()[0],
                (float) m_gyrDeviationCalculator.getSigmas()[1],
                (float) m_gyrDeviationCalculator.getSigmas()[2]};
        float gm = Math.abs(gs[0]);
        for (float g : gs)
            gm = Math.max(gm, Math.abs(g));

//        m_ahrs = new MadgwickAHRS(mf, (float) Math.sqrt(gm)); //todo use quaternion fot this
        m_ahrs = new MadgwickAHRS(mf, 0.3f);
        m_madgwickTask = new MadgwickTask((long) (100.0 / mf));
        m_madgwickTask.needTerminate = false;
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.HONEYCOMB)
            m_madgwickTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            m_madgwickTask.execute();
    }

    private MadgwickAHRS m_ahrs;
    private MadgwickTask m_madgwickTask;
    private float yaw, pitch, roll;
    private float[] auxYPR = new float[4];

    private void rebuildAccAxis() {
        android.opengl.Matrix.multiplyMV(accAxis, 0, RI, 0, this.linAcc, 0);
    }

    public void setRotation(float[] rotation) {
        System.arraycopy(rotation, 0, this.rotation, 0, rotation.length);
    }

    public void setGyroscope(float[] gyroscope, DeviationCalculator dc) {
        System.arraycopy(gyroscope, 0, this.gyroscope, 0, 3);
    }

    public void setGravity(float[] gravity, DeviationCalculator dc) {
        System.arraycopy(gravity, 0, this.gravity, 0, 3);
    }

    public void setGeomagnetic(float[] geomagnetic, DeviationCalculator dc) {
        System.arraycopy(geomagnetic, 0, this.geomagnetic, 0, 3);
    }

    public void setGpsHorizontalDop(double gpsHorizontalDop) {
        this.gpsHorizontalDop = gpsHorizontalDop;
    }

    public void setGpsVerticalDop(double gpsVerticalDop) {
        this.gpsVerticalDop = gpsVerticalDop;
    }

    private void refreshFilteredValues(double predictedPosLon,
                                       double predictedVelLon,
                                       double predictedPosLat,
                                       double predictedVelLat,
                                       double predictedPosAlt,
                                       double predictedVelAlt) {
        GeoPoint predictedPoint = Coordinates.MetersToGeoPoint(predictedPosLon,
                predictedPosLat);

        double predictedVE, predictedVN;
        predictedVE = predictedVelLon;
        predictedVN = predictedVelLat;
//        double resultantV = Math.sqrt(Math.pow(predictedVE, 2.0) + Math.pow(predictedVN, 2.0));

        filteredLat = predictedPoint.Latitude;
        filteredLon = predictedPoint.Longitude;
        filteredAlt = predictedPosAlt;

//        filteredSpeed = resultantV;
        filteredVel[east] = (float) predictedVE;
        filteredVel[north] = (float) predictedVN;
        filteredVel[up] = (float) predictedVelAlt;
    }

    public void setLinAcc(float[] linAcc) {
        System.arraycopy(linAcc, 0, this.linAcc, 0, linAcc.length);
        double predictedPosLat, predictedPosLon, predictedPosAlt;
        double predictedVelLat, predictedVelLon, predictedVelAlt;

        if (kfLat == null || kfLon == null || kfAlt == null) {
            return; //wait for initialization
        }

        timeStamp = System.currentTimeMillis();
        rebuildAccAxis();
        kfLon.Predict(timeStamp, accAxis[east]);
        kfLat.Predict(timeStamp, accAxis[north]);
        kfAlt.Predict(timeStamp, accAxis[up]);

        predictedPosLat = kfLat.getPredictedPosition();
        predictedVelLat = kfLat.getPredictedVelocity();

        predictedPosLon = kfLon.getPredictedPosition();
        predictedVelLon = kfLon.getPredictedVelocity();

        predictedPosAlt = kfAlt.getPredictedPosition();
        predictedVelAlt = kfAlt.getPredictedVelocity();

        refreshFilteredValues(predictedPosLon, predictedVelLon, predictedPosLat, predictedVelLat,
                predictedPosAlt, predictedVelAlt);
    }

    public void setGpsPosition(double gpsLat, double gpsLon, double gpsAlt) {
        this.gpsLat = gpsLat;
        this.gpsLon = gpsLon;
        this.gpsAlt = gpsAlt;

        GeomagneticField gf = new GeomagneticField((float)gpsLat, (float)gpsLon, (float)gpsAlt, System.currentTimeMillis());
        declination = gf.getDeclination();

        if (kfLat == null || kfLon == null || kfAlt == null) {
            return; //wait for initialization
        }

        kfLon.Update(Coordinates.LongitudeToMeters(gpsLon),
                velAxis[east],
                gpsHorizontalDop * 0.01,
                0.0);
        kfLat.Update(Coordinates.LatitudeToMeters(gpsLat),
                velAxis[north],
                gpsHorizontalDop * 0.01,
                0.0);
        kfAlt.Update(gpsAlt,
                -velAxis[up],
                gpsVerticalDop,
                0.0);

        double predictedPosLat = kfLat.getCurrentPosition();
        double predictedVelLat = kfLat.getCurrentVelocity();

        double predictedPosLon = kfLon.getCurrentPosition();
        double predictedVelLon = kfLon.getCurrentVelocity();

        double predictedPosAlt = kfAlt.getCurrentPosition();
        double predictedVelAlt = kfAlt.getCurrentVelocity();

        refreshFilteredValues(predictedPosLon, predictedVelLon, predictedPosLat, predictedVelLat,
                predictedPosAlt, predictedVelAlt);
    }

    public void setGpsSpeed(double gpsSpeed) {
        this.gpsSpeed = gpsSpeed;
    }

    public void setGpsCourse(double gpsCourse) {
        this.gpsCourse = gpsCourse;
    }

    public float[] getAccAxis() {
        return accAxis;
    }

    public float[] getVelAxis() {
        return velAxis;
    }

    public double getFilteredLat() {
        return filteredLat;
    }

    public double getFilteredLon() {
        return filteredLon;
    }

    public double getFilteredAlt() {
        return filteredAlt;
    }

    public float[] getFilteredVel() {
        return filteredVel;
    }

    @SuppressLint("DefaultLocale")
    public String debugString() {
        return String.format("" +
                        "declination:%f\n" +
                        "Lat:%f\nLon:%f\nAlt:%f\n" +
                        "Grav: E%.2f,N=%.2f,U=%.2f\n" +
                        "LAcc: E%.2f,N=%.2f,U=%.2f\n" +
                        "Vel: E=%.2f,N=%.2f,U:%.2f\n" +
                        "FLat:%f\nFLon:%f\nFAlt:%f\n" +
                        "FSE:%f\nFSN:%f\nFSU:%f\n" +
                        "Distance: %f\n" +
                        "yaw:%.3f--%.3f\n" +
                        "pitch:%.3f--%.3f\n" +
                        "roll:%.3f--%.3f",
                declination,
                gpsLat, gpsLon, gpsAlt,
                accAxis[east], accAxis[north], accAxis[up],
                gravity[0], gravity[1], gravity[2],
                linAcc[0], linAcc[1], linAcc[2],
                velAxis[east], velAxis[north], velAxis[up],
                filteredLat, filteredLon, filteredAlt,
                filteredVel[east], filteredVel[north], filteredVel[up],
                Coordinates.geoDistanceMeters(gpsLon, gpsLat, filteredLon, filteredLat),
                yaw, pitch, roll, rotation[0], rotation[1], rotation[2]);
    }
}
