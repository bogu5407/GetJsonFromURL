package com.example.boreng.kyobo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class MainActivity extends AppCompatActivity {

    HttpClient httpclient = new DefaultHttpClient();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        disableSSLCertificateChecking(); //ignore SSL
        //If you wanna get a data from url, you should execute it in thread
        new Thread() {
            public void run() {
                    String apiUrl = "https://openapi.kyobo.co.kr:1443/v1.0/INFO/user/husermapinfo";
                    String kyoboApiKey = "F1ArFxMzE0MSJeZBQFN99Q413q3R95tN";
                    StringBuffer jsonBuf = new StringBuffer();
                    jsonBuf.append("{\n");
                    jsonBuf.append(" \"dataHeader\" : {\n");
                    jsonBuf.append(" },\n");
                    jsonBuf.append(" \"dataBody\": {\n");
                    jsonBuf.append(" \"PRTY_REG_NO\": \"5606052172234\"\n"); //requestBody
                    ////내용
                    jsonBuf.append(" }\n");
                    jsonBuf.append("}\n");
                    String result = call(kyoboApiKey, apiUrl, jsonBuf.toString()); /// json을 string형태로 받아옴
                    Log.d("TEST",result);
            }
        }.start();

    }
    //Get a JSON file from URL with POST type
    //JSON형태로 받아와서 GSON을 사용하고 싶었지만 그게 안되어 String으로 했다 .ㅠㅠ
    public static String call(String kyoboApiKey, String apiUrl, String jsonStr) {
        String result = "";
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.addRequestProperty("content-Type", "application/json");
            con.addRequestProperty("kyoboApiKey", kyoboApiKey);
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setUseCaches(false); con.setDefaultUseCaches(false);
            con.connect();
            OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream(), "UTF-8");
            out.write(jsonStr); out.flush(); out.close();
            int responseCode = con.getResponseCode();

            BufferedReader br;
            if(responseCode == 200){
                br = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
            }else{
                br = new BufferedReader(new InputStreamReader(con.getErrorStream(), "UTF-8"));
            }

            String str = null;
            StringBuffer strBuf = new StringBuffer();
            while ((str = br.readLine()) != null) {
                strBuf.append(str);
            }
            result = strBuf.toString();
            ObjectMapper mapper = new ObjectMapper();
            Object json = mapper.readValue(result, Object.class);
            result = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        } catch (Exception e) {
            System.out.println(e);
        }
        return result;
    }


    //Ignore SSL!!!!!!!
    private static void disableSSLCertificateChecking() {
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {

            }

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {

            }
            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[0];
            }
        } };
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            @Override public boolean verify(String hostname, SSLSession session) {
                return true; } });
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
    //// Split string
    public String parse(JsonObject jobject, String jsonLine) {
        JsonElement jelement = new JsonParser().parse(jsonLine);
        //jsonLine = requestbody
        jobject = jobject.getAsJsonObject("dataBody");
        JsonArray jarray = jobject.getAsJsonArray("USER_INFO");  //user의 정보로 배열생성
        jobject = jarray.get(0).getAsJsonObject();
        String result = jobject.get("USER_INFO").toString();
//            JsonElement jelement = new JsonParser().parse("requestBody");
//            JsonObject jobject = jelement.getAsJsonObject();
//            jobject = jobject.getAsJsonObject("dataBody");
//            JsonArray jarray = jobject.getAsJsonArray("USER_INFO");  //user의 정보로 배열생성
//            jobject = jarray.get(0).getAsJsonObject();
//            String result = jobject.get("USER_INFO").toString();
//            Log.i("TEST",result);
        return result;

    }

}


