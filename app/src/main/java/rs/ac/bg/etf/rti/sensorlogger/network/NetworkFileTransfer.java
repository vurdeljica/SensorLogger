package rs.ac.bg.etf.rti.sensorlogger.network;

import android.os.Environment;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.io.File;
import java.util.concurrent.Future;

/**
 * NetworkFileTransfer is Singleton which gives api for
 * sending files over network to server
 */
public class NetworkFileTransfer {

    private static NetworkFileTransfer instance;

    /**
     * Classic Singleton method for getting instance.
     * @return NetworkFileTransfer instance
     */
    public static NetworkFileTransfer getInstance() {
        if (instance == null) {
            instance = new NetworkFileTransfer();
        }

        return instance;
    }

    private NetworkFileTransfer() {

    }

    /**
     * Send all files from given directory to server
     * @param _serverURL server uri in form: "http://<ipAddress>:<port>;
     * @param _directory directory which files will be sent to server
     */
    public void sendAllFilesInDirectory(String _serverURL, File _directory) {
        final String serverURL = _serverURL;
        final File directory = _directory;
        Thread thread = new Thread(new Runnable() {

            ExecutorService executor = null;

            @Override
            public void run() {
                try {
                    File[] files = directory.listFiles();
                    Random rand = new Random();
                    int id = rand.nextInt();
                    sendGeneralInformation(serverURL, files.length, id);

                    List<Future<?>> futures = new ArrayList<>();
                    executor = Executors.newFixedThreadPool(10);//creating a pool of 10 threads

                    for(int i = 0; i < files.length; i++) {
                        Callable<?> worker = makeCallableTaskForFileUpload(serverURL, files[i], id);
                        Future<?> f = executor.submit(worker);
                        futures.add(f);
                    }

                    for(Future<?> future : futures) {
                        boolean noErrorOccurred = (Boolean)future.get();
                        if (!noErrorOccurred) {
                            executor.shutdownNow();
                            break;
                        }
                    }

                    executor.shutdown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    private void sendGeneralInformation(String serverURL, int numOfFiles, int id) {
        try {
            List<NameValuePair> nameValuePairs = new ArrayList<>();
            nameValuePairs.add(new BasicNameValuePair("numOfFiles", Integer.toString(numOfFiles)));
            nameValuePairs.add(new BasicNameValuePair("id", Integer.toString(id)));

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(serverURL);
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = httpclient.execute(httppost);
        }
        catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Callable<Boolean> makeCallableTaskForFileUpload(String _serverURL, File _file, int _id) {
        final File file = _file;
        final String serverURL = _serverURL;
        final int id = _id;
        Callable<Boolean> callableTask = () -> {
            try {
                Log.d("PERFORMANCE", System.currentTimeMillis() + "");
                uploadFileToServer(serverURL, file, id);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        };

        return callableTask;
    }

    private void uploadFileToServer(String serverURL, File file, int id) throws IOException {
        final HttpParams httpParams = new BasicHttpParams();
        int fileType = -1;
        if (file.getName().contains("json")) {
            fileType = 0;
        }
        else if (file.getName().contains("mobile")) {
            fileType = 1;
        }
        else if (file.getName().contains("device")) {
            fileType = 2;
        }

        HttpConnectionParams.setConnectionTimeout(httpParams, 20000);
        HttpClient httpclient = new DefaultHttpClient(httpParams);
        HttpPost httppost = new HttpPost(serverURL + "/upload?fileType="+ fileType + "&id=" +id);
        FileInputStream fIn = new FileInputStream(file);
        InputStreamEntity reqEntity = new InputStreamEntity(fIn, -1);
        reqEntity.setContentType("binary/octet-stream");
        reqEntity.setChunked(true); // Send in multiple parts if needed
        httppost.setEntity(reqEntity);
        HttpResponse response = httpclient.execute(httppost);
        //Do something with response...
    }

}
