<?php
namespace App\Models;

use CodeIgniter\Model;

class MaterialModel extends Model {
    protected $table = "resource";

    protected $primaryKey = 'id';
    protected $allowedFields = ['category_id', 'name', 'img',];
    protected $returnType = 'array';

    public function save_material(int $category, string $word, string $img) {
            return $this->insert([
                'category_id' => $category,
                'name' => $word,
                'img' => $img,
                //'level' => $level,
            ]);
    }

    public function flashcard($data = null) {
        $query = "SELECT * FROM resource WHERE category = ".$data['category']." AND level <= ".$data['level'];
        $res = $this->query($query);
        return $res;
    }

    public function resources_list(): array
    {
        return $this->select("resource.id, resource.name, resource.img, category.name, category.difficulty, AS level")
                    ->join("category", "category.id = resource.category_id")
                    ->orderBy("resource.id", "DESC")
                    ->findAll();
    }
}