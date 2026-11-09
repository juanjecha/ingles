<?php
namespace App\Models;

use CodeIgniter\Model;

class MaterialModel extends Model {
    protected $table = "resource";

    protected $primaryKey = 'id';
    protected $allowedFields = ['category_id', 'name', 'translation', 'img',];
    protected $returnType = 'array';

    public function save_material(int $category, string $word, string $translation, string $img) {
            return $this->insert([
                'category_id' => $category,
                'name' => $word,
                'translation' => $translation,
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
        return $this->select("resource.id, resource.name, resource.translation, resource.img, category.name AS category, category.difficulty AS level")
                    ->join("category", "category.id = resource.category_id")
                    ->orderBy("resource.id", "DESC")
                    ->findAll();
    }

    //Flascards by a published category (app)
    public function getFlashcardsByCat(int $categoryID): array {
        return $this->select('resource.id, resource.category_id, resource.name AS word, resource.translation, resource.img')
        ->join('category', 'category.id = resource.category_id')
        ->where('resource.category_id', $categoryID)
        ->where('category.status', 1)
        ->orderBy('resource.id', 'ASC')
        ->findAll();
    }
}