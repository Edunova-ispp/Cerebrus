import { useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import { apiFetch } from '../../utils/api';
import { getCurrentUserInfo } from '../../types/curso';
import kingImg from '../../assets/props/king.png';
import espadaImg from '../../assets/props/espada.png';
import './OrdenacionAlumno.css';

type OrdenacionDTO = {
  readonly id: number;
  readonly titulo: string;
  readonly descripcion: string | null;
  readonly puntuacion: number;
  readonly imagen: string | null;
  readonly respVisible: boolean;
  readonly comentariosRespVisible: string | null;
  readonly posicion: number;
  readonly temaId: number | null;
  readonly valores: string[];
};

type RespAlumnoOrdenacionCreateResponse = {
  readonly respAlumnoOrdenacion: {
    readonly id: number;
    readonly correcta: boolean;
  };
  readonly comentario: string;
};

type ActividadAlumnoDTO = { readonly id: number };

function getCurrentUserIdFromJwt(): number | null {
  const info = getCurrentUserInfo();
  if (!info) return null;
  const raw = (info as Record<string, unknown>)?.id ?? (info as Record<string, unknown>)?.userId ?? (info as Record<string, unknown>)?.sub;
  const userId = typeof raw === 'string' ? Number(raw) : raw;
  return typeof userId === 'number' && Number.isFinite(userId) ? userId : null;
}

function isImageString(value: string): boolean {
  const trimmed = value.trim();
  if (!trimmed) return false;
  if (/^data:image\//i.test(trimmed)) return true;

  // Aceptar rutas relativas (Vite/NGINX) si parecen imagen por extensión.
  // Ejemplos: /seed/ordenacion/html.svg, seed/ordenacion/html.svg
  const pathLike = trimmed.split('#')[0]?.split('?')[0]?.toLowerCase() ?? '';
  if (/\.(png|jpe?g|gif|webp|bmp|svg)$/i.test(pathLike)) return true;

  try {
    const url = new URL(trimmed);
    const path = url.pathname.toLowerCase();
    return /\.(png|jpe?g|gif|webp|bmp|svg)$/i.test(path);
  } catch {
    return false;
  }
}

function moveItem<T>(items: readonly T[], fromIndex: number, toIndex: number): T[] {
  if (fromIndex === toIndex) return [...items];
  if (fromIndex < 0 || fromIndex >= items.length) return [...items];
  if (toIndex < 0 || toIndex >= items.length) return [...items];
  const next = [...items];
  const [moved] = next.splice(fromIndex, 1);
  next.splice(toIndex, 0, moved);
  return next;
}

export default function OrdenacionAlumno() {
  const { ordenacionId } = useParams<{ ordenacionId: string }>();
  const navigate = useNavigate();

  const initInFlightRef = useRef(false);
  const completedRef = useRef(false);
  const abandonReportedRef = useRef(false);
  const actividadAlumnoIdRef = useRef<number | null>(null);

  const [ordenacion, setOrdenacion] = useState<OrdenacionDTO | null>(null);
  const [items, setItems] = useState<string[]>([]);
  const [actividadAlumnoId, setActividadAlumnoId] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string>('');
  const [feedback, setFeedback] = useState<{ correcta: boolean; comentario?: string } | null>(null);
  const apiBase = (import.meta.env.VITE_API_URL ?? "").trim().replace(/\/$/, "");
  
  const ordenacionIdNum = useMemo(() => {
    if (!ordenacionId) return NaN;
    return Number.parseInt(ordenacionId, 10);
  }, [ordenacionId]);

  useEffect(() => {
    actividadAlumnoIdRef.current = actividadAlumnoId;
  }, [actividadAlumnoId]);

  useEffect(() => {
    return () => {
      const id = actividadAlumnoIdRef.current;
      if (!id) return;
      if (completedRef.current) return;
      if (abandonReportedRef.current) return;
      abandonReportedRef.current = true;
      apiFetch(`${apiBase}/api/actividades-alumno/${id}/abandon`, { method: 'POST' }).catch(() => {});
    };
  }, []);

  useEffect(() => {
    const run = async () => {
      if (initInFlightRef.current) return;
      initInFlightRef.current = true;

      if (!ordenacionId || Number.isNaN(ordenacionIdNum)) {
        setError('Falta el id de la ordenación en la URL');
        setLoading(false);
        initInFlightRef.current = false;
        return;
      }

      setLoading(true);
      setError('');
      setFeedback(null);

      try {
        const ordRes = await apiFetch(`${apiBase}/api/ordenaciones/${ordenacionIdNum}`);
        const ordData = (await ordRes.json()) as OrdenacionDTO;
        setOrdenacion(ordData);
        setItems(Array.isArray(ordData.valores) ? [...ordData.valores] : []);

        const alumnoId = getCurrentUserIdFromJwt();
        if (!alumnoId) throw new TypeError('No se pudo identificar al alumno conectado. Inicia sesión de nuevo.');

        const ensureRes = await apiFetch(`${apiBase}/api/actividades-alumno/ensure/${ordData.id}`);
        const ensureValue = (await ensureRes.json()) as unknown;
        const exists = ensureValue === 1 || ensureValue === '1' || ensureValue === true;

        if (exists) {
          const getAA = await apiFetch(`${apiBase}/api/actividades-alumno/alumno/${alumnoId}/actividad/${ordData.id}`);
          const aaData = (await getAA.json()) as ActividadAlumnoDTO;
          if (typeof aaData?.id === 'number' && Number.isFinite(aaData.id)) {
            setActividadAlumnoId(aaData.id);
          } else {
            throw new TypeError('Respuesta inválida al obtener ActividadAlumno');
          }
        } else {
          const createAA = await apiFetch(`${apiBase}/api/actividades-alumno`, {
            method: 'POST',
            body: JSON.stringify({ alumnoId, actividadId: ordData.id }),
          });
          const aaData = (await createAA.json()) as ActividadAlumnoDTO;
          if (typeof aaData?.id === 'number' && Number.isFinite(aaData.id)) {
            setActividadAlumnoId(aaData.id);
          } else {
            throw new TypeError('Respuesta inválida al crear ActividadAlumno');
          }
        }
      } catch (e) {
        const msg = e instanceof Error ? e.message : 'Error cargando la ordenación';
        setError(msg);
      } finally {
        setLoading(false);
        initInFlightRef.current = false;
      }
    };

    run();
  }, [ordenacionId, ordenacionIdNum]);

  const handleSubmit = async () => {
    setError('');
    setFeedback(null);

    if (!ordenacion || !ordenacion.id) {
      setError('No se ha cargado la actividad de ordenación');
      return;
    }
    if (!actividadAlumnoId) {
      setError('No se ha podido inicializar la actividad del alumno');
      return;
    }

    setSubmitting(true);
    try {
      const res = await apiFetch(`${apiBase}/api/respuestas-alumno-ordenacion`, {
        method: 'POST',
        body: JSON.stringify({
          actividadAlumno: { id: actividadAlumnoId },
          ordenacion: { id: ordenacion.id },
          valoresAlum: items,
        }),
      });
if (!res.ok) throw new Error('Error al guardar la respuesta');


      const data = (await res.json()) as RespAlumnoOrdenacionCreateResponse;
      const respuestaId = data?.respAlumnoOrdenacion?.id;
      if (respuestaId) {
        // 3. LLAMADA CRÍTICA: Corregir automáticamente la actividad (PUT)
        // Mandamos el ID en un array tal como espera el controlador
        await apiFetch(`${apiBase}/api/actividades-alumno/corregir-automaticamente/${actividadAlumnoId}`, {
          method: 'PUT',
          body: JSON.stringify([respuestaId]),
        });
      }
      const correcta = Boolean(data?.respAlumnoOrdenacion?.correcta);
      const comentario = typeof data?.comentario === 'string' ? data.comentario : '';

      if (correcta) {
        completedRef.current = true;
        window.alert('Tu respuesta es correcta.');
        navigate(-1);
        return;
      }

      setFeedback({
        correcta,
        comentario: ordenacion.respVisible ? comentario : undefined,
      });
    } catch (e) {
      const msg = e instanceof TypeError ? e.message : 'Error enviando la respuesta';
      setError(msg);
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="ordenacion-alumno-page">
        <NavbarMisCursos />
        <main className="ordenacion-alumno-main">
          <p className="ca-text">Cargando...</p>
        </main>
      </div>
    );
  }

  return (
    <div className="ordenacion-alumno-page">
      <NavbarMisCursos />

      <main className="ordenacion-alumno-main">
        {error && (
          <p className="ca-text" style={{ marginTop: 0 }}>
            {error}
          </p>
        )}

        {ordenacion && (
          
          <>
            <div className="ord-top">

              {/* Botón salir con espada */}
              <button className="ord-exit-btn" type="button" onClick={() => navigate(-1)}>
      <img src={espadaImg} alt="" className="ord-exit-icon" />
      Salir
    </button>

              {/* Banner título */}
              <div className="ord-title-banner">
                <h1 className="ord-title">{ordenacion.titulo}</h1>
                {ordenacion.descripcion && (
                  <p className="ord-subtitle">{ordenacion.descripcion}</p>
                )}
              </div>
            </div>

            {/* Layout: rey izquierda, items derecha */}
<div className="ord-content-row">

  {/* Columna izquierda: botón salir + rey + bocadillo */}
  <div className="ord-left-col">

    <div className="ord-king-row">
      <div className="ord-speech-bubble">
        <span>Esto es un caos</span>
        <span>Ordena las casillas</span>
        <span>Ordena el reino</span>
      </div>
      <img src={kingImg} alt="Rey" className="ord-king-img" />
    </div>
  </div>

  {/* Columna derecha: items + botón enviar */}
  <div>
    <div className="ord-items">
      {items.map((value, index) => (
        <div key={`${value}-${index}`} className="ord-item">
          <div className="ord-item-index">{index + 1}.</div>

          <div className="ord-item-value">
            {isImageString(value) ? (
              <img src={value} alt={`Elemento ${index + 1}`} className="ord-item-img" />
            ) : (
              <div className="ord-item-text">{value}</div>
            )}
          </div>

          <div className="ord-item-actions">
            <button
              className="ord-arrow-btn"
              type="button"
              disabled={index === 0}
              onClick={() => setItems((prev) => moveItem(prev, index, index - 1))}
            >
              ↑
            </button>
            <button
              className="ord-arrow-btn"
              type="button"
              disabled={index === items.length - 1}
              onClick={() => setItems((prev) => moveItem(prev, index, index + 1))}
            >
              ↓
            </button>
          </div>
        </div>
      ))}
    </div>

    <div className="ord-bottom">
      <div className="ord-bottom-inner">
        {feedback && (
          <div className="ord-feedback">
            <div>{feedback.correcta ? 'Tu respuesta es correcta.' : 'Tu respuesta es incorrecta.'}</div>
            {ordenacion.respVisible && feedback.comentario && <div>{feedback.comentario}</div>}
          </div>
        )}
        <button
          className="ca-btn-guardar"
          type="button"
          disabled={submitting || items.length === 0 || !actividadAlumnoId}
          onClick={handleSubmit}
        >
          {submitting ? 'Enviando...' : 'Enviar'}
        </button>
      </div>
    </div>
  </div>

</div>
            
          </>
        )}

        {!ordenacion && !error && <p className="ca-text">No se encontró la ordenación.</p>}
      </main>
    </div>
  );
}