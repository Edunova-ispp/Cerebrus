import { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { apiFetch } from '../../utils/api';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import './DetalleIntentoActividad.css';

interface RespuestaIntento {
  respuestaId: number;
  tipoRespuesta: string;
  enunciado: string;
  respuestaAlumno: string;
  correcta: boolean | null;
}

interface IntentoDetalle {
  intentoId: number;
  cursoId: number;
  alumnoId: number;
  actividadId: number;
  actividadTitulo: string;
  actividadTipo: string;
  actividadImagen?: string | null;
  puntuacionMaxima: number;
  fechaInicio: string;
  fechaFin: string;
  tiempoMinutos: number;
  puntuacion: number;
  nota: number;
  numAbandonos: number;
  respuestas: RespuestaIntento[];
}

interface IntentoActualizado {
  id: number;
  puntuacion: number;
  nota: number;
}

function formatFecha(iso?: string | null): string {
  if (!iso) return '—';
  const d = new Date(iso);
  if (isNaN(d.getTime())) return '—';
  return `${d.toLocaleDateString('es-ES')} ${d.toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' })}`;
}

function formatTiempo(min?: number | null): string {
  if (!min || min <= 0) return '—';
  return `${min} min`;
}

function tipoLegible(tipo: string): string {
  const map: Record<string, string> = {
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
  return map[tipo] ?? tipo;
}

export default function DetalleIntentoActividad() {
  const { cursoId, alumnoId, actividadId, intentoId } = useParams<{
    cursoId: string;
    alumnoId: string;
    actividadId: string;
    intentoId: string;
  }>();
  const navigate = useNavigate();

  const [detalle, setDetalle] = useState<IntentoDetalle | null>(null);
  const [puntuacionEditada, setPuntuacionEditada] = useState('');
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  useEffect(() => {
    if (!cursoId || !alumnoId || !actividadId || !intentoId) return;
    const apiBase = (import.meta.env.VITE_API_URL ?? '').trim().replace(/\/$/, '');

    const cargar = async () => {
      setLoading(true);
      setError(null);
      setSuccess(null);
      try {
        const res = await apiFetch(
          `${apiBase}/api/estadisticas/cursos/${cursoId}/alumnos/${alumnoId}/actividades/${actividadId}/intentos/${intentoId}`,
        );
        const data: IntentoDetalle = await res.json();
        setDetalle(data);
        setPuntuacionEditada(String(data.puntuacion ?? 0));
      } catch (e) {
        setError((e as Error).message);
      } finally {
        setLoading(false);
      }
    };

    cargar();
  }, [cursoId, alumnoId, actividadId, intentoId]);

  const titulo = useMemo(() => {
    if (!detalle) return 'Detalle de intento';
    return `${detalle.actividadTitulo} · Detalle del intento`;
  }, [detalle]);

  const guardarPuntuacion = async () => {
    if (!detalle || !cursoId || !alumnoId || !actividadId || !intentoId) return;
    setSaving(true);
    setError(null);
    setSuccess(null);
    const apiBase = (import.meta.env.VITE_API_URL ?? '').trim().replace(/\/$/, '');

    try {
      const valor = Number(puntuacionEditada);
      if (!Number.isFinite(valor)) {
        throw new Error('La puntuación debe ser numérica.');
      }
      if (valor > (detalle.puntuacionMaxima ?? 0)) {
        throw new Error(`La puntuación no puede superar ${detalle.puntuacionMaxima ?? 0} puntos.`);
      }

      const res = await apiFetch(
        `${apiBase}/api/estadisticas/cursos/${cursoId}/alumnos/${alumnoId}/actividades/${actividadId}/intentos/${intentoId}/puntuacion`,
        {
          method: 'PUT',
          body: JSON.stringify({ puntuacion: Math.round(valor) }),
        },
      );

      const updated: IntentoActualizado = await res.json();
      setDetalle(prev => prev ? { ...prev, puntuacion: updated.puntuacion, nota: updated.nota } : prev);
      setPuntuacionEditada(String(updated.puntuacion));
      setSuccess('Puntuación actualizada correctamente.');
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return <div className="dia-wrap"><div className="dia-box">Cargando intento...</div></div>;
  }

  if (error && !detalle) {
    return <div className="dia-wrap"><div className="dia-box dia-error">{error}</div></div>;
  }

  if (!detalle) {
    return null;
  }

  return (
    <>
      <NavbarMisCursos />
      <div className="dia-wrap">
        <div className="dia-box">
        <div className="dia-topbar">
          <button type="button" className="dia-back" onClick={() => navigate(-1)}>Volver</button>
          <Link to={`/cursos/${cursoId}`} className="dia-link-curso">Ir al curso</Link>
        </div>

        <h1 className="dia-title">{titulo}</h1>
        <p className="dia-subtitle">Tipo: {tipoLegible(detalle.actividadTipo)}</p>

        {detalle.actividadTipo === 'MarcarImagen' && detalle.actividadImagen && (
          <div className="dia-actividad-imagen-wrap">
            <img
              src={detalle.actividadImagen}
              alt={`Imagen de ${detalle.actividadTitulo}`}
              className="dia-actividad-imagen"
            />
          </div>
        )}

        <div className="dia-metrics">
          <div className="dia-metric"><span>Fecha inicio</span><strong>{formatFecha(detalle.fechaInicio)}</strong></div>
          <div className="dia-metric"><span>Fecha fin</span><strong>{formatFecha(detalle.fechaFin)}</strong></div>
          <div className="dia-metric"><span>Tiempo</span><strong>{formatTiempo(detalle.tiempoMinutos)}</strong></div>
          <div className="dia-metric"><span>Abandonos</span><strong>{detalle.numAbandonos ?? 0}</strong></div>
          <div className="dia-metric"><span>Nota</span><strong>{detalle.nota ?? 0}/10</strong></div>
        </div>

        <div className="dia-score-editor">
          <label htmlFor="puntuacion">Puntuación asignada</label>
          <div className="dia-score-row">
            <input
              id="puntuacion"
              type="number"
              min={0}
              max={detalle.puntuacionMaxima ?? 0}
              value={puntuacionEditada}
              onChange={e => setPuntuacionEditada(e.target.value)}
            />
            <span className="dia-max">/ {detalle.puntuacionMaxima ?? 0} pts</span>
            <button type="button" onClick={guardarPuntuacion} disabled={saving}>
              {saving ? 'Guardando...' : 'Guardar cambios'}
            </button>
          </div>
          {success && <p className="dia-success">{success}</p>}
          {error && <p className="dia-error">{error}</p>}
        </div>

        <div className="dia-respuestas">
          <h2>Respuestas del alumno</h2>
          {detalle.respuestas.length === 0 ? (
            <p className="dia-empty">Este intento no tiene respuestas registradas.</p>
          ) : (
            detalle.respuestas.map((r, idx) => (
              <article key={r.respuestaId ?? idx} className="dia-respuesta-card">
                <header>
                  <span className="dia-pill">{r.tipoRespuesta}</span>
                  {r.correcta !== null && (
                    <span className={`dia-correcta ${r.correcta ? 'ok' : 'ko'}`}>
                      {r.correcta ? 'Correcta' : 'Incorrecta'}
                    </span>
                  )}
                </header>
                <h3>{r.enunciado || `Respuesta #${idx + 1}`}</h3>
                <p>{r.respuestaAlumno || 'Sin contenido'}</p>
              </article>
            ))
          )}
        </div>
      </div>
      </div>
    </>
  );
}
