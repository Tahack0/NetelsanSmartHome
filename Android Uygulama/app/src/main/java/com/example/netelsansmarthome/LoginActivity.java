package com.example.netelsansmarthome;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText edtUser = findViewById(R.id.edtUsername);
        EditText edtPass = findViewById(R.id.edtPassword);
        Button btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {
            String u = edtUser.getText().toString();
            String p = edtPass.getText().toString();

            // Basit doğrulama (Buraya veritabanı kontrolü gelecek)
            if (u.equals("admın") && p.equals("1234")) {
                // Giriş Başarılı -> HomeActivity'e git
                Intent i = new Intent(LoginActivity.this, HomeActivity.class);
                startActivity(i);
                finish(); // Geri tuşuna basınca login'e dönmesin
            } else {
                Toast.makeText(this, "Hatalı Kullanıcı Adı veya Şifre", Toast.LENGTH_SHORT).show();
            }
        });
    }
}