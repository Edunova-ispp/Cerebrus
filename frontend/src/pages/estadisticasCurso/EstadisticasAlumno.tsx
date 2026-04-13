import { useState, useEffect, useMemo } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { apiFetch } from '../../utils/api';
import './EstadisticasAlumno.css';

// ===== Types =====

interface IntentoActividad {
  id: number;
  fechaInicio: string;
  fechaFin: string;
  puntuacion: number;
  nota: number;
  tiempoMinutos: number;
  tiempoSegundos?: number;
  numAbandonos: number;
}

interface ActividadEstadisticas {
  actividadId: number;
  titulo: string;
  tipo: string;
  puntuacionMaxima: number;
  completada: boolean;
  notaAlumno: number | null;
  puntuacionAlumno: number | null;
  notaMediaClase: number;
  desviacion: number | null;
  intentos: IntentoActividad[];
}

interface TemaEstadisticas {
  temaId: number;
  titulo: string;
  completado: boolean;
  actividades: ActividadEstadisticas[];
}

interface EstadisticasAlumnoData {
  alumnoId: number;
  nombreAlumno: string;
  notaMedia: number;
  notaMin: number | null;
  notaMax: number | null;
  numActividadesCompletadas: number;
  totalActividades: number;
  tiempoTotalMinutos: number;
  tiempoTotalSegundos?: number;
  temas: TemaEstadisticas[];
}

// ===== Helpers =====

const TIPO_LABELS: Record<string, string> = {
  GeneralTest: 'Test',
  Ordenacion: 'Ordenación',
  Teoria: 'Teoría',
  MarcarImagen: 'Imagen',
  Tablero: 'Tablero',
  Clasificacion: 'Clasificación',
  Carta: 'Carta',
  Crucigrama: 'Crucigrama',
  Abierta: 'Abierta',
};

function tipoLabel(tipo: string): string {
  return TIPO_LABELS[tipo] ?? tipo;
}

function formatFecha(iso: string | null): string {
  if (!iso) return '—';
  const d = new Date(iso);
  if (isNaN(d.getTime())) return '—';
  return (
    d.toLocaleDateString('es-ES', { day: '2-digit', month: '2-digit', year: '2-digit' }) +
    ' ' +
    d.toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' })
  );
}

function formatTiempo(min?: number | null, segundos?: number | null): string {
  const totalSegundos = ((min ?? 0) * 60) + (segundos ?? 0);
  if (totalSegundos <= 0) return '—';
  if (totalSegundos < 60) return `${totalSegundos} s`;
  const mins = Math.floor(totalSegundos / 60);
  const secs = totalSegundos % 60;
  if (secs === 0) return mins === 1 ? '1 min' : `${mins} min`;
  return `${mins} min ${secs} s`;
}

function notaBadgeClass(nota: number | null, media: number): string {
  if (nota === null) return 'ea-nota-badge ea-nota-badge--none';
  if (nota >= media + 1) return 'ea-nota-badge ea-nota-badge--above';
  if (nota <= media - 1) return 'ea-nota-badge ea-nota-badge--below';
  return 'ea-nota-badge ea-nota-badge--avg';
}

// ===== Props =====

interface EstadisticasAlumnoProps {
  readonly cursoIdProp?: string;
  readonly alumnoId?: number;
  readonly embedded?: boolean;
}

