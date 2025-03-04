package com.ppc8.skyWatch;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.time.*;

@Entity
public class Tick {
    @Id
    private String id; // symbol + unix msec timestamp
    private double open;
    private double close;
    private long volume;
    private int transactions;
    private double volumeWeightedPrice;
    private LocalDate date;
    private LocalTime time;
    private String market;
    private int trend;
    public Tick(String symbol, double openPrice, double closePrice, long volume, int transactions, double vwPrice, long timestamp, String market, Company company, int trend) {
        this.id = symbol + "_" + timestamp;
        this.open = openPrice;
        this.close = closePrice;
        this.volume = volume;
        this.transactions = transactions;
        this.volumeWeightedPrice = vwPrice;
        LocalDateTime datetime = LocalDateTime.ofEpochSecond(timestamp / 1000, 0, ZoneId.of("America/New_York").getRules().getOffset(Instant.now()));
        this.date = datetime.toLocalDate();
        this.time = datetime.toLocalTime();
        this.market = market;
        this.trend = trend;
    }
    public Tick() {}

    public String getId() { return id; }
    public double getOpen() { return open; }
    public double getClose() { return close; }
    public long getVolume() { return volume; }
    public int getTransactions() { return transactions; }
    public double getPrice() { return volumeWeightedPrice; }
    public LocalDate getDate() { return date; }
    public LocalTime getTime() { return time; }
    public String getMarket() { return market; }
    public int getTrend() { return trend; }
    public void setOpen(double open) { this.open = open; }
    public void setClose(double close) { this.close = close; }
    public void setVolume(long volume) { this.volume = volume; }
    public void setTransactions(int transactions) { this.transactions = transactions; }
    public void setPrice(double price) { this.volumeWeightedPrice = price; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setTime(LocalTime time) { this.time = time; }
    public void setMarket(String market) { this.market = market; }
    public void setTrend(int trend) { this.trend = trend; }
}