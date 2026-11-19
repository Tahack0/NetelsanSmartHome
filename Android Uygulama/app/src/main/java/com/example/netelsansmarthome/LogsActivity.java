package com.example.netelsansmarthome;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class LogsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logs); // Aşağıda layoutu var

        RecyclerView rv = findViewById(R.id.rvLogs);
        rv.setLayoutManager(new LinearLayoutManager(this));

        List<LogItem> logs = new ArrayList<>();
        // Örnek Veriler
        logs.add(new LogItem("Salon Kamera", "Hareket Algılandı", "14:32"));
        logs.add(new LogItem("Giriş Kapısı", "Kapı Açıldı", "12:15"));
        logs.add(new LogItem("Mutfak Sensör", "Gaz Algılanmadı", "09:00"));
        logs.add(new LogItem("Klima" , " Sıcaklık Artırıldı " ,  "18.45"));
        rv.setAdapter(new LogAdapter(logs));
    }
}