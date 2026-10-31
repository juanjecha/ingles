<?php
namespace App\Models;

use CodeIgniter\Database\OCI8\Builder;
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

    public function connect($data = null) {
        $res = $this->where('enrollment' ,$data['enrollment'])->get()->getRows();
        if ($res && password_verify($data["password"], $res["password"])) {
            return true;
        } else return false;
    }

    public function show_students($data = null) {
        $query = "SELECT s.name, s.enrollment, s.course
                  FROM student s
                  INNER JOIN student_level "
    }
}