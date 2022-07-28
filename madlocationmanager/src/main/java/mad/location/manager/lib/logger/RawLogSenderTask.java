package mad.location.manager.lib.logger;

import android.os.AsyncTask;

import com.elvishew.xlog.XLog;

public class RawLogSenderTask extends AsyncTask<String, Integer, Integer> {
    private static final String STARTING = "Send raw log to server...";
    private static final String FINISHED = "Raw log was sended";

    @Override
    protected Integer doInBackground(String... parameter) {

        //int myProgress = 0;
        //publishProgress(myProgress);
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