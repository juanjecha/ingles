<?php
namespace App\Models;

use CodeIgniter\Model;

class MaterialModel extends Model {
    protected $table = "resource";

    protected $primaryKey = 'id';
    protected $allowedFields = ['categoria_id', 'name', 'img', 'level'];

    public function save_material(int $category, string $word, $) {
        model
    }
}