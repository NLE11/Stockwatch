package com.hle.stockwatch;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

public class MyViewHolder extends RecyclerView.ViewHolder {
    TextView stocksymbol;
    TextView companyname;
    TextView dir;
    TextView price;
    TextView change;
    TextView changepercentage;

    MyViewHolder(View v) {
        super(v);
        stocksymbol = itemView.findViewById(R.id.stocksymbol);
        companyname = itemView.findViewById(R.id.companyname);
        dir = itemView.findViewById(R.id.dir);
        price = itemView.findViewById(R.id.price);
        change = itemView.findViewById(R.id.change);
        changepercentage = itemView.findViewById(R.id.changepercentage);
    }
}
