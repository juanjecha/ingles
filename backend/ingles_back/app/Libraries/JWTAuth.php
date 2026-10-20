<?php
 
namespace App\Libraries;
 
use Firebase\JWT\JWT;
use Firebase\JWT\Key;
use Exception;
 
class JWTAuth
{
    private static function getSecretKey(): string
    {
        return env('JWT_SECRET_KEY', 'default_secret_key');
    }
 
    public static function generateToken(array $data): string
    {
        $issuedAt = time();
        $ttl = (int) env('JWT_TIME_TO_LIVE', 7200);
        $payload = [
            'iss'  => 'flashcards-api',
            'iat'  => $issuedAt,
            'exp'  => $issuedAt + $ttl,
            'data' => $data // Contiene: id, email, rol (estudiante/docente/admin)
        ];
 
        return JWT::encode($payload, self::getSecretKey(), 'HS256');
    }
 
    public static function validateToken(string $token)
    {
        try {
            $decoded = JWT::decode($token, new Key(self::getSecretKey(), 'HS256'));
            return $decoded->data;
        } catch (Exception $e) {
            return false;
        }
    }
}