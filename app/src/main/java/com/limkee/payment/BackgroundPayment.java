package com.limkee.payment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

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

    public class BackgroundPayment extends AsyncTask<String,Void,String> {
        private Context context;
        private Activity activity;
        private String totalPayable;

    BackgroundPayment(Context ctx, Activity act) {
        context = ctx;
        activity = act;
    }

    @Override
    protected String doInBackground(String... params) {
        String type = params[0];
        totalPayable = params[1];
        String token = params[2];
        String payment_url = "http://13.229.114.72:80/JavaBridge/";
        if(type.equals("pay_with_new_card")){
            try {
                payment_url += "payment_newcard.php";
                URL url = new URL(payment_url);
                HttpURLConnection huc = (HttpURLConnection)url.openConnection();
                huc.setRequestMethod("POST");
                huc.setDoInput(true);
                huc.setDoOutput(true);
                OutputStream ops = huc.getOutputStream();
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(ops,"UTF-8"));
                String post_data = URLEncoder.encode("token","UTF-8")+"="+URLEncoder.encode(token,"UTF-8")
                        +"&"+URLEncoder.encode("totalPayable","UTF-8")+"="+URLEncoder.encode(totalPayable,"UTF-8");
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
        }
        else if (type.equals("pay_with_saved_card")){
            try {
                payment_url += "payment_savedcard.php";
                URL url = new URL(payment_url);
                HttpURLConnection huc = (HttpURLConnection)url.openConnection();
                huc.setRequestMethod("POST");
                huc.setDoInput(true);
                huc.setDoOutput(true);
                OutputStream ops = huc.getOutputStream();
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(ops,"UTF-8"));
                String post_data = URLEncoder.encode("token","UTF-8")+"="+URLEncoder.encode(token,"UTF-8")
                        +"&"+URLEncoder.encode("totalPayable","UTF-8")+"="+URLEncoder.encode(totalPayable,"UTF-8");
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
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        System.out.println("finally");
        Intent it = new Intent(context.getApplicationContext(), ConfirmationActivity.class);
        it.putExtra("result",result);
        Double tp = Double.parseDouble(totalPayable);
        it.putExtra("totalPayable", tp/100);
        context.startActivity(it);
        activity.finish();
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }
}