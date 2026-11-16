<?php
namespace App\Models;

use CodeIgniter\Model;

class AssignmentModel extends Model {
    protected $table = "student_category";
    
    protected $primaryKey = 'id';
    protected $allowedFields = ["student_id", "category_id", "assigned_by"];
    protected $returnType = 'array';


    //get categories authorized by the te teacher for the student
    public function assignedIds(int $studentID): array {
        $rows = $this->select('category_id')->where('student_id', $studentID)->findAll();
        return array_map(static fn ($r) => (int) $r['category_id'], $rows);
    }

    public function isAssigned(int $studentID, int $categoryID): bool {
        return $this->where('student_id', $studentID)
                    ->where('category_id', $categoryID)
                    ->countAllResults() > 0;
    }

    //This function is to put the assignments of students in the variable $selected,
    //keeping in mind that only for the level of the student
    public function syncForLevel(int $studentId, array $levelCategoryIds, array $selected, int $teacherId) : void {
        $current = array_values(array_intersect($this->assignedIds($studentId), $levelCategoryIds));
        $toRemove = array_values(array_diff($current, $selected));
        $toAdd = array_values(array_diff($selected, $current));

        $this->db->transBegin();
        try {
            if ($toRemove) {
                $this->where('student_id', $studentId)->whereIn('category_id', $toRemove)->delete();
            }
            foreach ($toAdd as $categoryID) {
                $this->insert([
                    'student_id' => $studentId,
                    'category_id' => $categoryID,
                    'assigned_by' => $teacherId,
                ]);
                $this->db->transCommit();
            } 
        } catch (\Throwable $e) {
            $this->db->transRollback();
            throw $e;
        }
    }
}