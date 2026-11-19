package com.example.netelsansmarthome;

public class Module {
    public int id;

    public String m_id; // modulleri kaydetmek için
    public int roomId;  // Hangi odaya bağlı (Foreign Key)
    public String title;
    public int iconRes;
    public boolean isOn;
    public int level;

    public Module() {}

    public Module(String title, int iconRes, boolean isOn) {
        this.title = title;
        this.iconRes = iconRes;
        this.isOn = isOn;
        this.level = 25;
    }
}