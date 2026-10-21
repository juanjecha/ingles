package com.example.ingles_app;

public class Student {
    private String name, enrollment, email, course, password, hash;
    //constructor
    public Student(String name, String enrollment, String email, String course, String password, String hash){
        this.name = name;
        this.enrollment = enrollment;
        this.email = email;
        this.course = course;
        this.password = password;
        this.hash = hash;
    }
}
