package com.hle.stockwatch;

import android.content.Context;
import android.graphics.Color;
import android.icu.text.DecimalFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class StockAdapter extends RecyclerView.Adapter <MyViewHolder> {
    private static final String TAG = "CountryAdapter";
    private List<Stock> stockList;
    private MainActivity mainAct;

    StockAdapter(List<Stock> stockList, MainActivity ma) {
        this.stockList = stockList;
        mainAct = ma;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: MAKING NEW");

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.stock_list_entry, parent, false);

        itemView.setOnClickListener(mainAct);
        itemView.setOnLongClickListener(mainAct);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        DecimalFormat precision = new DecimalFormat("0.00");
        Stock stock = stockList.get(position);

        if (!mainAct.checkNetworkConnection2()) {
            holder.companyname.setText(stock.getCompanyname());
            holder.stocksymbol.setText(stock.getStocksymbol());
            holder.dir.setText("");
            holder.price.setText("0.00");
            holder.change.setText("0.00");
            holder.changepercentage.setText("0.00");
            holder.companyname.setTextColor(Color.WHITE);
            holder.stocksymbol.setTextColor(Color.WHITE);
            holder.dir.setTextColor(Color.WHITE);
            holder.price.setTextColor(Color.WHITE);
            holder.change.setTextColor(Color.WHITE);
            holder.changepercentage.setTextColor(Color.WHITE);
        }
        else {
            holder.companyname.setText(stock.getCompanyname());
            holder.stocksymbol.setText(stock.getStocksymbol());
            holder.dir.setText(stock.getDirection());
            holder.price.setText(String.valueOf((precision.format(stock.getPrice()))));
            holder.change.setText(String.valueOf((precision.format(stock.getChange()))));
            holder.changepercentage.setText("(" + String.valueOf((precision.format(stock.getChangePercentage()))) + "%)");
            if (stock.getChange() >= 0) {
                holder.companyname.setTextColor(Color.GREEN);
                holder.stocksymbol.setTextColor(Color.GREEN);
                holder.dir.setTextColor(Color.GREEN);
                holder.price.setTextColor(Color.GREEN);
                holder.change.setTextColor(Color.GREEN);
                holder.changepercentage.setTextColor(Color.GREEN);
            } else {
                holder.companyname.setTextColor(Color.RED);
                holder.stocksymbol.setTextColor(Color.RED);
                holder.dir.setTextColor(Color.RED);
                holder.price.setTextColor(Color.RED);
                holder.change.setTextColor(Color.RED);
                holder.changepercentage.setTextColor(Color.RED);
            }
        }
    }

    @Override
    public int getItemCount() {
        return stockList.size();
    }

}

