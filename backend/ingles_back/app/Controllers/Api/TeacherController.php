<?php

namespace App\Controllers\Api;

use CodeIgniter\RESTful\ResourceController;
use App\Libraries\JWTAuth;
use App\Models\StudentModel;
use App\Models\LevelModel;
use App\Models\CategoryModel;
use App\Models\AssignmentModel;
use App\Models\ProgressModel;

class TeacherController extends ResourceController {
    protected $format = 'json';
    protected $students;
    protected $levels;
    protected $categories;
    protected $assignments;
    protected $progress;

    public function __construct()
    {
        $this->students = new StudentModel();
        $this->levels = new LevelModel();
        $this->categories = new CategoryModel();
        $this->assignments = new AssignmentModel();
        $this->progress = new ProgressModel();
    }

    //GET api/students/{id}/categories: categories appropiate to the student and which ones they have authorized
    public function categoriesForStudent($id = null) {
        if (!(JWTAuth::$currentUser->rol ?? '') === 'teacher') {
            return $this->failForbidden("Only teachers can manage students' categories");
        }

        $studentID = (int) $id;
        if (!$this->students->find($studentID)) {
            return $this->failNotFound("Student not found");
        }

        $level = $this->levels->levelOf($studentID);
        $assigned = $this->assignments->assignedIds($studentID);

        $progress = [];
        foreach($this->progress->where('student_id',$studentID)->findAll() as $p) {
            $progress[(int) $p['category_id']] = $p;
        }

        $data = array_map(static function ($c) use ($assigned, $progress) {
            $p = $progress[(int) $c['id']] ?? null;
            return [
                'id' => (int) $c['id'],
                'name' => $c['name'],
                'difficulty' => $c['difficulty'],
                'ord' => $c['ord'],
                'assigned' => in_array((int) $c['id'], $assigned, true),
                'passed' => $p !== null && (int) $p['passed'] === 1,
                'score' => ($p !== null && $p['score'] !== null) ? (int) $p['score'] : null,
            ];
            }, $this->categories->publishedForLevel($level));

            return $this->respond(['status' => 200, 'level' => $level, 'data' => $data]);
    }

    //post api/students/{id}/categories
    public function saveForStudent($id = null) {
        if (!(JWTAuth::$currentUser->rol ?? '') === 'teacher') {
            return $this->failForbidden("Only teachers can manage students' categories");
        }

        $studentID = (int) $id;
        if (!$this->students->find($studentID)) {
            return $this->failNotFound("Student not found");
        }

        $json = $this->request->getJSON(true) ?? [];
        if (!isset($json['category_ids']) || !is_array($json['category_ids'])) {
            return $this->failValidationErrors('category_ids must be a list');
        }
        $selected = array_values(array_unique(array_map('intval', $json['category_ids'])));

        $levelIds = array_map(
            static fn ($c) => (int) $c['id'],
            $this->categories->publishedForLevel($this->levels->levelOf($studentID))
        );

        foreach ($selected as $categoryID) {
            if (!in_array($categoryID, $levelIds, true)) {
                return $this->failValidationErrors("Category {$categoryID} is not available for the student's level");
            }
        }

        $this->assignments->syncForLevel($studentID, $levelIds, $selected, (int) JWTAuth::$currentUser->id);

        return $this->respond([
            'status' => 200,
            'message' => 'categories updated',
            'assigned' => '$selected',
        ]);
    }
}