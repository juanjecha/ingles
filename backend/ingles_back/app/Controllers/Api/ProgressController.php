<?php

namespace App\Controllers\Api;

use App\Libraries\JWTAuth;
use App\Models\CategoryModel;
use App\Models\ProgressModel;
use CodeIgniter\RESTful\ResourceController;

class ProgressController extends ResourceController {
    protected $format = 'json';
    protected $progressModel;
    protected $categoryModel;

    public function __construct()
    {
        $this->progressModel = new ProgressModel();
        $this->categoryModel = new CategoryModel();
    }

    //student's score
    //GET api/progress
    public function index() {
        $user = JWTAuth::$currentUser;
        if(($user->rol ?? '') !== 'student') {
            return $this->failForbidden('Only students have a progress');
        }

        $rows = $this->progressModel->getAll($user);

        $data = array_map(static fn ($r) => [
            'category_id' => (int) $r['category_id'],
            'passed' => (int) $r['passed'],
            'score' => $r['score'] === null ? null : (int) $r['score'],
        ], $rows);

        return $this->respond(['status' => 200, 'data' => $data]);
    }

    //keep performance of a test
    //POST api/progress
    public function save() {
        $user =JWTAuth::$currentUser;
        if(($user->rol ?? '') !== 'student') {
            return $this->failForbidden('Only students have progress saved');
        }

        $json = $this->request->getJSON(true) ?? [];
        $categoryId = (int) ($json['category_id'] ?? 0);
        $passed = !empty($json['passed']) ? 1 : 0;
        $score = isset($json['score']) ? (int) $json['score'] : null;

        $categories = $this->categoryModel;
        $category = $categories->where('id', $categoryId)->where('status', 1)->first();
        if (!$category) {
            return $this->failNotFound("That category doesn't exist");
        }

        $model = $this->progressModel;
        $existing = $model->checkExists($user, $categoryId);

        if ($existing) {
            //a passed category won't be marked as failed
            $model->update($existing['id'], [
                'passed' => max((int) $existing['passed'], $passed),
                'score' => $score ?? $existing['score'],
            ]);
        } else {
            $model->insert([
                'student_id' => $user->id,
                'category_id' => $categoryId,
                'passed' => $passed,
                'score' => $score,
            ]);
        }

        //if the student pass the test, the next published category will be unlocked
        $next = null;
        if ($passed) {
            $next = $categories->next($category);
        }

        return $this->respond([
            'status' => 200,
            'message' => 'progress saved succesfully',
            'next_category_id' => $next ? (int) $next['id'] : null,
        ]);
    }
}