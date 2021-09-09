package com.hle.stockwatch;

import java.io.Serializable;
import java.util.Objects;

public class Stock implements Serializable, Comparable<Stock> {

    private String symbol;
    private String name;
    private String direction;
    private double price;
    private double change;
    private double changePercentage;

    public Stock(String symbol, String name, double price, String dir, double change, double changePercentage) {
        this.symbol = symbol;
        this.name = name;
        this.direction = dir;
        this.price = price;
        this.change = change;
        this.changePercentage = changePercentage;
    }

    public String getStocksymbol() { return symbol; }
    public String getCompanyname() { return name; }
    public String getDirection() {
        if (this.change >= 0) {
            return "▲";
        } else return "▼";
    }
    public double getPrice() { return price; }
    public double getChange() { return change; }
    public double getChangePercentage() { return changePercentage; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stock country = (Stock) o;
        return name.equals(country.name) &&
                symbol.equals(country.symbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, symbol);
    }

    @Override
    public int compareTo(Stock stock) {
        return name.compareTo(stock.getCompanyname());
    }
}

