package com.example.netelsansmarthome;

import java.util.ArrayList;
import java.util.List;

public class Room {

    public int id;
    public String  mid; // module bars update için

    public String name;
    public int iconRes;
    public boolean isOn;
    public int level;

    // Cihaz listesi
    public List<Module> devices = new ArrayList<>();
    public Room() {
        this.devices = new ArrayList<>();
    }

    public Room(String name, int iconRes) {
        this.name = name;
        this.iconRes = iconRes;
        this.devices = new ArrayList<>(); // Listeyi başlat
        this.isOn = false;
        this.level = 0;
    }
    public ArrayList<Module> getModules() {
        if (devices instanceof ArrayList) {
            return (ArrayList<Module>) devices;
        } else {
            return new ArrayList<>(devices);
        }
    }
    public String getName() {
        return name;
    }
    public void setDevices(ArrayList<Module> modules) {
        this.devices = modules;
    }
}