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
    ];
    protected $returnType = 'array';

    public function createWithLevel(array $data) {
        $this->db->transBegin();

        try {
            $studentId = $this->insert($data);

            $this->db->table('student_level')->insert([
                'student_id' => $studentId,
                'enrollment' => $data['enrollment'],
                'level' => 'A1'
            ]);

            $this->db->transCommit();
            return (int) $studentId;
        } catch (\Throwable $e){
            $this->db->transRollback();
            log_message('error', 'createWithLevel: ' . $e->getMessage());
            return false;
        }
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