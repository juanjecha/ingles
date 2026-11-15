<?php

namespace App\Commands;

use App\Models\TeacherModel;
use CodeIgniter\CLI\BaseCommand;
use CodeIgniter\CLI\CLI;

class TeacherCreate extends BaseCommand {
    protected $group = "Ingles";
    protected $name = "teacher:create";
    protected $description = "It creates a teacher account. Only the system administrator can run it";
    protected $usage = "teacher:create [teacher_key] [full_name]";
    protected $arguments = [
        'teacher_key' => 'Institutional teacher key',
        'full_name' => "Teacher's full name",
    ];

    public function run(array $params) {
        $key = strtoupper(trim($params[0] ?? CLI::prompt('Teacher key', null, 'required')));
        $name = trim($params[1] ?? CLI::prompt('Full name', null, 'required'));
        if (!preg_match('/^[A-Z0-9_-]{8,10}$/', $key)) {
            CLI::error('They key must have 10 characters at the most');
            return EXIT_ERROR;
        }

        $model = new TeacherModel();
        if ($model->where('teacher_key', $key)->first()) {
            CLI::error("A techer with key {$key} already exists");
            return EXIT_ERROR;
        }

        $password = CLI::prompt('Password (leave empty go generate one automatically)');
        $generated = false;
        if ($password === '') {
            $password = bin2hex(random_bytes(6));
            $generated = true;
        } elseif (strlen($password) < 8) {
            CLI::error('The password must have at least 8 characters');
            return EXIT_ERROR;
        }

        $email = strtolower(CLI::prompt('Institutional email', null, 'required|valid_email'));
        if ($model->where('email',$email)->first()) {
            CLI::error ("A teacher with email {email} already exists");
            return EXIT_ERROR;
        }

        $model->insert([
            'teacher_key' => $key,
            'name' => $name,
            'email' => $email,
            'password' => $password,
            'hash' => password_hash($password, PASSWORD_DEFAULT),
        ]);

        CLI::write("Teacher created: {$key} - {$name}", "green");
        if($generated) {
            CLI::write("Generated password: {$password}", 'yellow');
        }
        return EXIT_SUCCESS;
    }
}