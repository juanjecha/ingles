<?php
namespace App\Models;

use CodeIgniter\Model;

class CategoryModel extends Model {
    protected $table = "category";

    protected $primaryKey = 'id';
    protected $allowedFields = ['name', 'difficulty'];
    protected $returnType = 'array';

    public function list() {
        return $this->select('*')->orderBy('name')->findAll();
    }
}