package com.example.ingles_app;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ingles_app.api.LoginActivity;
import com.example.ingles_app.api.RegisterActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //if there's an an active session, show the main view
        if (new SessionManager(this).isLoggedIn()) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_welcome);

        Button btnGoLogin = findViewById(R.id.btnGoLogin);
        Button btnGoRegister = findViewById(R.id.btnGoRegister);

        //Verify if we have overlay permission
        //verifyOverlay();

        btnGoLogin.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        });

        btnGoRegister.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, RegisterActivity.class));
        });

        if (android.os.Build.VERSION.SDK_INT >= 37 && checkSelfPermission("android.permission.ACCESS_LOCAL_NETWORK")
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{"android.permission.ACCESS_LOCAL_NETWORK"}, 100);
        }
    }
}