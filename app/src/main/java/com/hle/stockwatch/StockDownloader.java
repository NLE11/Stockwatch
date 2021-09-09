package com.hle.stockwatch;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static java.lang.Double.parseDouble;

public class StockDownloader implements Runnable {
    private static final String TAG = "StockDownloader";
    private static final String REGION_URL = "https://cloud.iexapis.com/stable/stock/";
    private static final String API = "/quote?token=pk_03367abe044e4309a6fc22210a2a9d63";
    private MainActivity mainActivity;
    private String Symbol;

    public StockDownloader(MainActivity mainActivity, String Symbol) {
        this.mainActivity = mainActivity;
        this.Symbol = Symbol;
    }

    @Override
    public void run() {
        Uri.Builder uriBuilder = Uri.parse(REGION_URL + Symbol + API).buildUpon();
        // https://cloud.iexapis.com/stable/stock/TSLA/quote?token=pk_03367abe044e4309a6fc22210a2a9d63
        String urlToUse = uriBuilder.toString();

        Log.d(TAG, "run: " + urlToUse);

        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(urlToUse);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "run: HTTP ResponseCode NOT OK: " + conn.getResponseCode());
                return;
            }

            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }

            Log.d(TAG, "run: " + sb.toString());

        } catch (Exception e) {
            Log.e(TAG, "run: ", e);
            return;
        }

        process(sb.toString());
        Log.d(TAG, "run: ");

    }

    private void process(String sb) {
        try {
            //JSONArray jArray = new JSONArray();
            //jArray.put(sb);
            //JSONObject jStock = (JSONObject) jArray.get(0); //take first object, in fact there is only one object

            JSONObject jStock = new JSONObject(sb);

            String name = jStock.getString("companyName");
            String symbol = jStock.getString("symbol");

            String priceString = jStock.getString("latestPrice");
            double price = 0;
            if (!priceString.trim().isEmpty() && !priceString.trim().equals("null"))
                price = Double.parseDouble(priceString);

            String changeString = jStock.getString("change");
            double change = 0;
            if (!changeString.trim().isEmpty() && !changeString.trim().equals("null"))
                change = Double.parseDouble(changeString);

            String dir;
            if (change >= 0) {
                dir = "▲";
            } else dir = "▼";

            String changePercentString = jStock.getString("changePercent");
            double changePercent = 0;
            if (!changePercentString.trim().isEmpty() && !changePercentString.trim().equals("null"))
                changePercent = Double.parseDouble(changePercentString);

            final Stock stock = new Stock(symbol, name, price, dir, change, changePercent);

            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mainActivity.addStock(stock);
                }
            });

        } catch (Exception e) {
            Log.d(TAG, "parseJSON: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
