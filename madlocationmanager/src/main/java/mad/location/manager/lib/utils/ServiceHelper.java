package mad.location.manager.lib.utils;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import mad.location.manager.lib.Interfaces.SimpleTempCallback;

public class ServiceHelper {

    private static ServiceHelper instance = new ServiceHelper();

    private List<ServiceItem> items;

    private ServiceHelper() {
        items = new ArrayList<>();
    }

    public static <T extends BaseService> void getService(Context context, Class<T> classType, SimpleTempCallback<T> callback) {
        boolean itemFound = false;

        for (ServiceItem item : instance.items) {
            if (item.getClassType() == classType) {
                item.getService(context, callback);
                itemFound = true;
                break;
            }
        }

        if (!itemFound) {
            ServiceItem<T> item = new ServiceItem<T>(classType);
            item.getService(context, callback);
            instance.items.add(item);
        }
    }
}
