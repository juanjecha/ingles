<?php

namespace App\Controllers\Api;
use CodeIgniter\RESTful\ResourceController;
use App\Models\MaterialModel;
use App\Models\CategoryModel;

class VocabularyController extends ResourceController {
    protected $format = 'json';
    protected $categoryModel;
    protected $flashcardModel;
    
    public function __construct()
    {
        $this->categoryModel = new CategoryModel();
        $this->flashcardModel = new MaterialModel();
    }


    public function getCategories() {
        $data = array_map(static fn ($c) => [
            'id' => (int) $c['id'],
            'name' => $c['name'],
            'difficulty' => $c['difficulty'],
            'ord' => (int) $c['ord'],
        ], $this->categoryModel->published());
        //$categories = $this->categoryModel->findAll();
        return $this->respond([
            'status' => 200,
            'data' => $data,
        ]);
    }

    public function getFlashcards() {
        $categoryID = $this->request->getGet('category_id');
        if ($categoryID <= 0) {
            return $this->failValidationErrors('Variable category_id is required');
        }

        $data = array_map(static fn ($f) => [
            'id' => (int) $f['id'],
            'category_id' => (int) $f['category_id'],
            'word' => $f['word'],
            'translation' => $f['translation'],
            'img' => $f['img'],
        ], $this->flashcardModel->getFlashcardsByCat($categoryID));

        //$flashcards = $this->flashcardModel->getFlashcardsByCat($categoryID);

        return $this->respond([
            'status' => 200,
            'data' => $data
        ]);
    }
}