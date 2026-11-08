<?php
namespace App\Controllers\Api;

use App\Libraries\JWTAuth;
use CodeIgniter\RESTful\ResourceController;
use Firebase\JWT\JWT;
use Firebase\JWT\Key;
use App\Models\MaterialModel;
use App\Models\CategoryModel;
use CodeIgniter\Controller;
use Config\Validation;
use Exception;

class MaterialController extends ResourceController {
    protected $format = 'json';
    protected $MaterialModel;
    protected $CategoryModel;

    public function __construct()
    {
        $this->MaterialModel = new MaterialModel();
        $this->CategoryModel = new CategoryModel();
    }

    public function categories() {
        return $this->respond($this->CategoryModel->list());
    }

    public function new_content() {
        //validar credenciales de autenticación
        $user = JWTAuth::$currentUser;
        if (($user->rol ?? '') !== 'teacher') {
            return $this->failForbidden('Only teachers can upload new words');
        }

        //validar datos de entrada de react por formulario
        $validation = \Config\Services::validation();
        $rules = [
            'word' => 'required|string|max_length[150]',
            'translation' => 'required|string|max_length[150]',
            'category' => 'required|is_natural_no_zero|is_not_unique[category.id]',
            //'level' => 'required|is_natural_no_zero',
            'image' => 'uploaded[image]|is_image[image]|mime_in[image,image/jpg,image/jpeg,image/png,image/webp]|max_size[image,2048]',
        ];

        if (! $this->validate($rules)) {
            return $this->failValidationErrors($this->validator->getErrors());
        }

        //procesar almacenamiento de imagen
        $file = $this->request->getFile('image');
        if (!$file->isValid() || $file->hasMoved()) {
            return $this->fail('Your image is not valid');
        }

        $newName = $file->getRandomName();
        $file->move(FCPATH . 'uploads', $newName);

        $id = $this->MaterialModel->save_material(
            (int) $this->request->getPost('category'),
            trim((string) $this->request->getPost('word')),
            trim((string) $this->request->getPost('translation')),
            $newName,
            //(int) $this->request->getPost('level')
        );

        if (!$id) {
            return $this->fail('An error has happend');
        }

        return $this->respondCreated([
                'status' => 201,
                'message' => 'Content saved succesfully',
                'id' => $id,
                'img' => $newName,
            ]); 
    }

    public function resources() {
        return $this->respond($this->MaterialModel->resources_list());
    }

    public function render_flashcards() {
        $model = new MaterialModel();
        $category = $this->request->getVar('category');
        $level = $this->request->getVar('level');
        $data = [
            'category' => $category,
            'level' => $level
        ];
        $card = $model->flashcard();
        return $this->respond($card);
    }
}