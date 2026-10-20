<?php
namespace App\Models;

use CodeIgniter\Model;

class TeacherModel extends Model {
    protected $table = "student";

    protected $primaryKey = 'id';
    protected $allowedFields = [
        "name",
        "email",
        "password",
        "hash"
    ];
    protected $returnType = 'array';

    public function verify($student = null) {
        $answer = true;
        $stmodel = new StudentModel();
        $res = $stmodel->where("enrollment", $student["enrollment"])
                       ->where("password", $student["password"])
                       ->first();
        if(!$res) {
            $answer = false;
        }
        return $answer;
    }
}