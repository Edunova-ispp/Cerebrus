import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import './EstadisticasCurso.css'; // Reutilizamos los estilos pixel art

// Función auxiliar para mostrar el tiempo
function formatearTiempo(segundosTotales: number): string {
  if (!segundosTotales || segundosTotales <= 0) return '0s';
  const minutos = Math.floor(segundosTotales / 60);
  const segundos = Math.floor(segundosTotales % 60);
  if (minutos === 0) return `${segundos}s`;
  return `${minutos}m ${segundos}s`;
}

interface TiempoActividad {
  nombreActividad: string;
  tiempo: number; // en segundos
}

export default function EstadisticasAlumno() {
  // Sacamos el id del curso y el nombre del alumno de la URL
  const { cursoId, alumnoNombre } = useParams<{ cursoId: string; alumnoNombre: string }>();
  const navigate = useNavigate();
  
  const [actividades, setActividades] = useState<TiempoActividad[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    cargarDetalleAlumno();
  }, [cursoId, alumnoNombre]);

  const cargarDetalleAlumno = async () => {
    setLoading(true);
    setError('');
    try {
      const token = localStorage.getItem('token');
      const apiBase = (import.meta.env.VITE_API_URL ?? "").trim().replace(/\/$/, "");

      // Llamada al endpoint que tu compañero de backend tiene que preparar
      const res = await fetch(`${apiBase}/api/estadisticas/cursos/${cursoId}/alumno/${alumnoNombre}/tiempos`, {
        headers: { 'Authorization': `Bearer ${token}` },
      });

      if (!res.ok) throw new Error('Error al cargar el detalle del alumno');

      const data = await res.json();
      
      // Asumimos que el backend devuelve algo como: { "Test Tema 1": 120, "Tablero de Sumas": 45, ... }
      const listaActividades = Object.entries(data).map(([nombre, tiempo]) => ({
        nombreActividad: nombre,
        tiempo: tiempo as number,
      }));

      setActividades(listaActividades);
    } catch (err) {
      setError((err as Error).message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="estadisticas-page">
      <NavbarMisCursos />
      <main className="estadisticas-main">
        <button className="btn-volver-pixel" onClick={() => navigate(-1)}>
          ← Volver
        </button>

        <h1 className="estadisticas-titulo-curso">
          Detalle de {decodeURIComponent(alumnoNombre || '')}
        </h1>

        <div className="estadisticas-yellow-card">
          {loading && <div className="stats-info-msg">Cargando actividades...</div>}
          {error && <div className="stats-error-msg">{error}</div>}
          
          {!loading && !error && actividades.length === 0 && (
            <div className="stats-info-msg">Este alumno aún no ha realizado actividades.</div>
          )}

          {!loading && !error && actividades.length > 0 && (
            <div className="table-scroll-container">
              <table className="pixel-table">
                <thead>
                  <tr>
                    <th>Actividad</th>
                    <th>Tiempo Empleado</th>
                  </tr>
                </thead>
                <tbody>
                  {actividades.map((act, index) => (
                    <tr key={index}>
                      <td>{act.nombreActividad}</td>
                      <td className="text-center">{formatearTiempo(act.tiempo)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </main>
    </div>
  );
}