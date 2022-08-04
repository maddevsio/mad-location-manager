package mad.location.manager.lib.logger;

import android.os.AsyncTask;
import com.elvishew.xlog.XLog;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maddevs.logtransferobject.types.Record;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.net.HttpURLConnection;
import java.util.List;

import mad.location.manager.lib.Utils.UniquePsuedoID;

public class RawLogSenderTask extends AsyncTask<List<Record>, Integer, Integer> {
    private static final String STARTING = "Send raw log to server...";
    private static final String FINISHED = "Raw log was sended";

    private String CONNECTION_STRING;
    private final String uniquePhoneID = UniquePsuedoID.getUniquePsuedoID();
    private HttpClient httpclient = new DefaultHttpClient();
    private ObjectMapper objectMapper = new ObjectMapper();

    public RawLogSenderTask() {
        super();
        CONNECTION_STRING = String.format("http://192.168.1.33:8085/api/collector/%s", uniquePhoneID);
    }

    @Override
    protected Integer doInBackground(List<Record>... records) {
        try {
            HttpPost request = new HttpPost(CONNECTION_STRING);
            request.addHeader("content-type", "application/json");
            HttpEntity httpEntity = new StringEntity(objectMapper.writeValueAsString(records[0])
                    , "UTF-8");
            request.setEntity(httpEntity);
            HttpResponse response = httpclient.execute(request);
            XLog.i("STATUS", String.valueOf(response.getStatusLine().getStatusCode()));
        } catch (Exception e) {
            XLog.e(e.getLocalizedMessage());
        }

        return 0;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        XLog.v(STARTING);
        // [... Обновите индикатор хода выполнения, уведомления или другой
        // элемент пользовательского интерфейса ...]
    }

//    @Override
//    protected void onPostExecute(Integer... result) {
//        // [... Сообщите о результате через обновление пользовательского
//        // интерфейса, диалоговое окно или уведомление ...]
//        XLog.v(FINISHED);
//    }
}