<?php

namespace App\Controllers\Api;

use CodeIgniter\RESTful\ResourceController;
use App\Models\StudentModel;
use App\Libraries\JWTAuth;

class StudentController extends ResourceController
{
    protected $format = 'json';

    public function register_student() {
        $json = $this->request->getJSON(true) ?? [];
        $name = trim($json['name'] ?? '');
        $enrollment = trim($json['enrollment'] ?? '');
        $email = strtolower(trim($json['email'] ?? ''));
        $course = trim($json['course']);
        $password = $json['password'];

        if ($name === '' || $enrollment === '' || $email === '' || $course === '' || $password === '') {
            return $this->failValidationErrors("Todos los campos son obligatorios");
        }
        if (!ctype_digit($enrollment) || strlen($enrollment) > 9) {
            return $this->failValidationErrors("La matrícula debe ser numérica y de 9 digitos");
        }
        if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
            return $this->failValidationErrors("El correo no es válido");
        }
        if (!in_array($course, ["ISTII", "IAA", "IPGI"], true)) {
            return $this->failValidationErrors("La carrera no es válida");
        }

        $model = new StudentModel();
        if ($model->where("enrollment", $enrollment)->first()) {
            return $this->fail("Matrícula ya registrado", 409);
        }
        if($model->where("email", $email)->first()) {
            return $this->fail("Ese correo ya está registrado",409);
        }

        $id = $model->insert([
            'name' => $name,
            'enrollment' => $enrollment,
            'email' => $email,
            'course' => $course,
            'hash' => password_hash($password,PASSWORD_DEFAULT),
        ]);

        $payload = [
            'id' => (int) $id,
            'name' => $name,
            'enrollment' => $enrollment,
        ];

        return $this->respondCreated([
            'status' => 201,
            'message' => 'Registro exitoso',
            //'token' => JWTAuth::generateToken($payload),
            'user' => $payload
        ]);
    }
}