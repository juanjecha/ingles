<?php
 
namespace App\Filters;
 
use CodeIgniter\Filters\FilterInterface;
use CodeIgniter\HTTP\RequestInterface;
use CodeIgniter\HTTP\ResponseInterface;
use App\Libraries\JWTAuth;
use Firebase\JWT\JWT;
use Firebase\JWT\Key;
use Config\Services;
 
class JWTFilter implements FilterInterface
{
    public function before(RequestInterface $request, $arguments = null)
    {
        $header = $request->getServer('HTTP_AUTHORIZATION');
 
        if (!$header) {
            return Services::response()
                ->setJSON(['status' => 401, 'error' => 'Token no proporcionado'])
                ->setStatusCode(401);
        }
 
        $token = str_replace('Bearer ', '', $header);
        $decodedData = JWTAuth::validateToken($token);
 
        if (!$decodedData) {
            return Services::response()
                ->setJSON(['status' => 401, 'error' => 'Token inválido o expirado'])
                ->setStatusCode(401);
        }
 
        // Se adjuntan los datos cargados en el token a la petición
        $request->userData = $decodedData;
    }
 
    public function after(RequestInterface $request, ResponseInterface $response, $arguments = null)
    {
        // No requiere acción posterior
    }
}