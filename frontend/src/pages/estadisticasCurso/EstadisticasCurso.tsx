import React, { useEffect, useMemo, useState, useCallback } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import MediasCurso from './MediasCurso';
import EstadisticasTema from './EstadisticasTema';
import EstadisticasActividad from './EstadisticasActividad';
import EstadisticasAlumno from './EstadisticasAlumno';
import './EstadisticasCurso.css';

interface EstadisticaAlumno {
  nombre: string;
  idAlumno?: number; 
  puntos: number;
  actividadesRealizadas: number;
  tiempoTotal: number;
}

interface OpcionItem {
  id: number;
  nombre: string;
}

type StatsView =
  | { mode: "resumen" }
  | { mode: "medias"; temaId?: number }
  | { mode: "tiemposTema"; temaId?: number }
  | { mode: "tiemposActividad"; actividadId?: number }
  | { mode: "alumnos" }
  | { mode: "alumnoDetalle"; alumnoId: number; alumnoNombre: string };

function formatearTiempo(minutosTotales: number): string {
  if (!minutosTotales || minutosTotales === 0) return '0 mins';
  if (minutosTotales === 1) return '1 min';
  return `${minutosTotales} mins`;
}

interface EstadisticasCursoProps {
  readonly cursoId?: string;
  readonly embedded?: boolean;
}

