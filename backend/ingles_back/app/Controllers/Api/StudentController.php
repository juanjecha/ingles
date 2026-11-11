<?php

namespace App\Controllers\Api;

use CodeIgniter\RESTful\ResourceController;
use Firebase\JWT\JWT;
use Firebase\JWT\Key;
use App\Models\StudentModel;
use App\Libraries\JWTAuth;

class StudentController extends ResourceController
{
    protected $format = 'json';

    public function login_student() {

        $json = $this->request->getJSON(true) ?? [];
        $enrollment = trim((string) ($json['enrollment'] ?? ''));
        $password = $json['password'];

        if ($enrollment === '' || $password === '') {
            return $this->failValidationErrors('Matrícula y contraseña son obligatorias');
        }
        $model = new StudentModel();
        $student = $model->where('enrollment', $enrollment)->first();

        //validate data introduced by the user in database
        if (!$student || !password_verify($password, $student['hash'])) {
            return $this->failUnauthorized("Credenciales incorrectas");
        }

        $payload = [
            "id" => (int) $student['id'], 
            "name" => $student['name'], 
            "enrollment" => (string) $student['enrollment'],
            "rol" => "student",
        ];

        //$jwt = JWT::encode($payload, $this->key, 'HS256');
        return $this->respond([
            'status' => 200,
            'message' => 'Autenticación exitosa',
            'token' => JWTAuth::generateToken($payload, (int) env('JWT_STUDENT_TIME_TO_LIVE', 2592000)),
            'user' => $payload,
            ]);
    }

    public function students_list() {
        $user = JWTAuth::$currentUser;
        if (($user->rol ?? '') !== 'teacher') {
            return $this->failForbidden("Only teachers can see this view");
        }

        return $this->respond((new StudentModel())->show_students());
    }
}