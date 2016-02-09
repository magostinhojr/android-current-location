package com.magostinhojr.currentlocation.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.magostinhojr.currentlocation.location.AbstractLocationListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by marceloagostinho on 1/28/16.
 */
public class CurrentLocationService extends Service {

    private LocationManager locationManager;

    private static final int FIVE_MINS_INTERVAL = 1000*60*5;
    private static final int ONE_MIN_INTERVAL = 1000*60*1;
    private static String UUID;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        TelephonyManager tManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);

        UUID = tManager.getDeviceId();

        Log.e("currLocation", "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.e("currLocation", "onCreate");
        super.onCreate();
        startGetCurrentLocationWithInterval(ONE_MIN_INTERVAL);
    }

    /**
     *
     * Método que executara a chamada para buscar a localização atual,
     * atualmente este método espera um Long de Milisegundos para saber de quanto em quanto tempo
     * deve executar a busca pela localização atual.
     *
     * Desta forma, poupamos bateria do celular ligando o Serviço de Localização, apenas quando
     * necessário.
     *
     * @param MIN_IN_MILLIS
     */
    private void startGetCurrentLocationWithInterval(final long MIN_IN_MILLIS){

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                getCurrentLocation();
                handler.postDelayed(this, MIN_IN_MILLIS);
            }
        }, 0);

    }



    /**
     *
     * Método responsável por adquirir o serviço de localização do celular
     * Atualmente o Android sabe qual o melhor PROVEDOR a executar, no caso, se o GPS estiver ligado,
     * ele sempre fara a localização por ele.
     *
     */
    private void getCurrentLocation(){
        // Setup location managers
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER )){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }else{
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,locationListener);
        }

    }

    /**
     *
     * LocationListener é responsavel por ouvir as alterações
     * Neste caso, escolhi criar uma classe abstrata que implementa os métodos da interface
     * LocationListener, desta forma, nesta instancia eu sobreescrevo apenas os métodos que me interessa
     *
     * Os métodos onProviderEnable e Disable ficam ouvindo qualquer alteração no sistema de Localização utilizado
     * No caso se estamos usando o GPS ou NETWORK para descobrirmos nossa localização
     *
     *
     */
    private final LocationListener locationListener = new AbstractLocationListener() {

        @Override
        public void onProviderEnabled(String provider) {
            super.onProviderEnabled(provider);
        }

        @Override
        public void onProviderDisabled(String provider) {
            super.onProviderDisabled(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            super.onLocationChanged(location);
            locationManager.removeUpdates(this);

            Log.d("LAT", String.valueOf(location.getLatitude()));
            Log.d("LNG", String.valueOf(location.getLongitude()));

            new BackgroundOperation().execute(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
        }
    };


    /**
     *
     * Não se assuste daqui para baixo!!!
     * Aqui estou criando um serviço assincrono que executa a requisição para o banco salvar os parametros de Latitude e Longitude
     * Existia uma API de requisição HTTP que foi removida dando espaço para uma outra, particularmente procurei o método puro
     * pois não estudei a nova API a fundo.
     *
     */
    private class BackgroundOperation extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params){
            //Your network connection code should be here .
            String response = postCall(params[0], params[1]);
            return response ;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d("Post Response", result);
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    public String  postCall(String lat, String lng){
        URL url;
//        HashMap<String ,String> postDataParams = new HashMap<String ,String>();
//        postDataParams.put("UserId", "123456789");
//        postDataParams.put("Latidude", lat);
//        postDataParams.put("Longitude", lng);

        JSONObject jObject = new JSONObject();
        try {
            jObject.put("UserId", UUID);
            jObject.put("Latitude", lat);
            jObject.put("Longitude", lng);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String response = "";
        try {
            url = new URL("http://app.contazen.com.br/api/usuariomobileapi/PostInformarPosicao");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setFixedLengthStreamingMode(jObject.toString().getBytes().length);

            //make some HTTP header nicety
            conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");

            //setup send
            OutputStream os = new BufferedOutputStream(conn.getOutputStream());
            os.write(jObject.toString().getBytes());

            //clean up
            os.flush();
            os.close();

            int responseCode=conn.getResponseCode();
            if(responseCode == HttpsURLConnection.HTTP_OK){
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                while ((line=br.readLine()) != null) {
                    response+=line;
                }
            } else {
                response="";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
