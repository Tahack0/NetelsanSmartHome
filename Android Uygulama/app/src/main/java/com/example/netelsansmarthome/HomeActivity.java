package com.example.netelsansmarthome;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity implements RoomAdapter.OnRoomAction {

    // 🔹 MOD SEÇİMİ
    enum ViewMode { ROOM, MODULE }
    private ViewMode currentMode = ViewMode.ROOM;

    // --- SUNUCU AYARLARI ---
// Bilgisayarının IP adresini buraya yaz (Sonuna :3000 eklemeyi unutma)
    private static final String API_URL = "http://10.0.2.2:3000/api/rooms";

    // 🔹 UI BİLEŞENLERİ
    RecyclerView rvRooms;                // Üst: Odalar (Yatay)
    RecyclerView rvRoomModules;          // Alt: Odaya ait cihazlar (Izgara)

    RecyclerView rvModulesHorizontal;    // Üst: Modül Tipleri (Yatay)
    RecyclerView rvModuleRooms;          // Alt: Modüle ait odalar (Izgara)

    View moduleModeContainer;
    FloatingActionButton fabAddRoom;
    ImageView btnLogs;

    // 🔹 VERİ LİSTELERİ
    ArrayList<Room> roomList = new ArrayList<>();
    List<Module> moduleList = new ArrayList<>();

    // 🔹 ADAPTÖRLER
    RoomAdapter roomAdapter;           // Odaya Göre -> Üst Liste
    ModuleAdapter moduleModeAdapter;   // Modüle Göre -> Üst Liste (Küçük İkonlar)

    // YENİ ADAPTÖRLER (Büyük Kartlar İçin)
    GridModuleAdapter gridModuleAdapter; // Odaya Göre -> Alt Liste (Cihazlar)
    GridRoomAdapter gridRoomAdapter;     // Modüle Göre -> Alt Liste (Odalar)

    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        dbHelper = new DatabaseHelper(this);

        // 🔹 MOD BUTONLARI
        TextView btnRoomMode = findViewById(R.id.btnRoomMode);
        TextView btnModuleMode = findViewById(R.id.btnModuleMode);

        btnRoomMode.setOnClickListener(v -> switchMode(ViewMode.ROOM));
        btnModuleMode.setOnClickListener(v -> switchMode(ViewMode.MODULE));

        // 🔹 ZİL BUTONU (LOGLAR)
        btnLogs = findViewById(R.id.btnLogs);
        if (btnLogs != null) {
            btnLogs.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, LogsActivity.class)));
        }

        // 🔹 SAAT & HAVA DURUMU
        TextView txtTime = findViewById(R.id.txtTime);
        startClock(txtTime);

        ImageView imgWeather = findViewById(R.id.imgWeather);
        TextView txtWeather = findViewById(R.id.txtWeather);
        loadWeather(imgWeather, txtWeather);

        // 🔹 VIEW TANIMLAMA
        rvRooms = findViewById(R.id.rvRooms);
        rvRoomModules = findViewById(R.id.rvModules);
        rvModulesHorizontal = findViewById(R.id.rvModeModules);
        rvModuleRooms = findViewById(R.id.rvModuleRooms);
        moduleModeContainer = findViewById(R.id.moduleModeContainer);
        fabAddRoom = findViewById(R.id.fabAddRoom);

        // 🔹 MODÜLLERİ OLUŞTUR (SABİT TİPLER)
        moduleList.add(new Module("Aydınlatma", R.drawable.lightbulb, true));
        moduleList.add(new Module("Perde", R.drawable.curtain__1_, false));
        moduleList.add(new Module("İklim", R.drawable.thermostat, true));
        moduleList.add(new Module("Güvenlik", R.drawable.security_system, true));
        moduleList.add(new Module("Sensör", R.drawable.motion_sensor, false));


        // ==========================================================
        // 🟢 ODAYA GÖRE MOD KURULUMU
        // ==========================================================

        // 1. Üst Liste (Odalar)
        rvRooms.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        roomAdapter = new RoomAdapter(roomList, this);
        rvRooms.setAdapter(roomAdapter);

        // 2. Alt Liste (Cihazlar) - Başlangıçta boş
        rvRoomModules.setLayoutManager(new GridLayoutManager(this, 2));
        gridModuleAdapter = new GridModuleAdapter(new ArrayList<>()); // Boş başlat
        rvRoomModules.setAdapter(gridModuleAdapter);

        // ==========================================================
        // 🔵 MODÜLE GÖRE MOD KURULUMU
        // ==========================================================

        // 1. Üst Liste (Modül Tipleri)
        rvModulesHorizontal.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        moduleModeAdapter = new ModuleAdapter(moduleList, module -> {
            // Modüle tıklandığında alttaki oda adaptörüne "Kategori Değiştir" diyoruz
            if (gridRoomAdapter != null) {
                gridRoomAdapter.setCategory(module.title);
            }
        });
        rvModulesHorizontal.setAdapter(moduleModeAdapter);

        // 2. Alt Liste (Odalar)
        rvModuleRooms.setLayoutManager(new GridLayoutManager(this, 2));
        gridRoomAdapter = new GridRoomAdapter(roomList);
        rvModuleRooms.setAdapter(gridRoomAdapter);

        // 🔹 FAB (Oda Ekleme)
        fabAddRoom.setOnClickListener(v -> showAddRoomDialog());



        roomList.clear();
        //roomList.addAll(dbHelper.getAllRoomsWithModules());
        fetchRoomsFromAPI();


        // Başlangıç Modu
        switchMode(ViewMode.ROOM);
    }



    // ==========================================================
    // 🔧 YARDIMCI FONKSİYONLAR
    // ==========================================================

    // Alt listeyi (Cihazları) yenileyen fonksiyon
    private void updateBottomList(List<Module> devices) {
        gridModuleAdapter = new GridModuleAdapter(devices);
        rvRoomModules.setAdapter(gridModuleAdapter);
    }

    // Odaya tıklanınca çalışan fonksiyon (RoomAdapter Interface'inden gelir)
    @Override
    public void onSelected(Room room) {
        if (room == null || room.devices == null) return;

        // 🔥 İŞTE SİHİR BURADA:
        // Adapter'i oluştururken "lambda" fonksiyonu ile dinleyiciyi bağlıyoruz.
        // Adapter'de düğmeye basılınca -> buradaki 'updateModuleInServer' çalışacak.

        gridModuleAdapter = new GridModuleAdapter(room.devices, module -> {
            // Adapter'den gelen sinyali yakala ve sunucu fonksiyonunu çağır
            updateModuleInServer(room , module);
        });

        rvRoomModules.setAdapter(gridModuleAdapter);

        //Toast.makeText(this, room.name + " cihazları yüklendi", Toast.LENGTH_SHORT).show();
    }

    // =========================
    // 🔄 MOD SWITCH
    // =========================
    private void switchMode(ViewMode mode) {
        currentMode = mode;
        TextView btnRoom = findViewById(R.id.btnRoomMode);
        TextView btnModule = findViewById(R.id.btnModuleMode);

        if (mode == ViewMode.ROOM) {
            // ODAYA GÖRE
            rvRooms.setVisibility(View.VISIBLE);
            rvRoomModules.setVisibility(View.VISIBLE);
            moduleModeContainer.setVisibility(View.GONE);

            btnRoom.setBackgroundColor(Color.parseColor("#D32F2F"));
            btnRoom.setTextColor(Color.WHITE);
            btnModule.setBackgroundColor(Color.parseColor("#EEEEEE"));
            btnModule.setTextColor(Color.parseColor("#757575"));

            if (gridModuleAdapter != null) gridModuleAdapter.notifyDataSetChanged();

        } else {
            // MODÜLE GÖRE
            rvRooms.setVisibility(View.GONE);
            rvRoomModules.setVisibility(View.GONE);
            moduleModeContainer.setVisibility(View.VISIBLE);
            rvModulesHorizontal.setVisibility(View.VISIBLE);
            rvModuleRooms.setVisibility(View.VISIBLE);

            btnModule.setBackgroundColor(Color.parseColor("#D32F2F"));
            btnModule.setTextColor(Color.WHITE);
            btnRoom.setBackgroundColor(Color.parseColor("#EEEEEE"));
            btnRoom.setTextColor(Color.parseColor("#757575"));

            if (gridRoomAdapter != null) gridRoomAdapter.notifyDataSetChanged();
        }
    }


    // =========================
    //   ODA EKLEME
    // =========================
    private void showAddRoomDialog() {
        final int[] selectedIcon = {R.drawable.bedroom}; // Varsayılan ikon

        // --- Diyalog Tasarımı ---
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_room, null);
        EditText edtRoomName = view.findViewById(R.id.edtRoomName);

        // İkon Seçimi Mantığı (Aynı kalıyor)
        ImageView[] icons = {
                view.findViewById(R.id.iconChild), view.findViewById(R.id.iconLiving),
                view.findViewById(R.id.iconKitchen), view.findViewById(R.id.iconBathroom),
                view.findViewById(R.id.iconBedroom), view.findViewById(R.id.iconBalcon)
        };
        int[] drawables = {
                R.drawable.baby, R.drawable.television, R.drawable.kitchen,
                R.drawable.bath, R.drawable.bedroom, R.drawable.balcony
        };

        for (int i = 0; i < icons.length; i++) {
            int index = i;
            if (icons[i] == null) continue;
            icons[i].setOnClickListener(v -> {
                selectedIcon[0] = drawables[index];
                for (ImageView img : icons) if (img != null) img.setAlpha(0.3f);
                v.setAlpha(1.0f);
            });
            if (i != 4) icons[i].setAlpha(0.3f);
        }

        // --- Sunucuya Gönderme İşlemi ---
        new AlertDialog.Builder(this)
                .setTitle("Yeni Oda (Sunucu)")
                .setView(view)
                .setPositiveButton("Ekle", (d, w) -> {
                    String name = edtRoomName.getText().toString().trim();

                    if (!name.isEmpty()) {
                        // Veriyi Sunucuya Gönder
                        postRoomToServer(name, selectedIcon[0]);
                    }
                })
                .setNegativeButton("İptal", null)
                .show();
    }


    // =========================
    //  SUNUCUYA ODA EKLEME
    // =========================
    private void postRoomToServer(String name, int iconRes) {
        JSONObject postData = new JSONObject();
        try {
            postData.put("name", name);
            postData.put("icon", iconRes);
            // Default cihazlar sunucuda (server.js) ayarlı olduğu için buradan göndermiyoruz
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                API_URL,
                postData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(HomeActivity.this, "Oda Eklendi! Listeleniyor...", Toast.LENGTH_SHORT).show();

                        // 🔥 KRİTİK NOKTA BURASI!
                        // Oda eklendi ama ID'sini henüz bilmiyoruz.
                        // O yüzden listeyi sunucudan tekrar çekiyoruz.
                        fetchRoomsFromAPI();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(HomeActivity.this, "Hata: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // İsteği kuyruğa ekle
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    // =========================
    //  ODA İŞLEMLERİ (SİLME/DÜZENLEME)
    // =========================
    @Override
    public void onEdit(Room room, int pos) {
        Toast.makeText(this, "Düzenle: " + room.name, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDelete(Room room, int pos) {
        // 1. Odanın ID'si var mı kontrol et
        if (room.mid == null) {
            Toast.makeText(this, "Hata: Silinecek odanın ID'si yok!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Silme URL'si: http://.../api/rooms/ODA_ID
        String deleteUrl = API_URL + "/" + room.mid;

        // 3. Volley DELETE İsteği
        StringRequest deleteRequest = new StringRequest(
                Request.Method.DELETE,
                deleteUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Başarılı olursa listeden ve ekrandan sil
                        Toast.makeText(HomeActivity.this, room.name + " Silindi!", Toast.LENGTH_SHORT).show();

                        roomList.remove(pos);
                        roomAdapter.notifyItemRemoved(pos);

                        // Alt taraftaki listeyi de temizle veya güncelle
                        if (gridRoomAdapter != null) gridRoomAdapter.notifyDataSetChanged();

                        // Eğer silinen oda seçiliyse alt paneli temizle
                        gridModuleAdapter = new GridModuleAdapter(new ArrayList<>(), null);
                        rvRoomModules.setAdapter(gridModuleAdapter);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null && error.networkResponse.statusCode == 400) {
                            Toast.makeText(HomeActivity.this, "⚠️ Bu isimde bir oda zaten var!", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(HomeActivity.this, "Bağlantı Hatası!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        // İsteği kuyruğa ekle
        Volley.newRequestQueue(this).add(deleteRequest);
    }

    // =========================
    //  SAAT & HAVA
    // =========================
    private void startClock(TextView txt) {
        Handler h = new Handler();
        h.post(new Runnable() {
            @Override
            public void run() {
                txt.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()));
                h.postDelayed(this, 60000);
            }
        });
    }

    private void loadWeather(ImageView img, TextView txt) {
        String url = "https://api.openweathermap.org/data/2.5/weather?q=Istanbul&units=metric&lang=tr&appid=4491ea05c384e6cda2bc728ca8de62fb";
        RequestQueue q = Volley.newRequestQueue(this);
        JsonObjectRequest r = new JsonObjectRequest(Request.Method.GET, url, null,
                res -> {
                    try {
                        int t = (int) res.getJSONObject("main").getDouble("temp");
                        String icon = res.getJSONArray("weather").getJSONObject(0).getString("icon");
                        txt.setText(t + "°C");
                        Picasso.get().load("https://openweathermap.org/img/wn/" + icon + "@2x.png").into(img);
                    } catch (Exception ignored) {}
                }, error -> {});
        q.add(r);
    }

    // ==========================================
    //  SUNUCUDAN VERİ ÇEKME (CİHAZLAR DAHİL)
    // ==========================================
    private void fetchRoomsFromAPI() {
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                API_URL,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        roomList.clear(); // Listeyi temizle

                        try {
                            // 1. Her bir ODA için döngü
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject roomObj = response.getJSONObject(i);

                                String name = roomObj.optString("name", "Oda");
                                //int icon = roomObj.optInt("icon", R.drawable.bedroom);
                                int icon = R.drawable.bedroom; // Varsayılan

                                if (name.equalsIgnoreCase("Salon")) icon = R.drawable.television;
                                else if (name.equalsIgnoreCase("Mutfak")) icon = R.drawable.kitchen;
                                else if (name.equalsIgnoreCase("Yatak Odası")) icon = R.drawable.bedroom;
                                else if (name.equalsIgnoreCase("Çocuk Odası")) icon = R.drawable.baby;
                                else if (name.equalsIgnoreCase("Banyo")) icon = R.drawable.bath;
                                else if (name.equalsIgnoreCase("Giriş")) icon = R.drawable.balcony;

                                // Odayı oluştur
                                Room room = new Room(name, icon);

                                if (roomObj.has("_id")) {
                                    room.mid = roomObj.getString("_id");
                                }

                                // 2. Odanın içindeki CİHAZLARI (devices) al
                                ArrayList<Module> roomModules = new ArrayList<>();

                                if (roomObj.has("devices")) {
                                    JSONArray devicesArr = roomObj.getJSONArray("devices");

                                    for (int j = 0; j < devicesArr.length(); j++) {
                                        JSONObject devObj = devicesArr.getJSONObject(j);

                                        String title = devObj.optString("title", "Cihaz");


                                        // Sunucudan "lamp" diye yazı geliyor, biz onu resme çeviriyoruz.
                                        String iconStr = devObj.optString("iconRes", "lamp");
                                        int devIcon = getDrawableFromName(iconStr);
                                        int level = devObj.optInt("level", 0);
                                        boolean isOn = devObj.optBoolean("isOn", false);

                                        Module m = new Module(title, devIcon, isOn);
                                        m.level = level;

                                        roomModules.add(m);

                                        if(devObj.has("_id") ) {
                                            m.m_id = devObj.getString("_id");

                                        }
                                    }
                                }

                                // 3. Cihazları odaya yükle
                                room.setDevices(roomModules);
                                roomList.add(room);
                            }

                            // Listeyi Güncelle
                            roomAdapter.notifyDataSetChanged();

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(HomeActivity.this, "JSON Hatası: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(HomeActivity.this, "Bağlantı Hatası!", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        queue.add(jsonArrayRequest);
    }

    // =========================
    //  İKON ÇEVİRİCİ (String -> R.drawable)
    // =========================
    private int getDrawableFromName(String iconName) {
        if (iconName == null) return R.drawable.lightbulb;

        switch (iconName) {
            case "lamp":      return R.drawable.lightbulb;
            case "ac":        return R.drawable.thermostat;
            case "curtain":   return R.drawable.curtain__1_; // Senin dosya ismin
            case "security":  return R.drawable.security_system;
            case "sensor":    return R.drawable.motion_sensor;
            default:          return R.drawable.lightbulb; // Bilinmiyorsa lamba göster
        }
    }


    // ==========================================
    // CİHAZ DURUMUNU GÜNCELLE (PUT İSTEĞİ)
    // ==========================================
    private void updateModuleInServer(Room room, Module module) {
        // Eğer ID yoksa işlem yapma (Hata çıkmasın)
        if (room.mid == null || module.m_id == null) {
            Toast.makeText(this, "Hata: Cihaz ID bulunamadı!", Toast.LENGTH_SHORT).show();
            return;
        }

        // URL Yapısı: http://.../api/rooms/ODA_ID/modules/CIHAZ_ID
        // (API_URL'in sonundaki "/api/rooms" kısmını temizlememiz gerekebilir, o yüzden baştan yapıyoruz)
        // Senin API_URL: "http://192.168.1.XX:3000/api/rooms"

        String updateUrl = API_URL + "/" + room.mid + "/modules/" + module.m_id;

        // Gönderilecek Veri (JSON)
        JSONObject putData = new JSONObject();
        try {
            putData.put("isOn", module.isOn);
            putData.put("level", module.level);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Volley PUT İsteği
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.PUT,
                updateUrl,
                putData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Başarılı! Sunucu güncellendi.
                        Log.d("Sunucu", "Güncellendi: " + module.title);
                        // Kullanıcıyı rahatsız etmeye gerek yok, sessizce halletsin.
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null && error.networkResponse.statusCode == 400) {
                            Toast.makeText(HomeActivity.this, "⚠️ Bu isimde bir oda zaten var!", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(HomeActivity.this, "Bağlantı Hatası: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

}//SQLite ile veri iletişimi CRUD - Sanallaştırma (mongo db )  ile sunucu kurulumu - Sunucu veritabanı...Asenkron Strogare...