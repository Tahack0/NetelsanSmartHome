package com.example.netelsansmarthome;

public class LogItem {
    public String title; // Örn: "Salon Kamerası"
    public String message; // Örn: "Hareket Algılandı"
    public String time; // Örn: "14:30"

    public LogItem(String title, String message, String time) {
        this.title = title;
        this.message = message;
        this.time = time;
    }
}