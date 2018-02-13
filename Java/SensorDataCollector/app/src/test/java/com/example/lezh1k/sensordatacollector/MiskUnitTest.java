package com.example.lezh1k.sensordatacollector;

import com.example.lezh1k.sensordatacollector.CommonClasses.SensorGpsDataItem;

import org.junit.Test;

import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

import static org.junit.Assert.assertTrue;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class MiskUnitTest {


    @Test
    public void PriorityQueueOrderTest() throws Exception {
        SensorGpsDataItem it1 = new SensorGpsDataItem(1.0,
                0.0,0.0,0.0,0.0,
                0.0,0.0,0.0,0.0, 0.0, 0.0, 0.0);
        SensorGpsDataItem it2 = new SensorGpsDataItem(2.0,
                0.0,0.0,0.0,0.0,
                0.0,0.0,0.0,0.0, 0.0,0.0, 0.0);
        SensorGpsDataItem it3 = new SensorGpsDataItem(3.0,
                0.0,0.0,0.0,0.0,
                0.0,0.0,0.0,0.0, 0.0,0.0, 0.0);

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