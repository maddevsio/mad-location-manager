package mad.location.manager.lib.logger;

import android.os.AsyncTask;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.elvishew.xlog.XLog;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.common.util.CollectionUtils;
import com.maddevs.logtransferobject.Log;
import com.maddevs.logtransferobject.Zipper;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import mad.location.manager.lib.Services.Settings;
import mad.location.manager.lib.Utils.UniquePsuedoID;

public class RawLogSenderTask extends AsyncTask<List<Log>, Integer, Integer> {
    private static final String STARTING = "Send raw log to server...";
    private static final String FINISHED = "Raw log was sended";

    private String CONNECTION_STRING;
    private final String uniquePhoneID = UniquePsuedoID.getUniquePsuedoID();
    private HttpClient httpclient = new DefaultHttpClient();
    private ObjectMapper objectMapper = new ObjectMapper();
    private final Settings settings = Settings.getInstance();
    List<Log> data;
    public RawLogSenderTask() {
        super();
        CONNECTION_STRING = String.format("http://%s/api/collector/%s", settings.server, uniquePhoneID);
    }

//    @Override
//    protected void onPreExecute() {
//        super.onPreExecute();
//    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected Integer doInBackground(List<Log>... records) {
        synchronized (this) {
            this.data = new ArrayList<>(records[0]);
        }

        XLog.i("=== SEND %s RECORDS ==", records[0].size());

        sendDataToServer(this.data);

        return 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private String sendDataToServer(List<Log> data)
    {
        try {
            HttpPost request = new HttpPost(CONNECTION_STRING);
            request.addHeader("content-type", "text/plain");

            String payload = objectMapper.writeValueAsString(data);
            byte[] compressed = Zipper.compress(payload.getBytes(StandardCharsets.UTF_8));
            HttpEntity httpEntity = new StringEntity(new String(compressed));
            request.setEntity(httpEntity);
            HttpResponse response = httpclient.execute(request);
            XLog.i("STATUS", String.valueOf(response.getStatusLine().getStatusCode()));
        } catch (Exception e) {
            XLog.e(e.getLocalizedMessage());
        }

        return "";
    }
}