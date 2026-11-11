package com.example.ingles_app.api;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ingles_app.DB.LoginRequest;
import com.example.ingles_app.R;
import com.example.ingles_app.SessionManager;
import com.example.ingles_app.repository.VocabularyRepository;
import com.example.ingles_app.HomeActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LOGIN";

    private EditText etMatricula, etPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etMatricula = findViewById(R.id.etLoginEmail);
        etPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> login());

        TextView tvToRegister = findViewById(R.id.tvToRegister);
        tvToRegister.setOnClickListener(v ->
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
    }

    private void login() {
        String matricula = etMatricula.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (matricula.isEmpty() || password.isEmpty()) {
            showMessage("Escribe tu matrícula y contraseña");
            return;
        }

        btnLogin.setEnabled(false);

        ApiCliente.getApiService().studentLogin(new LoginRequest(matricula, password))
                .enqueue(new Callback<ApiResponse>() {
                    @Override
                    public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                        btnLogin.setEnabled(true);
                        Log.d(TAG, "Codigo HTTP; " + response.code());

                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse body = response.body();
                            String token = body.getToken();
                            ApiResponse.User user = body.getUser();

                            if (token != null && user != null) {
                                boolean otherStudent = new SessionManager(getApplicationContext())
                                        .save(token, user.id, user.name);

                                //login from another student in the same device, delete data from the last one
                                if (otherStudent) {
                                    new VocabularyRepository(getApplicationContext()).clearLocalData();
                                }

                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } else {
                            showMessage("Unexpected server response");
                            }
                        } else if (response.code() == 401) {
                            showMessage("incorrect credentials");
                        } else {
                            showMessage("Error " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse> call, Throwable t) {
                        btnLogin.setEnabled(true);
                        Log.e(TAG, "Fallo de conexión", t);
                        showMessage("Error de conexión: " + t.getMessage());
                    }
                });
        }

        private void showMessage(String message) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }