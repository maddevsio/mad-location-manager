package com.example.lezh1k.sensordatacollector;

import android.content.Context;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;


/*****************************************************************/

public class LogUtils {
    public static final String TAG = Commons.AppName + LogUtils.class.getName();
    /**
     * Инициализирует androlog
     */
    public static void init(Context context) {
//        try {
//            Properties configuration = new Properties();
//            String email = "lezh1k.vohrer@gmail.com";
//            configuration.load(new StringReader("androlog.active=true\n" +
//                    "androlog.default.level=DEBUG\n" +
//                    "androlog.report.active=true\n" +
//                    "androlog.report.add-timestamp=true\n" +
//                    "androlog.report.reporters=de.akquinet.android.androlog.reporter.MailReporter\n" +
//                    "androlog.reporter.mail.address=" + email + "\n" +
//                    "androlog.report.log.items=1000\n" +
//                    "androlog.report.default.level=DEBUG"));
//            Log.init(context);
//            Log.configure(configuration);
//            Log.deactivateLogging();
//            Log.activateReporting();
//        } catch (IOException e) {
//            Log.e(TAG, "Cannot initialize logging");
//        }
    }
}
