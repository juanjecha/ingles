<?php

use CodeIgniter\Router\RouteCollection;

/** @var RouteCollection $routes */
$routes->get('/', 'Home::index');

$routes->group('api', ['namespace' => 'App\Controllers\Api'], static function ($routes) {
    // Públicas
        //login
    $routes->post('login/teacher', 'Auth::login_teacher');
    $routes->post('login/student', 'StudentController::login_student');

        //register
    $routes->post('register/teacher', 'Auth::register_teacher');
    $routes->post('register/student', 'Auth::register_student');
    
        // Protegidas con JWT
    $routes->get('me', 'Auth::me', ['filter' => 'jwt']);
    $routes->post('students', 'StudentController::save_student', ['filter' => 'jwt']);
    
        // Material
    $routes->post('newMaterial', 'MaterialController::new_content');

});

// Necesaria para que CORS responda a las peticiones "preflight" del navegador
$routes->options('api/(:any)', static function () {
});

//flashcard
$routes->get('api/flashcards', 'MaterialController::render_flashcards');