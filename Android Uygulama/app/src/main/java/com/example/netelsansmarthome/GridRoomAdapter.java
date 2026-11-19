package com.example.netelsansmarthome;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class GridRoomAdapter extends RecyclerView.Adapter<GridRoomAdapter.VH> {

    private final List<Room> roomList;
    private Context context;
    private DatabaseHelper dbHelper;
    private String currentCategory = "Aydınlatma"; // Varsayılan

    public GridRoomAdapter(List<Room> rooms) {
        this.roomList = rooms;
    }

    public void setCategory(String category) {
        this.currentCategory = category;
        notifyDataSetChanged();
    }

    // 🔍 YARDIMCI FONKSİYON: Odanın içinde kategoriye uygun cihazı bulup getirir
    // Örn: Kategori "Aydınlatma" ise, odadaki ilk lambayı bulur.
    private Module getTargetModule(Room room) {
        if (room.devices == null) return null;

        for (Module m : room.devices) {
            String title = m.title.toLowerCase();
            String cat = currentCategory.toLowerCase();

            if (cat.contains("aydınlatma") || cat.contains("lamba")) {
                if (title.contains("lamba") || title.contains("ışık") || title.contains("avize") || title.contains("spot")) return m;
            }
            else if (cat.contains("perde")) {
                if (title.contains("perde") || title.contains("stor") || title.contains("panjur")) return m;
            }
            else if (cat.contains("iklim") || cat.contains("klima")) {
                if (title.contains("klima") || title.contains("iklim") || title.contains("termostat")) return m;
            }
            else if (cat.contains("güvenlik") || cat.contains("kamera")) {
                if (title.contains("kamera") || title.contains("güvenlik")) return m;
            }
        }
        return null;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (context == null) context = parent.getContext();
        if (dbHelper == null) dbHelper = new DatabaseHelper(context);

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_smart_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Room r = roomList.get(pos);

        h.txtTitle.setText(r.name);
        h.imgIcon.setImageResource(r.iconRes);

        // Önce görünümü temizle
        resetViews(h);

        // Bu odada, seçili kategoriye uygun bir cihaz var mı? (Örn: Mutfakta klima var mı?)
        Module targetModule = getTargetModule(r);

        if (targetModule != null) {
            // CİHAZ BULUNDU -> KONTROLLERİ AKTİF ET
            setupActiveState(h, targetModule);
        } else {
            // CİHAZ YOK -> KARTI PASİF YAP
            setupEmptyState(h);
        }
    }

    // ==========================================
    // 🟢 CİHAZ VARSA ÇALIŞACAK KODLAR
    // ==========================================
    private void setupActiveState(VH h, Module m) {
        h.btnPower.setEnabled(true);
        h.btnPower.setAlpha(1.0f);

        // 1. RENK VE DURUM GÜNCELLEME
        updateColor(h, m.isOn);

        // 2. TIKLAMA İŞLEMİ (Veritabanına Kaydetme)
        h.btnPower.setOnClickListener(v -> {
            if (!currentCategory.toLowerCase().contains("güvenlik")) { // Kameralar düğme ile açılmaz
                m.isOn = !m.isOn;
                dbHelper.updateModule(m); // ✅ VERİTABANINA KAYDET
                updateColor(h, m.isOn);

                // Slider vb. varsa onun görünürlüğünü güncelle
                notifyItemChanged(h.getAdapterPosition());
            }
        });

        // 3. KATEGORİYE ÖZEL KONTROLLER
        // --- AYDINLATMA & PERDE ---
        if (currentCategory.equalsIgnoreCase("Aydınlatma") || currentCategory.equalsIgnoreCase("Perde")) {
            h.seekBar.setVisibility(View.VISIBLE);
            h.seekBar.setProgress(m.level);
            h.seekBar.setEnabled(m.isOn); // Kapalıysa slider çalışmasın

            updateStatusText(h, m);

            h.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar s, int p, boolean fromUser) {
                    if (fromUser) {
                        m.level = p;
                        updateStatusText(h, m);
                    }
                }
                @Override
                public void onStopTrackingTouch(SeekBar s) {
                    dbHelper.updateModule(m); // ✅ SLIDER BIRAKILINCA KAYDET
                }
                @Override public void onStartTrackingTouch(SeekBar s) {}
            });
        }
        // --- İKLİM ---
        else if (currentCategory.contains("İklim") || currentCategory.contains("Klima")) {
            h.layoutClimate.setVisibility(View.VISIBLE);
            h.txtTempDisplay.setText(m.level + "°C");
            updateStatusText(h, m);

            // Klima kapalıysa tuşlara basılmasın
            h.btnPlus.setEnabled(m.isOn);
            h.btnMinus.setEnabled(m.isOn);
            h.layoutClimate.setAlpha(m.isOn ? 1.0f : 0.4f);

            h.btnPlus.setOnClickListener(v -> {
                if (m.level < 35) {
                    m.level++;
                    h.txtTempDisplay.setText(m.level + "°C");
                    updateStatusText(h, m);
                    dbHelper.updateModule(m); // ✅ KAYDET
                }
            });

            h.btnMinus.setOnClickListener(v -> {
                if (m.level > 16) {
                    m.level--;
                    h.txtTempDisplay.setText(m.level + "°C");
                    updateStatusText(h, m);
                    dbHelper.updateModule(m); // ✅ KAYDET
                }
            });
        }
        // --- GÜVENLİK ---
        else if (currentCategory.contains("Güvenlik")) {
            h.btnWatchCamera.setVisibility(View.VISIBLE);
            h.txtStatus.setText("Kamera Aktif");
            h.txtStatus.setTextColor(Color.parseColor("#4CAF50"));

            // Güç butonu kamerada gizlensin veya işlevsiz olsun
            h.btnPower.setVisibility(View.GONE);

            h.btnWatchCamera.setOnClickListener(v -> {
                try {
                    context.startActivity(new Intent(context, CameraActivity.class));
                } catch (Exception e) {
                    Toast.makeText(context, "Kamera açılamadı", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // ==========================================
    // 🔴 CİHAZ YOKSA ÇALIŞACAK KODLAR
    // ==========================================
    private void setupEmptyState(VH h) {
        h.txtStatus.setText("Cihaz Yok");
        h.txtStatus.setTextColor(Color.GRAY);

        h.btnPower.setEnabled(false);
        h.btnPower.setAlpha(0.3f);
        h.btnPower.setColorFilter(Color.GRAY);

        GradientDrawable bg = (GradientDrawable) h.btnPower.getBackground();
        if (bg != null) bg.setColor(Color.parseColor("#EEEEEE"));
    }

    // 🧹 Temizlik
    private void resetViews(VH h) {
        h.seekBar.setVisibility(View.GONE);
        h.layoutClimate.setVisibility(View.GONE);
        h.btnWatchCamera.setVisibility(View.GONE);
        h.btnPower.setVisibility(View.VISIBLE);
    }

    private void updateStatusText(VH h, Module m) {
        if (!m.isOn) {
            h.txtStatus.setText("Kapalı");
            h.txtStatus.setTextColor(Color.GRAY);
            return;
        }

        h.txtStatus.setTextColor(Color.parseColor("#D32F2F"));

        if (currentCategory.equalsIgnoreCase("Aydınlatma")) h.txtStatus.setText("Parlaklık: %" + m.level);
        else if (currentCategory.equalsIgnoreCase("Perde")) h.txtStatus.setText("Açıklık: %" + m.level);
        else if (currentCategory.contains("İklim")) h.txtStatus.setText("Hedef: " + m.level + "°C");
        else h.txtStatus.setText("Açık");
    }

    private void updateColor(VH h, boolean isOn) {
        GradientDrawable bg = (GradientDrawable) h.btnPower.getBackground();
        if (isOn) {
            h.btnPower.setColorFilter(Color.WHITE);
            if (bg != null) bg.setColor(Color.parseColor("#D32F2F"));
        } else {
            h.btnPower.setColorFilter(Color.parseColor("#BDBDBD"));
            if (bg != null) bg.setColor(Color.parseColor("#EEEEEE"));
        }
    }

    @Override
    public int getItemCount() { return roomList.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView imgIcon, btnPower;
        TextView txtTitle, txtStatus;
        SeekBar seekBar;
        LinearLayout layoutClimate;
        TextView btnPlus, btnMinus, txtTempDisplay;
        Button btnWatchCamera;

        VH(View v) {
            super(v);
            imgIcon = v.findViewById(R.id.imgIcon);
            btnPower = v.findViewById(R.id.btnPower);
            txtTitle = v.findViewById(R.id.txtTitle);
            txtStatus = v.findViewById(R.id.txtStatus);
            seekBar = v.findViewById(R.id.seekBarBrightness);
            layoutClimate = v.findViewById(R.id.layoutClimate);
            btnPlus = v.findViewById(R.id.btnPlus);
            btnMinus = v.findViewById(R.id.btnMinus);
            txtTempDisplay = v.findViewById(R.id.txtTempDisplay);
            btnWatchCamera = v.findViewById(R.id.btnWatchCamera);
        }
    }
}