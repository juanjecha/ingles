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
        $email = $this->request->getPost('email');
        $password = $this->request->getPost('password');
 
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

}