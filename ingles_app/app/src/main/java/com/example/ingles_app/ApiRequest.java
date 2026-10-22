package com.example.ingles_app;

import com.google.gson.annotations.SerializedName;

public class ApiRequest {
    @SerializedName("name")     private String nombre;
    @SerializedName("enrollment")  private String matricula;
    @SerializedName("email")     private String correo;
    @SerializedName("password") private String contrasena;
    @SerializedName("course")    private String carrera;

    public ApiRequest(String nombre, String matricula, String correo, String contrasena, String carrera) {
        this.nombre = nombre;
        this.matricula = matricula;
        this.correo = correo;
        this.contrasena = contrasena;
        this.carrera = carrera;
    }
}