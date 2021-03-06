package com.limkee1.navigation;

import android.os.AsyncTask;
import com.limkee1.payment.AsyncResponse;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;

public class BackgroundCutoffTime extends AsyncTask<String,Void,String> {
    private String companyCode;
    private String password;

    public AsyncResponse delegate = null;

    public BackgroundCutoffTime(AsyncResponse delegate){
        this.delegate = delegate;
    }

    @Override
    protected String doInBackground(String... params) {
        String payment_url = "http://13.229.114.72:80/JavaBridge/cutofftime.php";
        String post_data;
        companyCode = params[0];
        password = params[1];
        try {
            URL url = new URL(payment_url);
            HttpURLConnection huc = (HttpURLConnection)url.openConnection();
            huc.setRequestMethod("POST");
            huc.setDoInput(true);
            huc.setDoOutput(true);
            OutputStream ops = huc.getOutputStream();
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(ops,"UTF-8"));
            post_data = URLEncoder.encode("companyCode","UTF-8")+"="+URLEncoder.encode(companyCode,"UTF-8")
                    +"&"+URLEncoder.encode("HashPassword","UTF-8")+"="+URLEncoder.encode(password,"UTF-8");
            bw.write(post_data);
            bw.flush();
            bw.close();
            ops.close();
            InputStream is = huc.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String result = "";
            String line;
            StringBuilder sb = new StringBuilder();
            while((line = br.readLine())!=null){
                sb.append(line);
            }
            br.close();
            is.close();
            huc.disconnect();
            result = sb.toString();
            return result;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    protected void onPostExecute(String result) {
        delegate.processFinish(result);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }
}