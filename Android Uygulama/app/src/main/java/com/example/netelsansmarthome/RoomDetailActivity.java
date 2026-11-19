package com.example.netelsansmarthome;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RoomDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_room_detail);

        TextView title = findViewById(R.id.txtRoomTitle);
        RecyclerView rv = findViewById(R.id.rvModules);

        Intent i = getIntent();
        title.setText(i.getStringExtra("room_name"));

        List<Module> list = new ArrayList<>();

        if (i.getBooleanExtra("hasLight", false))
            list.add(new Module("Light", R.drawable.lightbulb, false));
        if (i.getBooleanExtra("hasCurtain", false))
            list.add(new Module("Curtain", R.drawable.curtain, false));
        if (i.getBooleanExtra("hasClimate", false))
            list.add(new Module("Climate", R.drawable.thermostat, false));
        if (i.getBooleanExtra("hasSecurity", false))
            list.add(new Module("Security", R.drawable.security_system, false));
        if (i.getBooleanExtra("hasSensor", false))
            list.add(new Module("Sensor", R.drawable.motion_sensor, false));

        rv.setLayoutManager(new GridLayoutManager(this, 2));
        rv.setAdapter(new ModuleAdapter(list , null));
    }

}