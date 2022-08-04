package mad.location.manager.lib.Utils;

import android.os.Build;

import java.util.UUID;

public final class UniquePsuedoID {
    public static String getUniquePsuedoID() {

        String m_szDevIDShort = "35" + (Build.BOARD.length() % 10)
                + (Build.BRAND.length() % 10)
                + (Build.CPU_ABI.length() % 10)
                + (Build.DEVICE.length() % 10)
                + (Build.MANUFACTURER.length() % 10)
                + (Build.MODEL.length() % 10)
                + (Build.PRODUCT.length() % 10);

        String serial = null;
        try {
            serial = android.os.Build.class.getField("SERIAL").get(null)
                    .toString();
            return new UUID(m_szDevIDShort.hashCode(), serial.hashCode())
                    .toString();
        } catch (Exception exception) {
            // String needs to be initialized
            serial = "serial"; // some value
        }

        return new UUID(m_szDevIDShort.hashCode(), serial.hashCode())
                .toString();
    }
}