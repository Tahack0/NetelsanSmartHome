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
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class GridModuleAdapter extends RecyclerView.Adapter<GridModuleAdapter.VH> {

    // 🌟 KÖPRÜ (INTERFACE) TANIMI
    // Activity ile Adapter'in konuşmasını sağlar.
    public interface OnModuleChangeListener {
        void onModuleChanged(Module module);
    }

    private final List<Module> moduleList;
    private OnModuleChangeListener listener; // Dinleyici
    private Context context;

    // ✅ YENİ YAPICI METOT (Listener ekledik)
    public GridModuleAdapter(List<Module> modules, OnModuleChangeListener listener) {
        this.moduleList = modules;
        this.listener = listener;
    }

    // Eski yapıcı metot (Hata vermesin diye kalsın ama kullanma)
    public GridModuleAdapter(List<Module> modules) {
        this.moduleList = modules;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (context == null) context = parent.getContext();
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_smart_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Module m = moduleList.get(pos);
        String title = (m.title != null) ? m.title.toLowerCase() : "";

        h.txtTitle.setText(m.title);
        // Eğer sunucudan "lamp" diye string geliyorsa burada patlamaz, çünkü int iconRes tutuyoruz.
        // HomeActivity'de çevirip buraya int olarak verdiğimiz için sorun yok.
        h.imgIcon.setImageResource(m.iconRes);

        updateUI(h, m);

        // 🟢 AÇMA / KAPAMA BUTONU
        h.btnPower.setOnClickListener(v -> {
            if (!title.contains("sensör")) {
                m.isOn = !m.isOn; // 1. Yerel durumu değiştir (Hızlı tepki için)
                updateUI(h, m);   // 2. Görüntüyü güncelle

                // 3. SUNUCUYA HABER VER! 📡
                if (listener != null) listener.onModuleChanged(m);
            }
        });

        // =================================================
        // 1. AYDINLATMA & PERDE GRUBU (SeekBar)
        // =================================================
        boolean isLight = title.contains("lamba") || title.contains("ışık") || title.contains("avize") || title.contains("spot");
        boolean isCurtain = title.contains("perde") || title.contains("panjur");

        if (isLight || isCurtain) {
            setupControlMode(h, true, false, false);
            h.seekBar.setProgress(m.level);

            h.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar s, int p, boolean fromUser) {
                    if(fromUser) {
                        m.level = p;
                        h.txtStatus.setText((isLight ? "Parlaklık: %" : "Açıklık: %") + p);
                    }
                }
                @Override public void onStartTrackingTouch(SeekBar s) {

                }
                @Override
                public void onStopTrackingTouch(SeekBar s) {
                    // Elini çektiğinde sunucuya kaydet
                    if (listener != null) listener.onModuleChanged(m);
                }
            });
        }
        // =================================================
        // 2. İKLİM GRUBU (+ / - Tuşları)
        // =================================================
        else if (title.contains("iklim") || title.contains("klima")) {
            setupControlMode(h, false, true, false);
            h.txtTempDisplay.setText(m.level + "°C");

            if(m.isOn) {
                h.txtStatus.setText("Hedef: " + m.level + "°C");
            }

            // ARTI (+) BUTONU
            h.btnPlus.setOnClickListener(v -> {
                if (m.isOn && m.level < 35) { // Sadece cihaz açıksa çalışsın
                    m.level++; // 1. Değeri artır

                    // 2. EKRANI ANINDA GÜNCELLE (Gecikme olmasın)
                    h.txtTempDisplay.setText(m.level + "°C");
                    h.txtStatus.setText("Hedef: " + m.level + "°C");
                    h.btnPlus.setText("+");


                    // 3. Sunucuya haber ver
                    if (listener != null) listener.onModuleChanged(m);
                }
            });

            // EKSİ (-) BUTONU
            h.btnMinus.setOnClickListener(v -> {
                if (m.isOn && m.level > 16) {
                    m.level--; // 1. Değeri azalt

                    // 2. EKRANI ANINDA GÜNCELLE
                    h.txtTempDisplay.setText(m.level + "°C");
                    h.txtStatus.setText("Hedef: " + m.level + "°C");
                    h.btnMinus.setText("-");

                    // 3. Sunucuya haber ver
                    if (listener != null) listener.onModuleChanged(m);
                }
            });
        }
        // =================================================
        // 3. GÜVENLİK (Kamera)
        // =================================================
        else if (title.contains("güvenlik") || title.contains("kamera")) {
            setupControlMode(h, false, false, true);
            h.btnWatchCamera.setOnClickListener(v -> {
                try {
                    h.itemView.getContext().startActivity(new Intent(h.itemView.getContext(), CameraActivity.class));
                } catch (Exception e) {}
            });
        }
        // =================================================
        // 4. DİĞERLERİ
        // =================================================
        else {
            setupControlMode(h, false, false, false);
            if (title.contains("sensör")) {
                h.btnPower.setVisibility(View.GONE);
                h.txtStatus.setText("Normal");
            }
        }
    }

    private void setupControlMode(VH h, boolean showSeek, boolean showClimate, boolean showCam) {
        h.seekBar.setVisibility(showSeek ? View.VISIBLE : View.GONE);
        h.layoutClimate.setVisibility(showClimate ? View.VISIBLE : View.GONE);
        h.btnWatchCamera.setVisibility(showCam ? View.VISIBLE : View.GONE);
        h.btnPower.setVisibility(View.VISIBLE);
    }

    private void updateUI(VH h, Module m) {
        int activeColor = Color.parseColor("#D32F2F");
        int passiveColor = Color.parseColor("#BDBDBD");

        GradientDrawable bg = (GradientDrawable) h.btnPower.getBackground();

        if (m.isOn) {
            h.btnPower.setColorFilter(Color.WHITE);
            if (bg != null) bg.setColor(activeColor);
            h.txtStatus.setTextColor(activeColor);

            // Metin güncelleme mantığı
            String title = (m.title != null) ? m.title.toLowerCase() : "";
            if (title.contains("klima")) h.txtStatus.setText("Hedef: " + m.level + "°C");
            else if (title.contains("lamba")) h.txtStatus.setText("Parlaklık: %" + m.level);
            else h.txtStatus.setText("Açık");

            setControlsEnabled(h, true);
        } else {
            h.btnPower.setColorFilter(passiveColor);
            if (bg != null) bg.setColor(Color.parseColor("#EEEEEE"));
            h.txtStatus.setText("Kapalı");
            h.txtStatus.setTextColor(Color.parseColor("#757575"));

            setControlsEnabled(h, false);
        }
    }

    private void setControlsEnabled(VH h, boolean enabled) {
        float alpha = enabled ? 1.0f : 0.4f;
        h.seekBar.setEnabled(enabled);
        h.seekBar.setAlpha(alpha);
        h.layoutClimate.setAlpha(alpha);
        h.btnPlus.setEnabled(enabled);
        h.btnMinus.setEnabled(enabled);
    }

    @Override
    public int getItemCount() {
        return moduleList.size();
    }

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