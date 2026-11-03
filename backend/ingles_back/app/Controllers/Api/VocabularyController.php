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
        $categories = $this->categoryModel->findAll();
        return $this->respond([
            'status' => 200,
            'data' => $categories,
        ]);
    }

    public function getFlashcards() {
        $categoryID = $this->request->getGet('category_id');
        if (!$categoryID) {
            return $this->failValidationErrors('Variable category_id is required');
        }

        $flashcards = $this->flashcardModel->getFlashcardsByCat($categoryID);

        return $this->respond([
            'status' => 200,
            'data' => $flashcards
        ]);
    }
}