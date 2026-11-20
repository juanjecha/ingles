<?php
namespace App\Models;

use CodeIgniter\Database\ConnectionInterface;
use CodeIgniter\Model;
use CodeIgniter\Validation\ValidationInterface;
use Override;

class ProgressModel extends Model {
    private LevelModel $levels;
    private CategoryModel $categories;
    private AssignmentModel $assignments;


    protected $table = "progress";

    protected $primaryKey = 'id';
    protected $allowedFields = ['student_id', 'category_id', 'passed', 'score'];
    protected $returnType = 'array';


    public function __construct()
    {
        $this->levels = new LevelModel();
        $this->categories = new CategoryModel();
        $this->assignments = new AssignmentModel();
    }

    public function getAll($user) {
        return $this->select('category_id, passed, score')
                     ->where('student_id', (int) $user->id)
                     ->findAll();
    }

    public function checkExists($user, int $categoryId) {
        return $this->where('student_id', (int) $user->id)
                    ->where('category_id', $categoryId)
                    ->first();
    }

    public function summary(int $studentID): array {
        $level = $this->levels->levelOf($studentID);
        $assigned = $this->assignments->assignedIds($studentID);

        $progress = [];
        foreach ($this->where('student_id', $studentID)->findAll() as $p) {
            $progress[(int) $p['category_id']] = $p;
        }

        $rows = [];
        $scores = [];
        $completed = 0;
        foreach ($this->categories->publishedForLevel($level) as $c) {
            $id = (int) $c['id'];
            if (!in_array($id, $assigned, true)) continue;

            $p = $progress[$id] ?? null;
            $passed = $p !== null && (int) $p['passed'] === 1;
            $score = ($p !== null && $p['score'] !== null) ? (int) $p['score'] : null;

            if($passed) {
                $completed++;
                if ($score !== null) $scores[] = $score;
            }
            $rows[] = [
                'category_id' => $id,
                'name' => $c['name'],
                'passed' => $passed,
                'score' => $score,
            ];
        }

        $total = count($rows);
        $enabled = $total > 0 && $completed === $total;
        $next = LevelModel::nextLevel($level);

        return [
            'level' => $level,
            'enabled' => $enabled,
            'completed' => $completed,
            'total' => $total,
            'average' => ($enabled && $scores) ? (int) round(array_sum($scores) / count($scores)) : null,
            'suggested_level' => $enabled ? ($next ?? $level) : null,
            'can_promote' => $enabled && $next !== null,
            'categories' => $enabled ? $rows : [],
        ];
    }
}