package com.example.lezh1k.sensordatacollector.Loggers;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import com.elvishew.xlog.XLog;

import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.GGASentence;
import net.sf.marineapi.nmea.sentence.GLLSentence;
import net.sf.marineapi.nmea.sentence.GSASentence;
import net.sf.marineapi.nmea.sentence.GSVSentence;
import net.sf.marineapi.nmea.sentence.RMCSentence;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.VTGSentence;
import net.sf.marineapi.nmea.util.Position;

/**
 * Created by lezh1k on 12/25/17.
 */

public class GPSDataLogger implements GpsStatus.NmeaListener, LocationListener {

    private LocationManager m_locationManager = null;
    private Context m_context = null;

    public GPSDataLogger(LocationManager locationManager,
                         Context context) {
        m_locationManager = locationManager;
        m_context = context;
    }

    public boolean start() {
        if (m_locationManager == null)
            return false;
        if (ActivityCompat.checkSelfPermission(m_context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            m_locationManager.removeNmeaListener(this);
            m_locationManager.addNmeaListener(this);
            m_locationManager.removeUpdates(this);
            final int gpsMinTime = 1000;
            final int gpsMinDistance = 0;
            m_locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    gpsMinTime, gpsMinDistance, this);
            return true;
        }
        return false;
    }

    public void stop() {
        if (m_locationManager == null)
            return;
        m_locationManager.removeNmeaListener(this);
        m_locationManager.removeUpdates(this);
    }

    @Override
    public void onNmeaReceived(long timestamp, String msg) {

        for (int i = 0; i < 2; ++i) {
            char lc = msg.charAt(msg.length()-1);
            if (lc == '\r' || lc == '\n') //do we need to check '\r' ?
                msg = msg.substring(0, msg.length() - 1);
        }

        Position pos = null;
        Double speed = null;
        Double course = null;
        String msgToLog = null;

        try {
            Sentence s = SentenceFactory.getInstance().createParser(msg);
            switch (s.getSentenceId().toLowerCase()) {
                case "gsa":
                    GSASentence gsa = (GSASentence) s;
                    msgToLog = String.format("NMEA gsa: HDOP=%f, VDOP=%f", gsa.getHorizontalDOP(),
                            gsa.getVerticalDOP());
                    break;
                case "gga":
                    GGASentence gga = (GGASentence) s;
                    pos = gga.getPosition();
                    msgToLog = String.format("NMEA gga: HDOP=%f", gga.getHorizontalDOP());
                    break;
                case "gll":
                    GLLSentence gll = (GLLSentence) s;
                    pos = gll.getPosition();
                    break;
                case "rmc":
                    RMCSentence rmc = (RMCSentence) s;
                    pos = rmc.getPosition();
                    speed = rmc.getSpeed();
                    course = rmc.getCourse();
                    break;
                case "vtg":
                    VTGSentence vtg = (VTGSentence) s;
                    speed = vtg.getSpeedKnots();
                    course = vtg.getTrueCourse();
                    break;
                case "gsv":
                    GSVSentence gsv = (GSVSentence) s;
                    break;
                default:
                    //todo log messages that we don't handle for analyze
                    break;
            }
        } catch (Exception exc) {
            //we use exception here because net.sf.marineapi uses
            //exceptions as result code %)
            return;
        }

        if (msgToLog == null)
            msgToLog = "";

        if (pos != null) {
            msgToLog += String.format(" NMEA POS: lat=%f, lon=%f, alt=%f",
                    pos.getLatitude(), pos.getLongitude(), pos.getAltitude());
        }

        if (speed != null && course != null) {
            msgToLog += String.format(" NMEA SPEED: %f, NMEA COURSE: %f", speed.doubleValue(), course.doubleValue());
        }

        if (msgToLog.isEmpty())
            return;

        msgToLog = String.format("%d %s", System.currentTimeMillis(), msgToLog);
        XLog.d(msgToLog);
    }

    @Override
    public void onLocationChanged(Location loc) {
        String strToLog = String.format("%d GPS : pos lat=%f, lon=%f, alt=%f, hdop=%f, speed=%f, bearing=%f",
                System.currentTimeMillis(), loc.getLatitude(),
                loc.getLongitude(), loc.getAltitude(), loc.getAccuracy(),
                loc.getSpeed(), loc.getBearing());
        XLog.i(strToLog);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }
}
