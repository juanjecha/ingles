package com.example.ingles_app;

import com.google.gson.annotations.SerializedName;

public class ApiRequest {
    @SerializedName("nombre")     private String nombre;
    @SerializedName("matricula")  private String matricula;
    @SerializedName("correo")     private String correo;
    @SerializedName("contrasena") private String contrasena;
    @SerializedName("carrera")    private String carrera;

    public ApiRequest(String nombre, String matricula, String correo, String contrasena, String carrera) {
        this.nombre = nombre;
        this.matricula = matricula;
        this.correo = correo;
        this.contrasena = contrasena;
        this.carrera = carrera;
    }
}