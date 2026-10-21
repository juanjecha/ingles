<?php
 
namespace App\Controllers\Api;
 
use CodeIgniter\RESTful\ResourceController;
use App\Models\TeacherModel;
use App\Libraries\JWTAuth;
use App\Models\StudentModel;
use Firebase\JWT\JWT;
use Firebase\JWT\Key;
 
class Auth extends ResourceController
{
    protected $format = 'json';
 
    public function login_teacher()
    {
        $json = $this->request->getJSON(true) ?? [];
        $email = strtolower(trim($json['email'] ?? ''));
        $password = $json['password'];
 
        $userModel = new TeacherModel();
        $user = $userModel->where('email', $email)->first();
 
        if (!$user || !password_verify($password, $user['password'])) {
            return $this->failUnauthorized('Credenciales incorrectas.');
        }
 
        $payload = [
            'id'    => $user['id'],
            'email' => $user['email'],
            'rol'   => $user['rol'] // 'estudiante', 'docente', 'admin'
        ];
 
        $token = JWTAuth::generateToken($payload);
 
        return $this->respond([
            'status'  => 200,
            'message' => 'Login exitoso',
            'token'   => $token,
            'user'    => $payload
        ]);
    }

    public function login_student() {
        $student = $this->request->getJson(true);
        log_message("debug",print_r($student, true));
        $pass = $student["password"];
        $stModel = new StudentModel();
        $var = $stModel->verify();
        if($var) {
            $key = $pass;
            $payload= [
                "uid" => 1,
                "user"=>"student",
                "exp"=>time()+3600
            ];
        }
        return $this->response->setStatusCode(401)
        ->setJSON(["error"=> "Credenciales no válidas"]);
    }

    public function register_teacher() {
        $json = $this->request->getJSON(true) ?? [];
        $name = trim($json['name'] ?? '');
        $email = strtolower(trim($json['email'] ?? ''));
        $password = $json['password'] ?? '';

        if ($name === '' || $email === '' || $password === '') {
            return $this->failValidationErrors("Nombre, correo y contraseña son obligatorios");
        }
        if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
            return $this->failValidationErrors("El correo no es válido");
        }

        $model = new TeacherModel();
        if ($model->where('email', $email)->first()) {
            return $this->fail("Ese correo ya está registrado", 409);
        }

        $id = $model->insert([
            'name' => $name,
            'email' => $email,
            'password' => $password,
            'hash' => password_hash($password, PASSWORD_DEFAULT)
        ]);

        return $this->respondCreated(['success' => true, 'id' => $id]);
    }

}