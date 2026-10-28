<?php
namespace App\Controllers;

use CodeIgniter\RESTful\ResourceController;
use Firebase\JWT\JWT;
use Firebase\JWT\Key;
use App\Models\MaterialModel;
use CodeIgniter\Controller;
use Config\Validation;
use Exception;

class MaterialController extends ResourceController {
    public function new_content() {
        //validar credenciales de autenticación
        $autHeader = $this->request->getServer('HTTP_AUTHORIZATION');
        if (!$autHeader) {
            return $this->failUnauthorized('Token no autorizado');
        }
        try {
            $token = str_replace('Bearer', '', $autHeader);
            $key = getenv('JWT_SECRET_KEY')?: 'clave_secreta';
            $decoded = JWT::decode($token, new Key($key,'HS256'));
            $idStudent = $decoded->sub;
        } catch(Exception $e) {
            return $this->failUnauthorized('Token inválido o expirado');
        }

        //validar datos de entrada de react por formulario
        $validation = \Config\Services::validation();
        $validation->setRules([
            'word' => 'required|string',
            'img' => 'uploaded[image]|is_image[image]|max_size[image,100]',
            'category' => 'required|integer'
        ]);
        if (!$validation->withRequest($this->request)->run()) {
            return $this->failValidationErrors($validation->getErrors());
        }

        //procesar almacenamiento de imagen
        $file = $this->request->getFile('image');
        if ($file->isValid() && !$file->hasMoved()) {
            //$newName = $file->getRandomName();
            $word = $this->request->getVar('word');
            $category = $this->request->getVar('category');
            $file->move(FCPATH, 'uploads', $word);
            $model = new MaterialModel();
            $res = $model->save_material($category, $word, $word);
            if ($res){
                return $this->respondCreated([
                    'status' => 201,
                    'message' => 'información almacenada de manera existosa'
                ]); 
            } else {
                return $this->fail('Error al cargar la información');
            }
        }
    }
    
}