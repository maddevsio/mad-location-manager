package mad.location.manager.lib.Interfaces;

import mad.location.manager.lib.Commons.Matrix;
import mad.location.manager.lib.Commons.Quaternion;

public interface IOrientationProvider {
    Quaternion getQuaternion();
    Matrix getMatrix();

    boolean start();
    void stop();
}
