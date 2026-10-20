<?php
namespace App\Controllers;

use CodeIgniter\Controller;
use App\Models\StudentModel;
class Student extends Controller {

public function save_student() {
    $json = $this->request->getJSON(true);
    $model = new StudentModel();

    $password = password_hash($json["password"], PASSWORD_DEFAULT);
    
    $id = $model->insert([
        "name"=> $json["name"],
        "enrollment" => $json["enrollment"],
        "email" => $json["email"],
        "password" => $json["password"],
        "hash" => $json["hash"]
    ]
    );
    
    return $this->response->setJSON([
        "success" => true,
        "id" => $id
    ]);
}

}