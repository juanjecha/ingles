<?php
namespace App\Models;

use CodeIgniter\Model;

class StudentModel extends Model {
    protected $table = "student";

    protected $primaryKey = 'id';
    protected $allowedFields = [
        "name",
        "enrollment",
        "email",
        "course",
        "password",
        "hash",
        "id_student_level"
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