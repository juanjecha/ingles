import react, { useEffect, useState } from "react";
import { Form } from "react-router-dom";
export default function asignarNivel() {
    const [alumnos, setAlumnos] = useState([]);
    const [error, setError] = useState([]);
    const [cargando, setCargando] = useState([]);

    useEffect(()=> {
        const obtenerAlumnos = async()=> {
            const token = localStorage.getItem("token_jwt");
            if (!token) {
                setError("No estás autenticado como dicente, inicie sesión");
                setCargando(false);
                return;
            }
            
            try{
                const response = await fetch ("servidor local CI4");
                method: "GET";
                headers: {
                    "Content-Type"; "application/json",
                    "Authorization"; "Bearer $(token)"
                }
            }
        };
    });
    <div>
        <h2>Asignar niveles</h2>
        <Form>

            <label>Alumno</label>
            <select>
                <option>Seleccionar nombre de alumno</option>
            </select>

            <label>Nivel</label>
            <select>
                <option>Selecciona el nivel del alumno</option>
                <option>A1</option>
                <option>A2</option>
                <option>B1</option>
                <option>B2</option>
            </select>

            <input type="submit">Aceptar</input>
        </Form>
    </div>
}