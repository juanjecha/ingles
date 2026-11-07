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

    public function show_students(): array {
        return $this->select("student.id, student.name, student.enrollment, student.email, student.course, student_level.level")
        ->join("student_level", "student_level.id = student.id_student_level","left")
        ->orderBy("student.name", "ASC")->findAll();
    }

    /*
    public function students_list(){
        return $this->select("student.id, student.name, student.enrollment, student.email, student.course, student_level.level")
                    ->join("student_level", "student_level.id = student.id_student_level")
    }
    */
}