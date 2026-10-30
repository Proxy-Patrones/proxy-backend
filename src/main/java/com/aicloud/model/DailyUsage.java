package com.aicloud.model;

import java.time.LocalDate;

/** Consumo de un usuario en un dia. */
public class DailyUsage {

    private final LocalDate date;
    private int requests;
    private long tokens;

    public DailyUsage(LocalDate date, int requests, long tokens) {
        this.date = date;
        this.requests = requests;
        this.tokens = tokens;
    }

    public LocalDate getDate() {
        return date;
    }

    public int getRequests() {
        return requests;
    }

    public long getTokens() {
        return tokens;
    }

    public void add(int requests, long tokens) {
        this.requests += requests;
        this.tokens += tokens;
    }
}
