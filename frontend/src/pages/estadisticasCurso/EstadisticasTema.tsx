import React, { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import './EstadisticasCurso.css';

interface TiempoAlumno {
  nombreAlumno: string;
  alumnoId: number;
  tiempoMinutos: number;
}

interface RapidosLentosDTO {
  masRapidos: TiempoAlumno[];
  masLentos: TiempoAlumno[];
  promedio: number;
}

function formatearTiempo(minutos: number): string {
  if (!minutos || minutos <= 0) return '0 mins';
  if (minutos === 1) return '1 min';
  return `${Math.round(minutos)} mins`;
}

interface EstadisticasTemaProps {
  readonly temaIdProp?: string;
  readonly embedded?: boolean;
}

export default function EstadisticasTema({ temaIdProp, embedded }: EstadisticasTemaProps = {}) {
  const params = useParams<{ id: string }>();
  const id = temaIdProp ?? params.id;
  const navigate = useNavigate();
  const [datos, setDatos] = useState<RapidosLentosDTO | null>(null);
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
      
      const res = await fetch(`${apiBase}/api/estadisticas/temas/${id}/alumnos-rapidos-lentos?limite=1000`, {
        headers: { 'Authorization': `Bearer ${token}` },
      });

      if (!res.ok) throw new Error('Error al cargar las estadísticas');

      const data = await res.json();
      setDatos(data);
    } catch (err) {
      setError((err as Error).message);
    } finally {
      setLoading(false);
    }
  };

  const promedioCalculado = useMemo(() => {
    if (!datos || !datos.masRapidos || datos.masRapidos.length === 0) return 0;
        const sumaTotal = datos.masRapidos.reduce((acc, alumno) => acc + alumno.tiempoMinutos, 0);
        return sumaTotal / datos.masRapidos.length;
  }, [datos]);

  const alumnosCompletos = datos?.masRapidos || [];
  const elMasRapido = alumnosCompletos.length > 0 ? alumnosCompletos[0] : null;
  const elMasLento = alumnosCompletos.length > 0 ? alumnosCompletos[alumnosCompletos.length - 1] : null;

  return (
    <div className={embedded ? 'stats-sub-embedded' : 'estadisticas-page'}>
      {!embedded && <NavbarMisCursos />}
      <main className="estadisticas-main">
        {!embedded && (
          <>
            <button className="btn-volver-pixel" onClick={() => navigate(-1)}>← Volver al curso</button>
            <h1 className="estadisticas-titulo-curso">Estadísticas del Tema</h1>
          </>
        )}

        <div className="estadisticas-yellow-card">
          {loading && <div className="stats-info-msg">Cargando tiempos...</div>}
          {error && <div className="stats-error-msg">{error}</div>}
          
          {!loading && !error && datos && (
            <>
              <div className="estadisticas-tiempo-resumen" style={{ display: 'flex', justifyContent: 'space-around', marginBottom: '20px', padding: '15px', backgroundColor: 'rgba(255,255,255,0.5)', borderRadius: '8px' }}>
                <div className="text-center">
                  <strong>Tiempo medio</strong><br/>
                  {formatearTiempo(promedioCalculado)}
                </div>
                {elMasRapido && (
                  <div className="text-center">
                    <strong>Alumno más rápido</strong><br/>
                    {elMasRapido.nombreAlumno} ({formatearTiempo(elMasRapido.tiempoMinutos)})
                  </div>
                )}
                {elMasLento && (
                  <div className="text-center">
                    <strong>Alumno más lento</strong><br/>
                    {elMasLento.nombreAlumno} ({formatearTiempo(elMasLento.tiempoMinutos)})
                  </div>
                )}
              </div>

              <div className="table-scroll-container">
                <table className="pixel-table">
                  <thead>
                    <tr>
                      <th>Posición</th>
                      <th>Alumno</th>
                      <th>Tiempo Invertido</th>
                    </tr>
                  </thead>
                  <tbody>
                    {alumnosCompletos.map((alumno, index) => (
                      <tr key={alumno.alumnoId}>
                        <td className="text-center">{index + 1}</td>
                        <td>{alumno.nombreAlumno}</td>
                        <td className="text-center">{formatearTiempo(alumno.tiempoMinutos)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </>
          )}
        </div>
      </main>
    </div>
  );
}