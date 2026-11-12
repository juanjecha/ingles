<?php
namespace App\Models;

use CodeIgniter\Model;

class ProgressModel extends Model {
    protected $table = "progress";

    protected $primaryKey = 'id';
    protected $allowedFields = ['student_id', 'category_id', 'passed', 'score'];
    protected $returnType = 'array';

    public function getAll($user) {
        return $this->select('category_id, passed, score')
                     ->where('student_id', (int) $user->id)
                     ->findAll();
    }

    public function checkExists($user, int $categoryId) {
        return $this->where('student_id', (int) $user->id)
                    ->where('category_id', $categoryId)
                    ->first();
    }
}