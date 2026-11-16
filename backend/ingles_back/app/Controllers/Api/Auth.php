<?php
 
namespace App\Controllers\Api;

use CodeIgniter\RESTful\ResourceController;
//use App\Controllers\BaseController;
use CodeIgniter\API\ResponseTrait;
use App\Models\TeacherModel;
use App\Libraries\JWTAuth;
use App\Models\StudentModel;
use Config\Email;
use Firebase\JWT\JWT;
use Firebase\JWT\Key;
 
class Auth extends ResourceController
{
    protected $format = 'json';
 
    public function login_teacher()
    {
        $json = $this->request->getJSON(true) ?? [];
        $key = strtoupper(trim($json['teacher_key'] ?? ''));
        $password = $json['password'] ?? '';
 
        if ($key === '' || $password === '') {
            return $this->failValidationErrors("Your key and password are both needed");
        }

        $model = new TeacherModel();
        $user = $model->where('teacher_key', $key)->first();
 
        if (!$user || !password_verify($password, $user['hash'])) {
            return $this->failUnauthorized('Your credentiales are incorrect');
        }

        $payload = [
            'id'    => $user['id'],
            'name' => $user['name'],
            'teacher_key' => $user['teacher_key'],
            'rol' => 'teacher',
        ];

        $token = JWTAuth::generateToken($payload);
 
        return $this->respond([
            'status'  => 200,
            'message' => 'Logged in succesfully',
            'token'   => $token,
            'user'    => $payload
        ]);
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

    public function register_student() {
        $json = $this->request->getJSON(true) ?? [];
        $name = trim($json['name'] ?? '');
        $enrollment = trim($json['enrollment'] ?? '');
        $email = strtolower(trim($json['email'] ?? ''));
        $course = trim($json['course']) ?? '';
        $password = $json['password'] ?? '';

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

        $id = $model->createWithLevel([
            'name' => $name,
            'enrollment' => $enrollment,
            'email' => $email,
            'course' => $course,
            'password' => $password,
            'hash' => password_hash($password,PASSWORD_DEFAULT),
        ]);
        if (!$id) {
            return $this->fail("The account could not be created", 500);
        }

        $payload = [
            'id' => (int) $id,
            'name' => $name,
            'enrollment' => $enrollment,
            'level' => 'A1'
        ];

        return $this->respondCreated([
            'status' => 201,
            'message' => 'Registro exitoso',
            //'token' => JWTAuth::generateToken($payload),
            'user' => $payload
        ]);
    }

}