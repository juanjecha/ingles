<?php
namespace App\Models;

use CodeIgniter\Model;

class MaterialModel extends Model {
    protected $table = "resource";

    protected $primaryKey = 'id';
    protected $allowedFields = ['category_id', 'name', 'img', 'level'];

    public function save_material(int $category, string $word, $string image) {
        $model = new MaterialModel();
            $model->insert([
                'word' => $word,
                'img' => $word,
                'category' => $category,
            ]);

        return $this->insert($data);
    }
}