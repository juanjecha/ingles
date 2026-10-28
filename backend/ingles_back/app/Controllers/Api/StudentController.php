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
    private $key = "84981e652b16c9bf56896419911e1aeb74955fb76c5c2d24205daa575a97c65d";

    public function login_student() {
        $enrollment = $this->request->getVar('enrollment');
        $password = $this->request->getVar('password');
        $model = new StudentModel();

        $student = $model->where('enrollment', $enrollment)->first();

        //validate data introduced by the user in database
        if (!$student || !password_verify($password, $student['hash'])) {
            return $this->failUnauthorized("Credenciales incorrectas");
        }

        //$key = getenv("JWT_SECRET_KEY")?:'1de46771b19ad62fe0d54c23072ca4de3bc9e0ebf1006a2c931630ac3378eee2';

        $payload = [
            "iat" => time(),
            "exp" => time() + 3600,
            "data" => [
                "enrollment" => $enrollment
            ],
        ];
        $jwt = JWT::encode($payload, $this->key, 'HS256');
        return $this->respond(['token' => $jwt, 'message' => 'Autenticación exitosa']);
    }
}