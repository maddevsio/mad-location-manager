package mad.location.manager.lib.Provider;

import android.content.Context;

import mad.location.manager.lib.Filters.MadgwickAHRS;
import mad.location.manager.lib.Interfaces.IOrientationProvider;
import mad.location.manager.lib.Interfaces.ISensorDataProvider;

public class MadgwickOrientationProvider implements ISensorDataProvider.Client, IOrientationProvider.IProvide{

    private IOrientationProvider.IClient client;
    private ISensorDataProvider.Provider provider;
    private MadgwickAHRS madgwickAHRS;

    public MadgwickOrientationProvider (IOrientationProvider.IClient client, Context context) {
        this.provider = new SensorDataProvider(this, context);
        this.client = client;
        madgwickAHRS = new MadgwickAHRS(3, 2);
    }

    @Override
    public void absAccelerationDate(float[] absAcceleration) {
        madgwickAHRS.MadgwickAHRSupdate(1, 1, 3,
                                        1, 1, 4,
                                        1, 2, 4);
        client.GetMatrix(absAcceleration);
    }

    @Override
    public void rotationMatrixInv(float[] rotationMatrixInv, float[] rotationMatrix) {
        client.GetQuaternion(rotationMatrixInv, rotationMatrix);
    }

    @Override
    public void start() {
        provider.start();
    }

    @Override
    public void stop() {
        provider.stop();
    }
}
