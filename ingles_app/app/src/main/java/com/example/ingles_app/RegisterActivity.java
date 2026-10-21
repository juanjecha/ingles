package com.example.ingles_app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "REGISTRO";

    private EditText name, mat, email, pass;
    private Spinner carr;
    private Button btnRegister;
    private String jwtToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (View v, WindowInsetsCompat insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        name = findViewById(R.id.etRegisterName);
        mat = findViewById(R.id.etRegisterEnrollment);
        email = findViewById(R.id.etRegisterEmail);
        pass = findViewById(R.id.etRegisterPassword);

        String[] carrera = {"Elige carrera", "ISTII", "IAA", "IPGI"};
        carr = findViewById(R.id.actvRegisterCareer);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, carrera);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        carr.setAdapter(adapter);

        btnRegister = findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(v -> guardaAlumno());

        String token = getIntent().getStringExtra("JWT_TOKEN");
        if (token == null) {
            token = getSharedPreferences("preferencias", MODE_PRIVATE).getString("JWT_TOKEN", null);
        }
        jwtToken = "Bearer " + token;
    }

    public void guardaAlumno() {
        String sname = name.getText().toString().trim();
        String smat = mat.getText().toString().trim();
        String semail = email.getText().toString().trim();
        String spass = pass.getText().toString().trim();

        if (sname.isEmpty() || smat.isEmpty() || semail.isEmpty() || spass.isEmpty()) {
            mostrarMensaje("Hay campos vacíos");
            return;
        }

        if (carr.getSelectedItemPosition() == 0) {
            mostrarMensaje("Selecciona una carrera");
            return;
        }

        String scarr = carr.getSelectedItem().toString();

        // Evita dobles clics mientras se envía la petición
        btnRegister.setEnabled(false);
        mostrarMensaje("Enviando registro...");
        Log.d(TAG, "Enviando registro de " + smat);

        try {
            ApiRequest apiRequest = new ApiRequest(sname, smat, semail, spass, scarr);

            ApiCliente.getApiService().studentRegister(jwtToken, apiRequest).enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    btnRegister.setEnabled(true);
                    try {
                        Log.d(TAG, "Código HTTP: " + response.code());

                        if (response.isSuccessful() && response.body() != null) {
                            String token = response.body().getToken();
                            if (token != null) {
                                SharedPreferences preferences = getSharedPreferences("preferencias", MODE_PRIVATE);
                                preferences.edit().putString("JWT_TOKEN", token).apply();
                            }

                            mostrarMensaje("Registro exitoso");
                            limpiarFormulario();
                        } else {
                            String detalle = "";
                            if (response.errorBody() != null) {
                                detalle = response.errorBody().string();
                                Log.e(TAG, "Respuesta de error: " + detalle);
                            }
                            mostrarMensaje("Error " + response.code() + (detalle.isEmpty() ? "" : ": " + detalle));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error al procesar la respuesta", e);
                        mostrarMensaje("Error al procesar respuesta: " + e);
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    btnRegister.setEnabled(true);
                    Log.e(TAG, "Fallo en la petición", t);
                    String msg = (t.getMessage() != null) ? t.getMessage() : t.toString();
                    mostrarMensaje("Error de conexión: " + msg);
                }
            });
        } catch (Exception e) {
            // Errores al construir la petición (por ejemplo, anotaciones mal escritas en ApiService)
            btnRegister.setEnabled(true);
            Log.e(TAG, "Error al crear la petición", e);
            mostrarMensaje("Error interno: " + e);
        }
    }

    private void limpiarFormulario() {
        name.setText("");
        mat.setText("");
        email.setText("");
        pass.setText("");
        carr.setSelection(0);
        name.requestFocus();
    }

    private void mostrarMensaje(String texto) {
        if (isFinishing() || isDestroyed()) return;
        Toast.makeText(RegisterActivity.this, texto, Toast.LENGTH_LONG).show();
    }
}