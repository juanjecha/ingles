<?php
namespace App\Models;

use CodeIgniter\Model;

class CategoryModel extends Model {
    protected $table = "category";

    protected $primaryKey = 'id';
    protected $allowedFields = ['name', 'difficulty', 'ord', 'status'];
    protected $returnType = 'array';

    public function list() {
        return $this->select('*')->orderBy('name')->findAll();
    }

    public function published(): array {
        return $this->select('id, name, difficulty, ord')
        ->where('status', 1)
        ->orderBy('ord', 'ASC')
        ->findAll();
    }

    public function next(array $category) {
        return $this->where('status', 1)
                    ->where('ord >', (int) $category['ord'])
                    ->orderBy('ord', 'ASC')
                    ->first();
    }
}