<?

namespace App\Models;

use CodeIgniter\Model;

class LevelModel extends Model {
    protected $table = "student_level";

    protected $allowedFields = ["student_id", "enrollment", "level"];
    protected $returnType = 'array';

    public const LEVELS = ['A1', 'A2', 'B1', 'B2', 'C1'];

    public static function nextLevel(string $level): ?string {
        $i = array_search($level, self::LEVELS, true);
        return ($i === false || $i >= count(self::LEVELS) -1) ? null : self::LEVELS[$i + 1]; 
    }
}