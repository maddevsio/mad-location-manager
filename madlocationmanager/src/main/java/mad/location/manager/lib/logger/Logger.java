package mad.location.manager.lib.logger;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Logger {
    private static final String TAG = "Logger";
    private static final String LOGS_DIRECTORY_NAME = "logs";
    private static final String LOG_FILE_NAME = "mlm_log.txt";
    private static final int DELAY = 1000;
    private static final int MAX_ATTEMPTS_COUNT = 2;
    private static final byte[] LINE_BREAKER = "\n".getBytes();

    private static Logger instance;

    private File file;
    private FileOutputStream stream;
    private List<String> logQueue;
    private Handler fileWriterHandler;
    private Runnable fileWriterRunnable;

    private boolean enabled;

    private int attemptsCount;

    public static void init(Context context) {
        try {
            instance = new Logger(context);
        } catch (Exception e) {
            Log.e(TAG, "Logger not initialized");
        }
    }

    private Logger(Context context) throws Exception {
        File logsDirectory = new File(context.getCacheDir(), LOGS_DIRECTORY_NAME);

        if (!logsDirectory.exists()) {
            if (logsDirectory.mkdir()) {
                Log.d(TAG, "Logs directory created");
            } else {
                Log.e(TAG, "Logs directory not created");
                throw new Exception();
            }
        }

        file = new File(logsDirectory, LOG_FILE_NAME);

        if (!file.exists()) {
            if (file.createNewFile()) {
                Log.d(TAG, "Log file created");
            } else {
                Log.e(TAG, "Log file not created");
                throw new Exception();
            }
        }

        attemptsCount = 0;
        logQueue = new ArrayList<>();

        fileWriterHandler = new Handler();
        fileWriterRunnable = new Runnable() {
            @Override
            public void run() {
                if (!logQueue.isEmpty()) {
                    attemptsCount = 0;
                }
                if (attemptsCount++ == MAX_ATTEMPTS_COUNT) {
                    closeOutputStream();
                    return;
                }
                if (openOutputStream()) {
                    Iterator<String> iterator = logQueue.iterator();
                    while (iterator.hasNext()) {
                        writeToFile(iterator.next());
                        iterator.remove();
                    }

                    fileWriterHandler.removeCallbacks(fileWriterRunnable);
                    fileWriterHandler.postDelayed(fileWriterRunnable, DELAY);
                }
            }
        };
    }

    public static void setEnabled(boolean enabled) {
        if (instance != null) {
            instance.enabled = enabled;

            if (!enabled) {
                instance.logQueue.clear();

                instance.fileWriterHandler.removeCallbacks(instance.fileWriterRunnable);
                instance.closeOutputStream();
            }
        } else {
            Log.e(TAG, "Logger not initialized");
        }
    }

    public static void write(String log) {
        if (instance != null) {
            if (instance.enabled) {
                instance.logQueue.add(log);
                if (instance.stream == null) {
                    instance.fileWriterHandler.removeCallbacks(instance.fileWriterRunnable);
                    instance.fileWriterHandler.post(instance.fileWriterRunnable);
                }
            }
        } else {
            Log.e(TAG, "Logger not initialized");
        }
    }

    public static File getFile() {
        if (instance != null) {
            if (instance.file != null && instance.file.exists()) {
                return instance.file;
            }
        } else {
            Log.e(TAG, "Logger not initialized");
        }
        return null;
    }

    /**
     * To share log file, you need to add {@link android.support.v4.content.FileProvider}
     * <provider
     *     android:name="android.support.v4.content.FileProvider"
     *     android:authorities="{@param providerPath}"
     *     android:exported="false"
     *     android:grantUriPermissions="true">
     *         <meta-data
     *             android:name="android.support.FILE_PROVIDER_PATHS"
     * android:resource="@xml/provider_paths" />
     * </provider>
     * into AndroidManifest. Inside provider_paths, you need to add
     * <cache-path name="log_file" path="logs/"/>
     * <p>
     * Attention! Before sharing log_file, please stop Logger by using Logger.setEnabled(false).
     * Otherwise log_file won't contain all logs.
     **/
    public static Intent getShareIntent(Context context, String providerPath) {
        File file = getFile();
        if (file != null) {
            Uri uri = FileProvider.getUriForFile(context, providerPath, file);

            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
            sendIntent.setType("text/*");

            return Intent.createChooser(sendIntent, null);
        }
        return null;
    }

    private boolean openOutputStream() {
        try {
            if (stream == null) {
                stream = new FileOutputStream(file);
            }
            return true;
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Couldn't open output stream");
        }
        return false;
    }

    private void closeOutputStream() {
        try {
            if (stream != null) {
                stream.close();
            }
            stream = null;
        } catch (IOException e) {
            Log.e(TAG, "Couldn't close output stream");
        }
    }


    private void writeToFile(String log) {
        try {
            Log.d(TAG, String.format("Writing log: [%s]", log));
            stream.write(log.getBytes());
            stream.write(LINE_BREAKER);
        } catch (IOException e) {
            Log.e(TAG, String.format("Couldn't write [%s], skipping it", log));
        }
    }
}
