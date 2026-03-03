import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import { apiFetch } from '../../utils/api';
import './MapaCurso.css';

type ActividadDTO = {
  readonly id: number;
  readonly titulo: string;
  readonly descripcion: string | null;
  readonly puntuacion: number;
  readonly posicion: number;
  readonly tipo: string;
};

type TemaDTO = {
  readonly id: number;
  readonly titulo: string;
  readonly actividades: ActividadDTO[];
};

export default function MapaCurso() {
  const { id: cursoId } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [temas, setTemas] = useState<TemaDTO[]>([]);
  const [selectedIndex, setSelectedIndex] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  // actividadId → true if the student already has a record for it
  const [completionMap, setCompletionMap] = useState<Map<number, boolean>>(new Map());

  // Load all temas + their activities
  useEffect(() => {
    if (!cursoId) return;
    setLoading(true);
    setError('');
    apiFetch(`/api/temas/curso/${cursoId}/alumno`)
      .then((r) => r.json())
      .then((data: TemaDTO[]) => {
        setTemas(Array.isArray(data) ? data : []);
        setSelectedIndex(0);
      })
      .catch((e) => {
        const msg = e instanceof Error ? e.message : 'Error cargando los temas';
        setError(msg);
      })
      .finally(() => setLoading(false));
  }, [cursoId]);

  // When selected tema changes, check completion for its activities
  useEffect(() => {
    const tema = temas[selectedIndex];
    if (!tema || tema.actividades.length === 0) return;

    // Only check activities we haven't already checked
    const unchecked = tema.actividades.filter((act) => !completionMap.has(act.id));
    if (unchecked.length === 0) return;

    Promise.all(
      unchecked.map((act) =>
        apiFetch(`/api/actividades-alumno/ensure/${act.id}`)
          .then((r) => r.json())
          .then((val) => ({
            id: act.id,
            done: val === 1 || val === '1' || val === true,
          }))
          .catch(() => ({ id: act.id, done: false })),
      ),
    ).then((results) => {
      setCompletionMap((prev) => {
        const next = new Map(prev);
        for (const { id, done } of results) next.set(id, done);
        return next;
      });
    });
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [temas, selectedIndex]);

  const handleActivityClick = (act: ActividadDTO) => {
    if (act.tipo === 'general') {
      navigate(`/generales/test/${act.id}/alumno`);
    } else if (act.tipo === 'ordenacion') {
      navigate(`/ordenaciones/${act.id}/alumno`);
    }
    // other types: do nothing (button is disabled)
  };

  const selectedTema = temas[selectedIndex] ?? null;

  if (loading) {
    return (
      <div className="mapa-page">
        <NavbarMisCursos />
        <main className="mapa-main">
          <p className="ca-text">Cargando curso...</p>
        </main>
      </div>
    );
  }

  if (error) {
    return (
      <div className="mapa-page">
        <NavbarMisCursos />
        <main className="mapa-main">
          <p className="ca-text">{error}</p>
        </main>
      </div>
    );
  }

  return (
    <div className="mapa-page">
      <NavbarMisCursos />
      <main className="mapa-main">
        <button className="mapa-back-btn" type="button" onClick={() => navigate(-1)}>
          ← Volver
        </button>

        <div className="mapa-layout">
          {/* ── Left: Temas ── */}
          <aside className="mapa-sidebar">
            <h2 className="mapa-sidebar-title">Temas</h2>
            {temas.length === 0 && <p className="ca-text">Sin temas</p>}
            {temas.map((tema, idx) => (
              <button
                key={tema.id}
                type="button"
                className={`mapa-tema-btn${idx === selectedIndex ? ' mapa-tema-btn--active' : ''}`}
                onClick={() => setSelectedIndex(idx)}
              >
                {tema.titulo}
              </button>
            ))}
          </aside>

          {/* ── Right: Activities ── */}
          <section className="mapa-activities">
            {selectedTema && (
              <>
                <h2 className="mapa-activities-title">{selectedTema.titulo}</h2>
                {selectedTema.actividades.length === 0 && (
                  <p className="ca-text">Este tema no tiene actividades.</p>
                )}
                <div className="mapa-activities-grid">
                  {[...selectedTema.actividades]
                    .sort((a, b) => a.posicion - b.posicion)
                    .map((act) => {
                      const done = completionMap.get(act.id) ?? false;
                      const navigable = act.tipo === 'general' || act.tipo === 'ordenacion';
                      return (
                        <button
                          key={act.id}
                          type="button"
                          className={`mapa-act-card${done ? ' mapa-act-card--done' : ''}${!navigable ? ' mapa-act-card--disabled' : ''}`}
                          onClick={() => navigable && handleActivityClick(act)}
                          disabled={!navigable}
                          title={!navigable ? 'Próximamente' : undefined}
                        >
                          <div className="mapa-act-header">
                            <span className="mapa-act-titulo">{act.titulo}</span>
                            {done && <span className="mapa-act-done-badge">✓ Iniciada</span>}
                          </div>
                          {act.descripcion && (
                            <p className="mapa-act-desc">{act.descripcion}</p>
                          )}
                          <div className="mapa-act-footer">
                            <span className="mapa-act-puntos">{act.puntuacion} pts</span>
                            {!navigable && (
                              <span className="mapa-act-soon">Próximamente</span>
                            )}
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
