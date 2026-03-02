import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import './EstadisticasCurso.css';

interface EstadisticaAlumno {
  nombre: string;
  puntos: number;
  actividadesRealizadas: number;
}

export default function EstadisticasCurso() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [estadisticas, setEstadisticas] = useState<EstadisticaAlumno[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    cargarEstadisticas();
  }, [id]);

  const cargarEstadisticas = async () => {
    setLoading(true);
    setError('');
    try {
      const token = localStorage.getItem('token');
      const apiBase = (import.meta.env.VITE_API_URL ?? "").trim().replace(/\/$/, "");

      const [puntosRes, actividadesRes] = await Promise.all([
        fetch(`${apiBase}/api/estadisticas/cursos/${id}/puntos`, {
          headers: { 'Authorization': `Bearer ${token}` },
        }),
        fetch(`${apiBase}/api/estadisticas/cursos/${id}/actividades-completadas`, {
          headers: { 'Authorization': `Bearer ${token}` },
        })
      ]);

      if (!puntosRes.ok || !actividadesRes.ok) throw new Error('Error al cargar datos');

      const puntosData = await puntosRes.json();
      const actividadesData = await actividadesRes.json();

      const alumnosMap = new Map<string, EstadisticaAlumno>();

      // Procesar puntos
      Object.entries(puntosData).forEach(([keyStr, puntos]) => {
        let nombre = keyStr;
        try {
          const obj = JSON.parse(keyStr);
          nombre = obj.nombre || obj.nombreUsuario || keyStr;
        } catch { /* es string */ }
        alumnosMap.set(nombre, { nombre, puntos: puntos as number, actividadesRealizadas: 0 });
      });

      // Procesar actividades
      Object.entries(actividadesData).forEach(([nombre, acts]) => {
        if (alumnosMap.has(nombre)) {
          alumnosMap.get(nombre)!.actividadesRealizadas = acts as number;
        } else {
          alumnosMap.set(nombre, { nombre, puntos: 0, actividadesRealizadas: acts as number });
        }
      });

      setEstadisticas(Array.from(alumnosMap.values()));
    } catch (err) {
      setError('Error al cargar las estadísticas');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="estadisticas-page">
      <NavbarMisCursos />
      
      <main className="estadisticas-main">
        <button className="btn-volver-pixel" onClick={() => navigate(-1)}>
          ← Volver a información del curso
        </button>

        <h1 className="estadisticas-titulo-curso">Estadísticas del Curso</h1>

        <div className="estadisticas-yellow-card">
          {loading ? (
            <div className="stats-info-msg">Cargando...</div>
          ) : error ? (
            <div className="stats-error-msg">{error}</div>
          ) : (
            <div className="table-scroll-container">
              <table className="pixel-table">
                <thead>
                  <tr>
                    <th>Alumno</th>
                    <th>Puntos</th>
                    <th>Nº actividades</th>
                  </tr>
                </thead>
                <tbody>
                  {estadisticas.map((stat, index) => (
                    <tr key={index}>
                      <td>{stat.nombre}</td>
                      <td className="text-center">{stat.puntos}</td>
                      <td className="text-center">{stat.actividadesRealizadas}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        <button className="btn-medias-pixel" onClick={cargarEstadisticas}>
          Actualizar ↻
        </button>
      </main>
    </div>
  );
}