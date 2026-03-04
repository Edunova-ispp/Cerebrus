import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import { apiFetch } from '../../utils/api';
import { getCurrentUserInfo } from '../../types/curso'; // Asegúrate de importar esto
import './MapaCurso.css';

type ActividadDTO = {
  readonly id: number;
  readonly titulo: string;
  readonly descripcion: string | null;
  readonly puntuacion: number;
  readonly posicion: number;
  readonly tipo: string;
};

// 1. Definimos bien el tipo de la información de progreso
type CompletionInfo = {
  done: boolean;
  terminada: boolean;
  puntuacionObtenida?: number;
};

type TemaDTO = {
  readonly id: number;
  readonly titulo: string;
  readonly actividades: ActividadDTO[];
};

// Helper para el ID del alumno
function getCurrentUserIdFromJwt(): number | null {
  const info = getCurrentUserInfo();
  if (!info) return null;
  const raw = (info as any)?.id ?? (info as any)?.userId ?? (info as any)?.sub;
  return typeof raw === 'string' ? Number(raw) : raw;
}

export default function MapaCurso() {
  const { id: cursoId } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [temas, setTemas] = useState<TemaDTO[]>([]);
  const [selectedIndex, setSelectedIndex] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  void loading;
  void error;

  // 2. CORRECCIÓN: El Map ahora guarda objetos de tipo CompletionInfo
  const [completionMap, setCompletionMap] = useState<Map<number, CompletionInfo>>(new Map());

  useEffect(() => {
    const apiBase = (import.meta.env.VITE_API_URL ?? "").trim().replace(/\/$/, "");
    if (!cursoId) return;
    setLoading(true);
    apiFetch(`${apiBase}/api/temas/curso/${cursoId}/alumno`)
      .then((r) => r.json())
      .then((data: TemaDTO[]) => {
        setTemas(Array.isArray(data) ? data : []);
        setSelectedIndex(0);
      })
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, [cursoId]);

  useEffect(() => {
    const apiBase = (import.meta.env.VITE_API_URL ?? "").trim().replace(/\/$/, "");
    const tema = temas[selectedIndex];
    if (!tema || tema.actividades.length === 0) return;

    const unchecked = tema.actividades.filter((act) => !completionMap.has(act.id));
    if (unchecked.length === 0) return;

    const alumnoId = getCurrentUserIdFromJwt();
    if (!alumnoId) return;

    Promise.all(
      unchecked.map((act) =>
        apiFetch(`${apiBase}/api/actividades-alumno/alumno/${alumnoId}/actividad/${act.id}`)
          .then(async (r) => {
            if (r.status === 404) return { id: act.id, info: { done: false, terminada: false } };
            const data = await r.json();
            return {
              id: act.id,
              info: {
                done: true,
                terminada: !!data.acabada && data.acabada !== "1970-01-01T00:00:00",
                puntuacionObtenida: data.puntuacion
              }
            };
          })
          .catch(() => ({ id: act.id, info: { done: false, terminada: false } }))
      )
    ).then((results) => {
      setCompletionMap((prev) => {
        const next = new Map(prev);
        results.forEach(({ id, info }) => next.set(id, info));
        return next;
      });
    });
  }, [temas, selectedIndex]);

  const handleActivityClick = (act: ActividadDTO) => {
    const tipoReal = act.tipo ? act.tipo.toUpperCase() : '';
    if (tipoReal === 'TEORIA') navigate(`/actividades/teoria/${act.id}`);
    else if (tipoReal === 'TEST' || tipoReal === 'GENERAL') navigate(`/generales/test/${act.id}/alumno`);
    else if (tipoReal === 'ORDENACION') navigate(`/ordenaciones/${act.id}/alumno`);
  };

  const selectedTema = temas[selectedIndex] ?? null;

  return (
    <div className="mapa-page">
      <NavbarMisCursos />
      <main className="mapa-main">
        <button className="detalle-volver" onClick={() => navigate(-1)}>← Volver</button>
        <div className="mapa-layout">
          <aside className="mapa-sidebar">
            <h2 className="mapa-sidebar-title">Temas</h2>
            {temas.map((tema, idx) => (
              <button
                key={tema.id}
                className={`mapa-tema-btn${idx === selectedIndex ? ' mapa-tema-btn--active' : ''}`}
                onClick={() => setSelectedIndex(idx)}
              >
                {tema.titulo}
              </button>
            ))}
          </aside>

          <section className="mapa-activities">
            {selectedTema && (
              <>
                <h2 className="mapa-activities-title">{selectedTema.titulo}</h2>
                <div className="mapa-activities-grid">
                  {[...selectedTema.actividades]
                    .sort((a, b) => a.posicion - b.posicion)
                    .map((act) => {
                      // 3. Ahora 'info' sí tendrá las propiedades
                      const info = completionMap.get(act.id);
                      const done = info?.done ?? false;
                      const terminada = info?.terminada ?? false;
                      const puntosObtenidos = info?.puntuacionObtenida ?? 0;
                      
                      const tipo = act.tipo.toUpperCase();
                      const navigable = ['TEST', 'GENERAL', 'ORDENACION', 'TEORIA'].includes(tipo);

                      return (
                        <button
                          key={act.id}
                          className={`mapa-act-card${done ? ' mapa-act-card--done' : ''}`}
                          onClick={() => navigable && handleActivityClick(act)}
                          disabled={!navigable}
                        >
                          <div className="mapa-act-header">
                            <span className="mapa-act-titulo">{act.titulo}</span>
                            {done && (
                              <span className={`mapa-act-done-badge ${terminada ? 'badge--terminada' : ''}`}>
                                {terminada ? '✓ Terminado' : '• Iniciado'}
                              </span>
                            )}
                          </div>
                          {act.descripcion && <p className="mapa-act-desc">{act.descripcion}</p>}
                          <div className="mapa-act-footer">
                            <span className="mapa-act-puntos">
                              {terminada 
                                ? <strong>{puntosObtenidos} / {act.puntuacion} pts</strong> 
                                : `${act.puntuacion} pts`}
                            </span>
                          </div>
                        </button>
                      );
                    })}
                </div>
              </>
            )}
          </section>
        </div>
      </main>
    </div>
  );
}