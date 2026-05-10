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

interface AlumnoInscritoCursoDTO {
  alumnoId: number;
  nombre: string;
}

function formatearTiempo(minutos: number): string {
  if (!minutos || minutos <= 0) return '0 mins';
  if (minutos === 1) return '1 min';
  return `${Math.round(minutos)} mins`;
}

interface EstadisticasTemaProps {
  readonly temaIdProp?: string;
  readonly cursoIdProp?: string;
  readonly embedded?: boolean;
}

export default function EstadisticasTema({ temaIdProp, cursoIdProp, embedded }: EstadisticasTemaProps = {}) {
  const params = useParams<{ id: string }>();
  const id = temaIdProp ?? params.id;
  const navigate = useNavigate();
  const [datos, setDatos] = useState<RapidosLentosDTO | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    void cargarEstadisticas();
  }, [id, cursoIdProp]);

  type FetchHeaders = Record<string, string>;

  async function fetchJson<T>(url: string, headers: FetchHeaders): Promise<T> {
    const res = await fetch(url, { headers });
    if (!res.ok) throw new Error(`Request failed: ${res.status}`);
    return (await res.json()) as T;
  }

  async function fetchOptionalJson<T>(url: string, headers: FetchHeaders): Promise<T | null> {
    try {
      const res = await fetch(url, { headers });
      if (!res.ok) return null;
      return (await res.json()) as T;
    } catch {
      return null;
    }
  }

  function filtrarPorIds(data: RapidosLentosDTO, ids: Set<number> | null): RapidosLentosDTO {
    if (!ids) return data;
    return {
      ...data,
      masRapidos: (data.masRapidos ?? []).filter(a => ids.has(a.alumnoId)),
      masLentos: (data.masLentos ?? []).filter(a => ids.has(a.alumnoId)),
    };
  }

  async function resolverCursoIdTema(
    apiBase: string,
    temaId: string,
    headers: FetchHeaders,
    cursoIdProp?: string
  ): Promise<string | undefined> {
    if (cursoIdProp) return cursoIdProp;

    // TemaDTO tiene cursoId
    const tema = await fetchOptionalJson<{ cursoId?: number | string }>(
      `${apiBase}/api/temas/${temaId}`,
      headers
    );

    return tema?.cursoId != null ? String(tema.cursoId) : undefined;
  }

  async function cargarIdsInscritos(
    apiBase: string,
    cursoId: string | undefined,
    headers: FetchHeaders
  ): Promise<Set<number> | null> {
    if (!cursoId) return null;

    const alumnos = await fetchOptionalJson<AlumnoInscritoCursoDTO[]>(
      `${apiBase}/api/inscripciones/curso/${cursoId}/alumnos`,
      headers
    );

    if (!alumnos || alumnos.length === 0) return null;
    return new Set(alumnos.map(a => a.alumnoId));
  }

  // dentro del componente:
  const cargarEstadisticas = async () => {
    setLoading(true);
    setError('');

    try {
      const token = localStorage.getItem('token');
      const apiBase = (import.meta.env.VITE_API_URL ?? '').trim().replace(/\/$/, '');
      const headers = { Authorization: `Bearer ${token}` };

      const cursoIdEfectivo = await resolverCursoIdTema(apiBase, id!, headers, cursoIdProp);

      const [stats, idsInscritos] = await Promise.all([
        fetchJson<RapidosLentosDTO>(`${apiBase}/api/estadisticas/temas/${id}/alumnos-rapidos-lentos?limite=1000`, headers),
        cargarIdsInscritos(apiBase, cursoIdEfectivo, headers),
      ]);

      setDatos(filtrarPorIds(stats, idsInscritos));
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error desconocido');
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
            <button className="btn-volver-pixel" onClick={() => navigate(-1)}>Volver al curso</button>
            <h1 className="estadisticas-titulo-curso">Estadísticas del Tema</h1>
          </>
        )}

        <div className="estadisticas-yellow-card">
          {loading && <div className="stats-info-msg">Cargando tiempos...</div>}
          {error && <div className="stats-error-msg">{error}</div>}
          
          {!loading && !error && datos && (
            <>
              <div className="stat-cards-row">
                <div className="stat-card stat-card--tiempo">
                  <span className="stat-card__label">Tiempo medio</span>
                  <span className="stat-card__value">{formatearTiempo(promedioCalculado)}</span>
                </div>
                {elMasRapido && (
                  <div className="stat-card stat-card--rapido">
                    <span className="stat-card__label">Alumno más rápido</span>
                    <span className="stat-card__value">{formatearTiempo(elMasRapido.tiempoMinutos)}</span>
                    <span className="stat-card__name">{elMasRapido.nombreAlumno}</span>
                  </div>
                )}
                {elMasLento && (
                  <div className="stat-card stat-card--lento">
                    <span className="stat-card__label">Alumno más lento</span>
                    <span className="stat-card__value">{formatearTiempo(elMasLento.tiempoMinutos)}</span>
                    <span className="stat-card__name">{elMasLento.nombreAlumno}</span>
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
                        <td className="text-center">
                          <span className={`rank-badge${index < 3 ? ` rank-badge--${index + 1}` : ''}`}>{index + 1}</span>
                        </td>
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