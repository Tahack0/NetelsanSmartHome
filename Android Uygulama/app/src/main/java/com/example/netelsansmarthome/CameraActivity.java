package com.example.netelsansmarthome;

import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.squareup.picasso.Picasso;

public class CameraActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        ImageView cam1 = findViewById(R.id.cam1);
        ImageView cam2 = findViewById(R.id.cam2);


        Picasso.get()
                .load("https://images.unsplash.com/photo-1583847661641-180591700841?w=600")
                .placeholder(R.drawable.television) // Yüklenirken bu ikon görünsün
                .into(cam1);


        Picasso.get()
                .load("https://images.unsplash.com/photo-1560185007-cde436f6a4d0?w=600")
                .placeholder(R.drawable.security_system)
                .into(cam2);
    }
}