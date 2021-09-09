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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public class SymbolNameDownloader implements Runnable {
    private static final String TAG = "SymbolNameDownloader";
    private static final String REGION_URL = "https://api.iextrading.com/1.0/ref-data/symbols";
    public static HashMap<String, String> SymbolNameMap = new HashMap<>();


    @Override
    public void run() {
        Uri dataUri = Uri.parse(REGION_URL);
        String urlToUse = dataUri.toString();
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

            //if connected
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

        //save all data to sb
        process(sb.toString());
        Log.d(TAG, "run: ");

    }

    private void process(String sb) {
        try {
            JSONArray jObjMain = new JSONArray(sb);

            for (int i = 0; i < jObjMain.length(); i++) {
                JSONObject jStocks = (JSONObject) jObjMain.get(i);

                String symbol = jStocks.getString("symbol");
                String name = jStocks.getString("name");

                SymbolNameMap.put(symbol, name);
            }
            Log.d(TAG, "process: ");
        } catch (Exception e) {
            Log.d(TAG, "parseJSON: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public static ArrayList<String> findMatches(String string) {
        String strToMatch = string.toLowerCase().trim();
        HashSet<String> matchSet = new HashSet<>(); // This set appears as a result of search

        for (String sym : SymbolNameMap.keySet()) { // check all symbols
            if (sym.toLowerCase().trim().contains(strToMatch)) {  //check if string is in that symbol
                matchSet.add(sym + " : " + SymbolNameMap.get(sym));
            }
            String name = SymbolNameMap.get(sym); //check all company's name
            if (name != null && //check if string in the name
                    name.toLowerCase().trim().contains(strToMatch)) {
                matchSet.add(sym + " : " + name);
            }
        }

        ArrayList<String> results = new ArrayList<>(matchSet);
        Collections.sort(results);

        return results;
    }
}