export default function EstadisticasAlumno({ cursoIdProp, alumnoId }: EstadisticasAlumnoProps = {}) {
  const params = useParams<{ id: string }>();
  const id = cursoIdProp ?? params.id;
  const navigate = useNavigate();

  const [data, setData] = useState<EstadisticasAlumnoData | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [expandedTemas, setExpandedTemas] = useState<Set<number>>(new Set());
  const [expandedActividades, setExpandedActividades] = useState<Set<number>>(new Set());

  useEffect(() => {
    if (!id || !alumnoId) return;

    const controller = new AbortController();

    const cargar = async () => {
      setLoading(true);
      setData(null);
      const apiBase = (import.meta.env.VITE_API_URL ?? '').trim().replace(/\/$/, '');
      try {
        const res = await apiFetch(`${apiBase}/api/estadisticas/cursos/${id}/alumnos/${alumnoId}`);
        if (!res.ok) throw new Error('Error al cargar estadísticas del alumno');
        const d: EstadisticasAlumnoData = await res.json();
        if (controller.signal.aborted) return;
        setData(d);
        setExpandedTemas(new Set(d.temas.map(t => t.temaId)));
        setExpandedActividades(new Set());
        setError(null);
      } catch (err) {
        if (!controller.signal.aborted) {
          setError((err as Error).message);
        }
      } finally {
        if (!controller.signal.aborted) setLoading(false);
      }
    };

    cargar();
    return () => controller.abort();
  }, [id, alumnoId]);

  const toggleTema = (temaId: number) => {
    setExpandedTemas(prev => {
      const s = new Set(prev);
      if (s.has(temaId)) s.delete(temaId); else s.add(temaId);
      return s;
    });
  };

  const toggleActividad = (actId: number) => {
    setExpandedActividades(prev => {
      const s = new Set(prev);
      if (s.has(actId)) s.delete(actId); else s.add(actId);
      return s;
    });
  };

  const irDetalleIntento = (actividadId: number, intentoId: number) => {
    if (!id || !alumnoId) return;
    navigate(`/estadisticas/cursos/${id}/alumnos/${alumnoId}/actividades/${actividadId}/intentos/${intentoId}`);
  };

  const statCards = useMemo(() => {
    if (!data) return null;
    return [
      { label: 'Nota media', value: data.notaMedia > 0 ? data.notaMedia.toFixed(1) : '—', mod: 'media' },
      { label: 'Nota mínima', value: data.notaMin !== null ? String(data.notaMin) : '—', mod: 'min' },
      { label: 'Nota máxima', value: data.notaMax !== null ? String(data.notaMax) : '—', mod: 'max' },
      { label: 'Actividades', value: `${data.numActividadesCompletadas}/${data.totalActividades}`, mod: 'completadas' },
      { label: 'Tiempo total', value: formatTiempo(data.tiempoTotalMinutos, data.tiempoTotalSegundos), mod: 'tiempo' },
    ];
  }, [data]);

  if (!alumnoId) {
    return <div className="stats-placeholder">Selecciona un alumno de la lista</div>;
  }

  if (loading) return <div className="stats-info-msg">Cargando estadísticas...</div>;
  if (error) return <div className="stats-error-msg">{error}</div>;
  if (!data) return null;

  const content = (
    <>
      <div className="ea-alumno-header">
        <span className="ea-alumno-nombre">{data.nombreAlumno}</span>
        <span className="ea-alumno-subtitle">Estadísticas individuales</span>
      </div>

      {statCards && (
        <div className="stat-cards-row">
          {statCards.map(c => (
            <div key={c.mod} className={`stat-card stat-card--${c.mod}`}>
              <span className="stat-card__label">{c.label}</span>
              <span className="stat-card__value">{c.value}</span>
            </div>
          ))}
        </div>
      )}

      {data.temas.map(tema => (
        <div key={tema.temaId} className="ea-tema-section">
          <button
            type="button"
            className={`ea-tema-header${tema.completado ? ' ea-tema-header--done' : ''}`}
            onClick={() => toggleTema(tema.temaId)}
          >
            <span className="ea-tema-arrow">{expandedTemas.has(tema.temaId) ? '▾' : '▸'}</span>
            <span className="ea-tema-title">{tema.titulo}</span>
            <span className={`ea-tema-badge${tema.completado ? ' ea-tema-badge--done' : ' ea-tema-badge--pending'}`}>
              {tema.completado ? '✓ Completado' : 'En progreso'}
            </span>
          </button>

          {expandedTemas.has(tema.temaId) && (
            <div className="ea-actividades-list">
              {tema.actividades.length === 0 ? (
                <p className="ea-empty">Sin actividades</p>
              ) : (
                tema.actividades.map(act => (
                  <div key={act.actividadId} className="ea-actividad-block">
                    <button
                      type="button"
                      className={`ea-actividad-row${act.completada ? ' ea-actividad-row--done' : ' ea-actividad-row--pending'}`}
                      onClick={() => toggleActividad(act.actividadId)}
                    >
                      <span className="ea-act-tipo-badge">{tipoLabel(act.tipo)}</span>
                      <span className="ea-act-titulo">{act.titulo}</span>
                      <span className="ea-act-stats">
                        {act.completada && act.notaAlumno !== null ? (
                          <>
                            <span className={notaBadgeClass(act.notaAlumno, act.notaMediaClase)}>
                              {act.notaAlumno}/10
                            </span>
                            <span className="ea-act-media">Media: {act.notaMediaClase.toFixed(1)}</span>
                            {act.desviacion !== null && (
                              <span className={`ea-act-desv${act.desviacion >= 0 ? ' ea-act-desv--pos' : ' ea-act-desv--neg'}`}>
                                {act.desviacion >= 0 ? '+' : ''}{act.desviacion.toFixed(1)}
                              </span>
                            )}
                          </>
                        ) : (
                          <span className="ea-act-no-completada">Sin completar</span>
                        )}
                        {act.intentos.length > 0 && (
                          <span className="ea-act-intentos-badge">
                            {act.intentos.length} {act.intentos.length === 1 ? 'intento' : 'intentos'}
                          </span>
                        )}
                        <span className="ea-act-expand-arrow">
                          {expandedActividades.has(act.actividadId) ? '▴' : '▾'}
                        </span>
                      </span>
                    </button>

                    {expandedActividades.has(act.actividadId) && (
                      <div className="ea-intentos-list">
                        {act.intentos.length === 0 ? (
                          <p className="ea-empty">Sin intentos registrados</p>
                        ) : (
                          act.intentos.map((intento, idx) => (
                            <button
                              key={intento.id}
                              type="button"
                              className="ea-intento-row ea-intento-row--clickable"
                              onClick={() => irDetalleIntento(act.actividadId, intento.id)}
                            >
                              <span className="ea-intento-num">#{idx + 1}</span>
                              <span className="ea-intento-fecha">
                                {formatFecha(intento.fechaFin || intento.fechaInicio)}
                              </span>
                              <span className="ea-intento-nota">
                                {intento.nota !== null ? `${intento.nota}/10` : '—'}
                              </span>
                              <span className="ea-intento-puntos">
                                {intento.puntuacion !== null ? `${intento.puntuacion} pts` : '—'}
                              </span>
                              <span className="ea-intento-tiempo">
                                {formatTiempo(intento.tiempoMinutos, intento.tiempoSegundos)}
                              </span>
                              {intento.numAbandonos > 0 && (
                                <span className="ea-intento-abandonos">
                                  {intento.numAbandonos} abandono{intento.numAbandonos !== 1 ? 's' : ''}
                                </span>
                              )}
                            </button>
                          ))
                        )}
                      </div>
                    )}
                  </div>
                ))
              )}
            </div>
          )}
        </div>
      ))}
    </>
  );

  return <div className="estadisticas-yellow-card ea-root">{content}</div>;
}