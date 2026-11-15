<?php

namespace App\Controllers\Api;
use CodeIgniter\RESTful\ResourceController;
use App\Libraries\JWTAuth;
use App\Models\MaterialModel;
use App\Models\CategoryModel;
use App\Models\LevelModel;

class VocabularyController extends ResourceController {
    protected $format = 'json';
    protected $categoryModel;
    protected $flashcardModel;
    protected $levelModel;
    
    public function __construct()
    {
        $this->categoryModel = new CategoryModel();
        $this->flashcardModel = new MaterialModel();
        $this->levelModel = new LevelModel();
    }

    private function studentLevel(): ?string {
        $user = JWTAuth::$currentUser;
        if(($user->rol ?? '') !== 'student') {
            return null;
        }
        return $this->levelModel->levelOf((int) $user->id);
    }


    public function getCategories() {
        $level = $this->studentLevel();
        if ($level === null) {
            return $this->failForbidden('Only students can see this content');
        }

        $data = array_map(static fn ($c) => [
            'id' => (int) $c['id'],
            'name' => $c['name'],
            'difficulty' => $c['difficulty'],
            'ord' => (int) $c['ord'],
        ], $this->categoryModel->publishedForLevel($level));
        //$categories = $this->categoryModel->findAll();
        return $this->respond([
            'status' => 200,
            'data' => $data,
        ]);
    }

    public function getFlashcards() {
        $level = $this->studentLevel();
        if ($level === null) {
            return $this->failForbidden('Only students can see this content');
        }

        $categoryID = (int) $this->request->getGet('category_id');
        if ($categoryID <= 0) {
            return $this->failValidationErrors('Variable category_id is required');
        }

        //no student can see flashcards if they're not of their level
        $category = $this->categoryModel->where('id', $categoryID)
                                        ->where('status', 1)
                                        ->where('difficulty', $level)
                                        ->first();

        if (!$category) {
            return $this->failForbidden('That category is not available for your level');
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