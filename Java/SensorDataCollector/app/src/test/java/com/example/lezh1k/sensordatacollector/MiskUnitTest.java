package com.example.lezh1k.sensordatacollector;
import android.support.annotation.NonNull;
import org.junit.Test;

import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class MiskUnitTest {

    class SensorGpsDataItem implements Comparable<SensorGpsDataItem> {
        double timestamp;
        double gpsLat;
        double gpsLon;
        double gpsAlt;
        double absNorthAcc;
        double absEastAcc;
        double absUpAcc;
        double speed;
        double course;
        double posErr;

        public SensorGpsDataItem(double timestamp, double gpsLat, double gpsLon,
                                 double gpsAlt, double absNorthAcc, double absEastAcc,
                                 double absUpAcc, double speed, double course,
                                 double posErr) {
            this.timestamp = timestamp;
            this.gpsLat = gpsLat;
            this.gpsLon = gpsLon;
            this.gpsAlt = gpsAlt;
            this.absNorthAcc = absNorthAcc;
            this.absEastAcc = absEastAcc;
            this.absUpAcc = absUpAcc;
            this.speed = speed;
            this.course = course;
            this.posErr = posErr;
        }

        @Override
        public int compareTo(@NonNull SensorGpsDataItem o) {
            return (int) (this.timestamp - o.timestamp);
        }
    }

    @Test
    public void PriorityQueueOrderTest() throws Exception {
        SensorGpsDataItem it1 = new SensorGpsDataItem(1.0,
                0.0,0.0,0.0,0.0,
                0.0,0.0,0.0,0.0, 0.0);
        SensorGpsDataItem it2 = new SensorGpsDataItem(2.0,
                0.0,0.0,0.0,0.0,
                0.0,0.0,0.0,0.0, 0.0);
        SensorGpsDataItem it3 = new SensorGpsDataItem(3.0,
                0.0,0.0,0.0,0.0,
                0.0,0.0,0.0,0.0, 0.0);

        Queue<SensorGpsDataItem> q =
                new PriorityBlockingQueue<SensorGpsDataItem>();
        q.add(it2);
        q.add(it1);
        q.add(it3);

        assertEquals(it1, q.poll());
        assertEquals(it2, q.poll());
        assertEquals(it3, q.poll());
        assertEquals(null, q.poll());

        q.add(it2);
        q.add(it1);
        q.add(it3);

        q.clear();
        assertTrue(q.isEmpty());
    }
}