package mad.location.manager.test;

import mad.location.manager.lib.Commons.SensorGpsDataItem;

import org.junit.Test;

import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

import static org.junit.Assert.assertTrue;
import static junit.framework.Assert.assertEquals;

/**
 * Created by lezh1k on 2/13/18.
 */

public class SensorGpsDataItemQueueUnitTest {

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
