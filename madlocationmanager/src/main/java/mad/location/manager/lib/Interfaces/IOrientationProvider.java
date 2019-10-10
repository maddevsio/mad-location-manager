package mad.location.manager.lib.Interfaces;

public interface IOrientationProvider {
    void GetQuaternion(float[] rotationMatrix, float[] rotationMatrixInv);
    void GetMatrix(float[] absAcceleration);

    interface IProvide {
        void start();
        void stop();
    }

    interface IClient {
        void GetQuaternion(float[] rotationMatrix, float[] rotationMatrixInv);
        void GetMatrix(float[] absAcceleration);
    }
}
