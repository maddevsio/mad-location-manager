package mad.location.manager.lib.logger;

import android.os.AsyncTask;
import android.os.Build;
import android.util.Base64;

import androidx.annotation.RequiresApi;

import com.elvishew.xlog.XLog;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.common.util.CollectionUtils;
import com.maddevs.logtransferobject.Log;
import com.maddevs.logtransferobject.Logs;
import com.maddevs.logtransferobject.Zipper;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import mad.location.manager.lib.Services.Settings;
import mad.location.manager.lib.Utils.UniquePsuedoID;

public class RawLogSenderTask extends AsyncTask<List<Log>, Integer, Integer> {
    private static final String STARTING = "Send raw log to server...";
    private static final String FINISHED = "Raw log was sended";

    private String CONNECTION_STRING;
    private HttpClient httpclient = new DefaultHttpClient();
    private ObjectMapper objectMapper = new ObjectMapper();
    private final Settings settings = Settings.getInstance();

    public RawLogSenderTask(String uniquePhoneID) {
        super();
        CONNECTION_STRING = String.format("http://%s/api/collector/%s", settings.server, uniquePhoneID);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected Integer doInBackground(List<Log>... records) {
        List<Log> payload = records[0];
        XLog.i("=== SEND %s RECORDS ==", payload.size());
        String result = sendDataToServer(payload);
        if (result.length()==0) {
            return 0;
        }
        return Integer.parseInt(result);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private String sendDataToServer(List<Log> data) {
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(CONNECTION_STRING);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "text/plain");
            Logs logs = Logs.builder().logs(data).build();
            String serialised = objectMapper.writeValueAsString(logs);
            byte[] compressed = Zipper.compress(serialised.getBytes(StandardCharsets.UTF_8));
            String payload = new String(
                    Base64.encode(compressed, Base64.DEFAULT),
                    StandardCharsets.UTF_8
            );
            urlConnection.setRequestProperty("Content-Length", Integer.toString(payload.length()));
            urlConnection.setDoOutput(true);

            OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
            out.write(payload.getBytes(StandardCharsets.UTF_8));
            out.flush();
            out.close();

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            return readStream(in);
        }
        catch (Exception exc) {
            XLog.e(exc.getMessage());
        }
        finally {
            if (urlConnection!=null) {
                urlConnection.disconnect();
            }
        }

        return "";
    }

    private String readStream(InputStream in) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.substring(0, sb.length()-1);
    }
}