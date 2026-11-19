package com.example.netelsansmarthome;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Veritabanı Bilgileri
    private static final String DATABASE_NAME = "SmartHomeDB";
    private static final int DATABASE_VERSION = 1;

    // Tablo İsimleri
    private static final String TABLE_ROOMS = "rooms";
    private static final String TABLE_MODULES = "modules";

    // Tablo Sütunları
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_ICON = "icon";

    // Modül Sütunları
    private static final String KEY_ROOM_ID = "room_id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_IS_ON = "is_on";
    private static final String KEY_LEVEL = "level";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 1. ODA Tablosunu Yarat
        String CREATE_ROOMS = "CREATE TABLE " + TABLE_ROOMS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_NAME + " TEXT,"
                + KEY_ICON + " INTEGER" + ")";
        db.execSQL(CREATE_ROOMS);

        // 2. MODÜL Tablosunu Yarat
        String CREATE_MODULES = "CREATE TABLE " + TABLE_MODULES + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_ROOM_ID + " INTEGER," // Hangi odaya ait?
                + KEY_TITLE + " TEXT,"
                + KEY_ICON + " INTEGER,"
                + KEY_IS_ON + " INTEGER," // 1: Açık, 0: Kapalı
                + KEY_LEVEL + " INTEGER" + ")";
        db.execSQL(CREATE_MODULES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROOMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MODULES);
        onCreate(db);
    }

    // ==========================================
    // 🟢 CREATE (VERİ EKLEME)
    // ==========================================
    public long addRoom(Room room) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, room.name);
        values.put(KEY_ICON, room.iconRes);
        long id = db.insert(TABLE_ROOMS, null, values);
        db.close();
        return id;
    }

    public void addModule(Module module, long roomId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ROOM_ID, roomId);
        values.put(KEY_TITLE, module.title);
        values.put(KEY_ICON, module.iconRes);
        values.put(KEY_IS_ON, module.isOn ? 1 : 0);
        values.put(KEY_LEVEL, module.level);
        db.insert(TABLE_MODULES, null, values);
        db.close();
    }

    // ==========================================
    // 🔵 READ (VERİ OKUMA) - Tüm Odaları ve Cihazlarını Getir
    // ==========================================
    public ArrayList<Room> getAllRoomsWithModules() {
        ArrayList<Room> roomList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Önce odaları çek
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_ROOMS, null);
        if (cursor.moveToFirst()) {
            do {
                Room room = new Room();
                room.id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID));
                room.name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME));
                room.iconRes = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ICON));

                // Sonra bu odanın modüllerini çek
                room.devices = getModulesByRoomId(room.id);
                roomList.add(room);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return roomList;
    }

    public List<Module> getModulesByRoomId(int roomId) {
        List<Module> modules = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_MODULES + " WHERE " + KEY_ROOM_ID + " = " + roomId, null);

        if (cursor.moveToFirst()) {
            do {
                Module m = new Module();
                m.id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID));
                m.title = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TITLE));
                m.iconRes = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ICON));
                m.isOn = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IS_ON)) == 1;
                m.level = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_LEVEL));
                modules.add(m);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return modules;
    }

    // ==========================================
    // 🟠 UPDATE (GÜNCELLEME) - Işık aç/kapa, Slider vb.
    // ==========================================
    public void updateModule(int moduleId, boolean isOn, int level) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_IS_ON, isOn ? 1 : 0);
        values.put(KEY_LEVEL, level);

        db.update(TABLE_MODULES, values, KEY_ID + " = ?", new String[]{String.valueOf(moduleId)});
        db.close();
    }

    // Veritabanı boş mu? (İlk kurulum kontrolü için)
    public boolean isDatabaseEmpty() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT count(*) FROM " + TABLE_ROOMS, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count == 0;
    }

    public void updateModule(Module module) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Senin tanımladığın sabit değişkenleri (KEY_...) kullanıyoruz
        values.put(KEY_IS_ON, module.isOn ? 1 : 0);
        values.put(KEY_LEVEL, module.level);

        // "modules" yerine TABLE_MODULES, "id" yerine KEY_ID kullanıyoruz
        db.update(TABLE_MODULES, values, KEY_ID + " = ?", new String[]{String.valueOf(module.id)});

        db.close();
    }
}//mongodb+srv://<db_username>:<db_password>@cluster0.5k9xnzz.mongodb.net/?appName=Cluster0