export default function EstadisticasCurso({ cursoId, embedded }: EstadisticasCursoProps = {}) {
  const params = useParams<{ id: string }>();
  const id = cursoId ?? params.id;
  const navigate = useNavigate();
  const [estadisticas, setEstadisticas] = useState<EstadisticaAlumno[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [statsView, setStatsView] = useState<StatsView>({ mode: "resumen" });
  const [temasList, setTemasList] = useState<OpcionItem[]>([]);
  const [actividadesList, setActividadesList] = useState<OpcionItem[]>([]);
  const [alumnosList, setAlumnosList] = useState<{ id: number; nombre: string }[]>([]);
  const [cargandoLista, setCargandoLista] = useState(false);

  useEffect(() => {
    cargarEstadisticas();
  }, [id]);

  const cargarEstadisticas = async () => {
    setLoading(true);
    setError('');
    try {
      const token = localStorage.getItem('token');
      const apiBase = (import.meta.env.VITE_API_URL ?? "").trim().replace(/\/$/, "");

      // Hacemos 3 llamadas: Puntos, Actividades y la lista completa de tiempos (con límite 1000 para que vengan todos)
      const [puntosRes, actividadesRes, tiemposRes] = await Promise.all([
        fetch(`${apiBase}/api/estadisticas/cursos/${id}/puntos`, { headers: { 'Authorization': `Bearer ${token}` } }),
        fetch(`${apiBase}/api/estadisticas/cursos/${id}/actividades-completadas`, { headers: { 'Authorization': `Bearer ${token}` } }),
        fetch(`${apiBase}/api/estadisticas/cursos/${id}/alumnos-rapidos-lentos?limite=1000`, { headers: { 'Authorization': `Bearer ${token}` } })
      ]);

      if (!puntosRes.ok || !actividadesRes.ok) throw new Error('Error al cargar datos principales');

      const puntosData = await puntosRes.json();
      const actividadesData = await actividadesRes.json();
      const tiemposData = tiemposRes.ok ? await tiemposRes.json() : null;

      const alumnosMap = new Map<string, EstadisticaAlumno>();

      // 1. Procesar puntos
      Object.entries(puntosData).forEach(([nombre, puntos]) => {
        alumnosMap.set(nombre, { nombre, puntos: puntos as number, actividadesRealizadas: 0, tiempoTotal: 0 });
      });

      // 2. Procesar actividades completadas
      Object.entries(actividadesData).forEach(([nombre, acts]) => {
        if (!alumnosMap.has(nombre)) {
          alumnosMap.set(nombre, { nombre, puntos: 0, actividadesRealizadas: 0, tiempoTotal: 0 });
        }
        alumnosMap.get(nombre)!.actividadesRealizadas = acts as number;
      });

      // 3. Procesar tiempos desde el endpoint de tu compañero
      if (tiemposData && tiemposData.masRapidos) {
        tiemposData.masRapidos.forEach((t: any) => {
          if (!alumnosMap.has(t.nombreAlumno)) {
            alumnosMap.set(t.nombreAlumno, { nombre: t.nombreAlumno, puntos: 0, actividadesRealizadas: 0, tiempoTotal: 0 });
          }
          // El backend de tu compañero nos da el tiempo exacto en minutos
          alumnosMap.get(t.nombreAlumno)!.tiempoTotal = t.tiempoMinutos || 0;
          alumnosMap.get(t.nombreAlumno)!.idAlumno = t.alumnoId;
        });
      }

      setEstadisticas(Array.from(alumnosMap.values()));

    } catch (err) {
      setError((err as Error).message || 'Error cargando las estadísticas');
    } finally {
      setLoading(false);
    }
  };

  const metricasTiempo = useMemo(() => {
    if (estadisticas.length === 0) return null;
    const alumnosConTiempo = estadisticas.filter(est => est.tiempoTotal > 0);
    if (alumnosConTiempo.length === 0) return null;

    let totalMinutos = 0;
    let masRapido = alumnosConTiempo[0];
    let masLento = alumnosConTiempo[0];

    alumnosConTiempo.forEach(est => {
      totalMinutos += est.tiempoTotal;
      if (est.tiempoTotal < masRapido.tiempoTotal) masRapido = est;
      if (est.tiempoTotal > masLento.tiempoTotal) masLento = est;
    });

    return { media: totalMinutos / alumnosConTiempo.length, masRapido, masLento };
  }, [estadisticas]);

  const cargarListaTemas = useCallback(async () => {
    setCargandoLista(true);
    try {
      const token = localStorage.getItem('token');
      const apiBase = (import.meta.env.VITE_API_URL ?? "").trim().replace(/\/$/, "");
      const res = await fetch(`${apiBase}/api/temas/curso/${id}/maestro`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (!res.ok) throw new Error('No se pudo obtener la lista de temas');
      const temasData = await res.json();
      const nuevos: OpcionItem[] = temasData.map((t: any) => {
        const temaObj = t.tema || t;
        return { id: temaObj.id, nombre: temaObj.titulo || temaObj.nombre || `Tema ${temaObj.id}` };
      });
      setTemasList(nuevos);
    } catch (err) {
      console.error(err);
    } finally {
      setCargandoLista(false);
    }
  }, [id]);

  const cargarListaActividades = useCallback(async () => {
    setCargandoLista(true);
    try {
      const token = localStorage.getItem('token');
      const apiBase = (import.meta.env.VITE_API_URL ?? "").trim().replace(/\/$/, "");
      const res = await fetch(`${apiBase}/api/temas/curso/${id}/maestro`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (!res.ok) throw new Error('No se pudo obtener la lista');
      const temasData = await res.json();
      const acts: OpcionItem[] = [];
      temasData.forEach((t: any) => {
        const listaActs = t.actividades || t.actividadesDTO || [];
        listaActs.forEach((a: any) => {
          const actObj = a.actividad || a;
          const tipoAct = (actObj.tipo || "").toUpperCase();
          const tituloAct = (actObj.titulo || actObj.nombre || "").toLowerCase();
          const esTeoria = tipoAct === 'TEORIA' || tituloAct.includes('teoría') || tituloAct.includes('teoria');
          if (!esTeoria) {
            acts.push({ id: actObj.id, nombre: actObj.titulo || actObj.nombre || `Actividad ${actObj.id}` });
          }
        });
      });
      setActividadesList(acts);
    } catch (err) {
      console.error(err);
    } finally {
      setCargandoLista(false);
    }
  }, [id]);

  const handleMedias = async () => {
    await cargarListaTemas();
    setStatsView({ mode: "medias" });
  };

  const handleTiemposTema = async () => {
    await cargarListaTemas();
    setStatsView({ mode: "tiemposTema" });
  };

  const handleTiemposActividad = async () => {
    await cargarListaActividades();
    setStatsView({ mode: "tiemposActividad" });
  };

  const handleAlumnos = useCallback(async () => {
    setCargandoLista(true);
    try {
      const token = localStorage.getItem('token');
      const apiBase = (import.meta.env.VITE_API_URL ?? '').trim().replace(/\/$/, '');
      const res = await fetch(`${apiBase}/api/estadisticas/cursos/${id}/alumnos`, {
        headers: { 'Authorization': `Bearer ${token}` },
      });
      if (!res.ok) throw new Error();
      const data = await res.json();
      setAlumnosList(data);
    } catch (err) {
      console.error(err);
    } finally {
      setCargandoLista(false);
    }
    setStatsView({ mode: "alumnos" });
  }, [id]);

  let estadisticasContent: React.ReactNode;
  if (loading) {
    estadisticasContent = <div className="stats-info-msg">Cargando...</div>;
  } else if (error) {
    estadisticasContent = <div className="stats-error-msg">{error}</div>;
  } else {
    estadisticasContent = (
      <>
        {metricasTiempo && (
          <div className="stat-cards-row">
            <div className="stat-card stat-card--tiempo">
              <span className="stat-card__label">Tiempo medio</span>
              <span className="stat-card__value">{formatearTiempo(metricasTiempo.media)}</span>
            </div>
            <div className="stat-card stat-card--rapido">
              <span className="stat-card__label">Alumno más rápido</span>
              <span className="stat-card__value">{formatearTiempo(metricasTiempo.masRapido.tiempoTotal)}</span>
              <span className="stat-card__name">{metricasTiempo.masRapido.nombre}</span>
            </div>
            <div className="stat-card stat-card--lento">
              <span className="stat-card__label">Alumno más lento</span>
              <span className="stat-card__value">{formatearTiempo(metricasTiempo.masLento.tiempoTotal)}</span>
              <span className="stat-card__name">{metricasTiempo.masLento.nombre}</span>
            </div>
          </div>
        )}

        <div className="table-scroll-container">
          <table className="pixel-table">
            <thead>
              <tr>
                <th>Alumno</th>
                <th>Puntos</th>
                <th>Nº actividades</th>
                <th>Tiempo Total</th>
              </tr>
            </thead>
            <tbody>
              {estadisticas.map((stat, index) => (
                <tr key={index}>
                  <td>{stat.nombre}</td>
                  <td className="text-center">{stat.puntos}</td>
                  <td className="text-center">{stat.actividadesRealizadas}</td>
                  <td className="text-center">{formatearTiempo(stat.tiempoTotal)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </>
    );
  }

  const renderSidebar = () => {
    if (statsView.mode === "medias") {
      return (
        <>
          <button className="stats-sidebar-btn stats-sidebar-back" onClick={() => setStatsView({ mode: "resumen" })}>
            ← Volver
          </button>
          <h3 className="stats-sidebar-title">Temas</h3>
          {cargandoLista ? (
            <p className="stats-sidebar-loading">Cargando...</p>
          ) : temasList.map(t => (
            <button
              key={t.id}
              className={`stats-sidebar-btn${'temaId' in statsView && statsView.temaId === t.id ? ' stats-sidebar-btn--active' : ''}`}
              onClick={() => setStatsView({ mode: "medias", temaId: t.id })}
            >
              {t.nombre}
            </button>
          ))}
        </>
      );
    }

    if (statsView.mode === "tiemposTema") {
      return (
        <>
          <button className="stats-sidebar-btn stats-sidebar-back" onClick={() => setStatsView({ mode: "resumen" })}>
             Volver
          </button>
          <h3 className="stats-sidebar-title">Temas</h3>
          {cargandoLista ? (
            <p className="stats-sidebar-loading">Cargando...</p>
          ) : temasList.map(t => (
            <button
              key={t.id}
              className={`stats-sidebar-btn${'temaId' in statsView && statsView.temaId === t.id ? ' stats-sidebar-btn--active' : ''}`}
              onClick={() => setStatsView({ mode: "tiemposTema", temaId: t.id })}
            >
              {t.nombre}
            </button>
          ))}
        </>
      );
    }

    if (statsView.mode === "tiemposActividad") {
      return (
        <>
          <button className="stats-sidebar-btn stats-sidebar-back" onClick={() => setStatsView({ mode: "resumen" })}>
             Volver
          </button>
          <h3 className="stats-sidebar-title">Actividades</h3>
          {cargandoLista ? (
            <p className="stats-sidebar-loading">Cargando...</p>
          ) : actividadesList.map(a => (
            <button
              key={a.id}
              className={`stats-sidebar-btn${'actividadId' in statsView && statsView.actividadId === a.id ? ' stats-sidebar-btn--active' : ''}`}
              onClick={() => setStatsView({ mode: "tiemposActividad", actividadId: a.id })}
            >
              {a.nombre}
            </button>
          ))}
        </>
      );
    }

    if (statsView.mode === "alumnos" || statsView.mode === "alumnoDetalle") {
      return (
        <>
          <button className="stats-sidebar-btn stats-sidebar-back" onClick={() => setStatsView({ mode: "resumen" })}>
            ← Volver
          </button>
          <h3 className="stats-sidebar-title">Alumnos</h3>
          {cargandoLista ? (
            <p className="stats-sidebar-loading">Cargando...</p>
          ) : alumnosList.length === 0 ? (
            <p className="stats-sidebar-loading">Sin alumnos inscritos</p>
          ) : alumnosList.map(a => (
            <button
              key={a.id}
              className={`stats-sidebar-btn${statsView.mode === 'alumnoDetalle' && statsView.alumnoId === a.id ? ' stats-sidebar-btn--active' : ''}`}
              onClick={() => setStatsView({ mode: "alumnoDetalle", alumnoId: a.id, alumnoNombre: a.nombre })}
            >
              {a.nombre}
            </button>
          ))}
        </>
      );
    }

    return (
      <>
        <button className="stats-sidebar-btn stats-sidebar-btn--refresh" onClick={cargarEstadisticas}>
          Actualizar ↻
        </button>
        <hr className="stats-sidebar-divider" />
        <button
          className={`stats-sidebar-btn${statsView.mode === 'resumen' ? ' stats-sidebar-btn--active' : ''}`}
          onClick={() => setStatsView({ mode: "resumen" })}
        >
          Resumen general
        </button>
        <button
          className={`stats-sidebar-btn${'temaId' in statsView ? ' stats-sidebar-btn--active' : ''}`}
          onClick={handleMedias}
        >
          Puntuaciones medias
        </button>
        <button className="stats-sidebar-btn stats-sidebar-btn--tema" onClick={handleTiemposTema}>
          Tiempos por Tema 
        </button>
        <button className="stats-sidebar-btn stats-sidebar-btn--actividad" onClick={handleTiemposActividad}>
          Tiempos por Actividad 
        </button>
        <button className="stats-sidebar-btn stats-sidebar-btn--alumnos" onClick={handleAlumnos}>
          Alumnos 
        </button>
      </>
    );
  };

  const renderStatsContent = () => {
    switch (statsView.mode) {
      case "medias":
        return <MediasCurso
          cursoIdProp={id}
          embedded
          temaIdSeleccionado={'temaId' in statsView ? statsView.temaId : undefined}
        />;
      case "tiemposTema":
        if ('temaId' in statsView && statsView.temaId) {
          return <EstadisticasTema temaIdProp={String(statsView.temaId)} embedded />;
        }
        return <div className="stats-placeholder">Selecciona un tema de la lista</div>;
      case "tiemposActividad":
        if ('actividadId' in statsView && statsView.actividadId) {
          return <EstadisticasActividad actividadIdProp={String(statsView.actividadId)} embedded />;
        }
        return <div className="stats-placeholder">Selecciona una actividad de la lista</div>;
      case "alumnos":
        return <div className="stats-placeholder">Selecciona un alumno de la lista</div>;
      case "alumnoDetalle":
        return (
          <EstadisticasAlumno
            cursoIdProp={id}
            alumnoId={statsView.alumnoId}
            embedded
          />
        );
      default:
        return (
          <div className="estadisticas-yellow-card">
            {estadisticasContent}
          </div>
        );
    }
  };

return (
    <div className={embedded ? 'estadisticas-embedded' : 'estadisticas-page'}>
      {!embedded && <NavbarMisCursos />}
      
      <main className="estadisticas-main">
        {!embedded && (
          <>
            <div style={{ width: '100%', display: 'flex', marginBottom: '10px' }}>
              <button className="btn-volver-pixel" onClick={() => navigate(-1)}>
                 Volver
              </button>
            </div>
            <h1 className="estadisticas-titulo-curso">Estadísticas del Curso</h1>
          </>
        )}

        <div className="stats-layout">
          <div className="stats-sidebar">
            {renderSidebar()}
          </div>
          <div className="stats-content-area">
            {renderStatsContent()}
          </div>
        </div>
      </main>
    </div>
  );
}