package com.hle.stockwatch;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.icu.text.DecimalFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.JsonWriter;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    private static final String TAG = "MainActivity";
    private final List<Stock> stockList = new ArrayList<>();  // Main content is here
    private RecyclerView recyclerView;
    private StockAdapter stockAdapter;
    private String choice;
    private SwipeRefreshLayout swiper;
    //private static final String yahoo = "https://www.yahoo.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        stockList.clear();

        readJSONData(); //update stockList

        recyclerView = findViewById(R.id.recycler);
        stockAdapter = new StockAdapter(stockList, this);
        recyclerView.setAdapter(stockAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView.smoothScrollToPosition(0);

        swiper = findViewById(R.id.swiper);

        swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!checkNetworkConnection()) {
                    swiper.setRefreshing(false);
                    return;
                }

                for (int i = 0; i < stockList. size(); i++) {
                    String s = stockList.get(i).getStocksymbol();
                    stockList.remove(i);
                    refreshData(s);
                }
                ///////////////do something like run a runnable to download all the data again

                stockAdapter.notifyDataSetChanged();
                downloadSN();
                swiper.setRefreshing(false); //stop busy circle

            }
        });
        downloadSN();
    }

    public void downloadSN() { //download all symbols and names
        SymbolNameDownloader sn = new SymbolNameDownloader(); //for adding
        new Thread(sn).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add) {
            makeStockDialog(); //popup the dialogue to ask for th input search string
            //stockAdapter.notifyDataSetChanged();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void makeStockDialog() {

        if (!checkNetworkConnection()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final EditText et = new EditText(this);
        et.setInputType(InputType.TYPE_CLASS_TEXT);
        et.setGravity(Gravity.CENTER_HORIZONTAL);
        et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);

        builder.setView(et);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                choice = et.getText().toString().trim();

                final ArrayList<String> results = SymbolNameDownloader.findMatches(choice);

                if (results.size() == 0) {
                    doNoAnswer(choice);
                } else if (results.size() == 1) {
                    doSelection(results.get(0));  // VNA : Vietnam is the only choice in result list
                } else {
                    String[] array = results.toArray(new String[0]);  //turn results onto string array

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Make a selection");
                    builder.setItems(array, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            String option = results.get(which);
                            doSelection(option);
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
                    AlertDialog dialog2 = builder.create();
                    dialog2.show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

        builder.setMessage("Please enter a Symbol or Company Name:");
        builder.setTitle("Stock Selection");

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void doNoAnswer(String symbol) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("No data for specified stock symbol/name");
        builder.setTitle("Symbol not found: " + symbol);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void doSelection(String sym) {
        String[] data = sym.split(":"); //[VNA, Vietnam] //get the symbol and company name
        StockDownloader StockDownloader = new StockDownloader(this, data[0].trim()); //trim symbol
        new Thread(StockDownloader).start();
    }

    private void refreshData(String sym) {
        StockDownloader StockDownloader = new StockDownloader(this, sym);
        new Thread(StockDownloader).start();
    }

    public void addStock(Stock stock) {
        if (stock == null) {
            badDataAlert(choice);
            return;
        }
        if (stockList.contains(stock)) { // check to see if the stock being added is already in the list
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setMessage(stock.getCompanyname() + " is already added.");
            builder.setTitle("Duplicate Stock");
            builder.setIcon(R.drawable.twotone_error_black_18dp);

            AlertDialog dialog = builder.create();
            dialog.show();
            return;
        }
        stockList.add(stock);
        Collections.sort(stockList);
        writeJSONData();
        stockAdapter.notifyDataSetChanged();
    }

    private void badDataAlert(String sym) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("No data found.");
        builder.setTitle("Symbol Not Found: " + sym);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        writeJSONData();
    }

    public boolean checkNetworkConnection() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) return true;
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("No Network Connection");
            builder.setMessage("Cannot process current action without an internet connection.");
            AlertDialog dialog = builder.create();
            dialog.show();
            return false;
        }
    }

    public boolean checkNetworkConnection2() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) return true;
        else {
            return false;
        }
    }

    @Override
    public void onClick(View v) {  // click listener called by ViewHolder clicks
        if (!checkNetworkConnection()) {
            swiper.setRefreshing(false);
            return;
        }
        int pos = recyclerView.getChildLayoutPosition(v);
        Stock stock = stockList.get(pos);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        String link = "https://www.marketwatch.com/investing/stock/" + stock.getStocksymbol();
        intent.setData(Uri.parse(link));
        startActivity(intent);
    }

    @Override
    public boolean onLongClick (View v) {  // long click listener called by ViewHolder long clicks
        final int pos = recyclerView.getChildLayoutPosition(v);
        final Stock stock = stockList.get(pos);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.twotone_delete_black_18dp);
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                stockList.remove(stock);
                writeJSONData();
                stockAdapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

        builder.setMessage("Delete " + stockList.get(pos).getCompanyname() + "?");
        builder.setTitle("Delete Stock");

        AlertDialog dialog = builder.create();
        dialog.show();
        return false;
    }

    private void readJSONData() {

        try {
            FileInputStream fis = getApplicationContext().
                    openFileInput(getString(R.string.data_file));

            // Read string content from file
            byte[] data = new byte[fis.available()]; // this technique is good for small files
            int loaded = fis.read(data);
            Log.d(TAG, "readJSONData: Loaded " + loaded + " bytes");
            fis.close();
            String json = new String(data);

            // Create JSON Array from string file content
            JSONArray noteArr = new JSONArray(json);
            for (int i = 0; i < noteArr.length(); i++) {
                JSONObject cObj = noteArr.getJSONObject(i);

                String name = cObj.getString("companyName");
                String symbol = cObj.getString("symbol");
                String latestPrice = cObj.getString("latestPrice");
                String dir = cObj.getString("direction");
                String change = cObj.getString("change");
                String changePercent = cObj.getString("changePercent");

                // Create Stock and add to ArrayList
                Stock stock = new Stock(symbol, name, Double.parseDouble(latestPrice), dir, Double.parseDouble(change), Double.parseDouble(changePercent));
                stockList.add(stock);
            }
            stockAdapter.notifyDataSetChanged();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeJSONData() {

        try {
            FileOutputStream fos = getApplicationContext().
                    openFileOutput(getString(R.string.data_file), Context.MODE_PRIVATE);

            JsonWriter writer = new JsonWriter(new OutputStreamWriter(fos, StandardCharsets.UTF_8));
            writer.setIndent("  ");
            writer.beginArray();
            for (Stock stock : stockList) {
                DecimalFormat precision = new DecimalFormat("0.00");

                writer.beginObject();

                writer.name("companyName").value(stock.getCompanyname());
                writer.name("symbol").value(stock.getStocksymbol());
                writer.name("latestPrice").value(String.valueOf((precision.format(stock.getPrice()))));
                writer.name("direction").value(stock.getDirection());
                writer.name("change").value(String.valueOf((precision.format(stock.getChange()))));
                writer.name("changePercent").value(String.valueOf((precision.format(stock.getChangePercentage()))));

                writer.endObject();
            }
            writer.endArray();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "writeJSONData: " + e.getMessage());
        }
    }
}