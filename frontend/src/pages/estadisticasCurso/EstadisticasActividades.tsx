import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import './EstadisticasCurso.css';

interface Actividad {
  id: number;
  titulo: string;
}

interface Tema {
  id: number;
  titulo: string;
  actividades: Actividad[];
}

interface EstadisticasActividadDTO {
  notaMediaActividad: number | null;
  notaMaximaActividad: number | null;
  notaMinimaActividad: number | null;
  tiempoMedioActividad: number | null;
  actividadCompletadaPorTodos: boolean | null;
}

interface RepeticionesActividadDTO {
  repeticionesMedia: number | null;
  repeticionesMinima: number | null;
  repeticionesMaxima: number | null;
}

export default function EstadisticasActividades() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  
  const [temas, setTemas] = useState<Tema[]>([]);
  const [temaSeleccionado, setTemaSeleccionado] = useState<Tema | null>(null);
  const [mapaEstadisticas, setMapaEstadisticas] = useState<Map<number, EstadisticasActividadDTO>>(new Map());
  const [mapaRepeticiones, setMapaRepeticiones] = useState<Map<number, RepeticionesActividadDTO>>(new Map());
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    cargarTodo();
  }, [id]);

  const cargarEstadisticasTema = async (temaId: number) => {
    const token = localStorage.getItem('token');
    const apiBase = (import.meta.env.VITE_API_URL ?? "").trim().replace(/\/$/, "");

    const [resStats, resReps] = await Promise.all([
      fetch(`${apiBase}/api/estadisticas/cursos/${id}/temas/${temaId}/estadisticas-actividades`, {
        headers: { 'Authorization': `Bearer ${token}` }
      }),
      fetch(`${apiBase}/api/estadisticas/cursos/${id}/temas/${temaId}/repeticiones-actividades`, {
        headers: { 'Authorization': `Bearer ${token}` }
      }),
    ]);

    if (!resStats.ok) throw new Error('Error al obtener estadísticas de actividades');
    if (!resReps.ok) throw new Error('Error al obtener repeticiones de actividades');

    const data = await resStats.json();
    const nuevoMapa = new Map<number, EstadisticasActividadDTO>();
    Object.entries(data as Record<string, EstadisticasActividadDTO>).forEach(([actividadId, stats]) => {
      nuevoMapa.set(Number(actividadId), stats);
    });
    setMapaEstadisticas(nuevoMapa);

    const repsData = await resReps.json();
    const nuevoMapaReps = new Map<number, RepeticionesActividadDTO>();
    Object.entries(repsData as Record<string, RepeticionesActividadDTO>).forEach(([actividadId, reps]) => {
      nuevoMapaReps.set(Number(actividadId), reps);
    });
    setMapaRepeticiones(nuevoMapaReps);
  };

  const seleccionarTema = async (tema: Tema) => {
    setTemaSeleccionado(tema);
    setError('');
    try {
      await cargarEstadisticasTema(tema.id);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error de conexión');
    }
  };

  const notaMediaMostrada = (stats: EstadisticasActividadDTO | undefined) => {
    const valor = stats?.notaMediaActividad;
    if (typeof valor !== 'number' || !Number.isFinite(valor)) return 0;
    return Math.round(valor * 100) / 100;
  };

  const repeticionesMediaMostrada = (reps: RepeticionesActividadDTO | undefined) => {
    const valor = reps?.repeticionesMedia;
    if (typeof valor !== 'number' || !Number.isFinite(valor)) return 0;
    return Math.round(valor * 100) / 100;
  };

  const formatearTiempoMinutos = (minutos: number | null | undefined): string => {
    if (typeof minutos !== 'number' || !Number.isFinite(minutos) || minutos <= 0) return '0 mins';
    const redondeado = Math.round(minutos);
    if (redondeado === 1) return '1 min';
    return `${redondeado} mins`;
  };

  const terminadaPorTodosMostrada = (stats: EstadisticasActividadDTO | undefined) => {
    if (stats?.actividadCompletadaPorTodos === null || stats?.actividadCompletadaPorTodos === undefined) return 'N/A';
    return stats.actividadCompletadaPorTodos ? 'Sí' : 'No';
  }

  const cargarTodo = async () => {
    setLoading(true);
    setError('');
    try {
      const token = localStorage.getItem('token');
      const apiBase = (import.meta.env.VITE_API_URL ?? "").trim().replace(/\/$/, "");

      const resEstructura = await fetch(`${apiBase}/api/temas/curso/${id}/maestro`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });

      if (!resEstructura.ok) throw new Error('Error al obtener la estructura del curso');

      const temasData: Tema[] = await resEstructura.json();
      setTemas(temasData);

      if (temasData.length > 0) {
        const temaIdPreferido = temaSeleccionado?.id;
        const temaInicial = temasData.find(t => t.id === temaIdPreferido) ?? temasData[0];
        setTemaSeleccionado(temaInicial);
        await cargarEstadisticasTema(temaInicial.id);
      } else {
        setTemaSeleccionado(null);
        setMapaEstadisticas(new Map());
        setMapaRepeticiones(new Map());
      }

    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error de conexión');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="estadisticas-page">
      <NavbarMisCursos />
      <main className="estadisticas-main">
        <button className="btn-volver-pixel" onClick={() => navigate(-1)}>← Volver</button>
        <h1 className="estadisticas-titulo-curso">Actividades del Curso</h1>

        {loading && <p className="msg-placeholder">Cargando datos...</p>}
        {error && <p className="msg-placeholder" style={{ color: 'red' }}>{error}</p>}

        <div className="layout-estadisticas">
          {/* PANEL IZQUIERDO: TEMAS */}
          <aside className="panel-temas">
            <h3>Temas</h3>
            {temas.length === 0 ? (
              <p className="msg-vacio">Este curso aun no tiene temas</p>
            ) : (
              <ul className="lista-temas-scroll">
                {temas.map((tema) => (
                  <li
                    key={tema.id}
                    className={`btn-medias-pixel ${temaSeleccionado?.id === tema.id ? 'active' : ''}`}
                    onClick={() => seleccionarTema(tema)}
                    onKeyDown={(e) => { if (e.key === 'Enter' || e.key === ' ') seleccionarTema(tema); }}
                    role="button"
                    tabIndex={0}
                  >
                    {tema.titulo}
                  </li>
                ))}
              </ul>
            )}
          </aside>

          {/* PANEL DERECHO: ACTIVIDADES */}
          <section className="panel-actividades">
            {temaSeleccionado ? (
              <>
                <h3>{temaSeleccionado.titulo}</h3>
                <div className="table-scroll-container">
                  <table className="pixel-table">
                    <thead>
                      <tr>
                        <th>Nº</th>
                        <th>Actividad</th>
                        <th>Nota Media</th>
                        <th>Nota Máx.</th>
                        <th>Nota Mín.</th>
                        <th>Tiempo Medio</th>
                        <th>Reps Media</th>
                        <th>Reps Mín.</th>
                        <th>Reps Máx.</th>
                        <th>Terminada por todos</th>
                      </tr>
                    </thead>
                    <tbody>
                      {temaSeleccionado.actividades.map((act, index) => (
                        <tr key={act.id}>
                          <td className="text-center">{index + 1}</td>
                          <td>{act.titulo}</td>
                          {(() => {
                            const stats = mapaEstadisticas.get(act.id);
                            const reps = mapaRepeticiones.get(act.id);
                            return (
                              <>
                                <td className="text-center font-bold">{notaMediaMostrada(stats)}</td>
                                <td className="text-center font-bold">{stats?.notaMaximaActividad ?? 0}</td>
                                <td className="text-center font-bold">{stats?.notaMinimaActividad ?? 0}</td>
                                <td className="text-center font-bold">{formatearTiempoMinutos(stats?.tiempoMedioActividad)}</td>
                                <td className="text-center font-bold">{repeticionesMediaMostrada(reps)}</td>
                                <td className="text-center font-bold">{reps?.repeticionesMinima ?? 0}</td>
                                <td className="text-center font-bold">{reps?.repeticionesMaxima ?? 0}</td>
                                <td className="text-center font-bold">{terminadaPorTodosMostrada(stats)}</td>
                              </>
                            );
                          })()}
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </>
            ) : (
              <p className="msg-placeholder">Selecciona un tema</p>
            )}
          </section>
        </div>

        <div className="botones-footer">
          <button className="btn-medias-pixel" onClick={cargarTodo}>Actualizar ↻</button>
          <button
            className="btn-medias-pixel"
            onClick={() => navigate(`/estadisticas/${id}/actividades/graficas`)}
          >
            Gráficas
          </button>
        </div>
      </main>
    </div>
  );
}