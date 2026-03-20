import { useEffect, useMemo, useState } from 'react';
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

interface MediasCursoProps {
  readonly cursoIdProp?: string;
  readonly embedded?: boolean;
  readonly temaIdSeleccionado?: number;
}

export default function MediasCurso({ cursoIdProp, embedded, temaIdSeleccionado }: MediasCursoProps = {}) {
  const params = useParams<{ id: string }>();
  const id = cursoIdProp ?? params.id;
  const navigate = useNavigate();

  const [temas, setTemas] = useState<Tema[]>([]);
  const [temaSeleccionado, setTemaSeleccionado] = useState<Tema | null>(null);
  const [mapaNotas, setMapaNotas] = useState<Map<number, number>>(new Map());
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => { cargarTodo(); }, [id]);

  // When parent controls which tema is selected
  useEffect(() => {
    if (temaIdSeleccionado !== undefined && temas.length > 0) {
      const found = temas.find(t => t.id === temaIdSeleccionado);
      if (found) setTemaSeleccionado(found);
    }
  }, [temaIdSeleccionado, temas]);

  const cargarTodo = async () => {
    setLoading(true);
    setError('');
    try {
      const token = localStorage.getItem('token');
      const apiBase = (import.meta.env.VITE_API_URL ?? '').trim().replace(/\/$/, '');

      const [resEstructura, resNotas] = await Promise.all([
        fetch(`${apiBase}/api/temas/curso/${id}/maestro`, { headers: { Authorization: `Bearer ${token}` } }),
        fetch(`${apiBase}/api/cursos/${id}/NotasMedias`,  { headers: { Authorization: `Bearer ${token}` } }),
      ]);

      if (!resEstructura.ok || !resNotas.ok) throw new Error('Error al obtener datos');

      const temasData: Tema[] = await resEstructura.json();
      const notasData: number[] = await resNotas.json();

      const nuevoMapa = new Map<number, number>();
      let idx = 0;
      temasData.forEach(tema => {
        tema.actividades.forEach(act => {
          if (idx < notasData.length) nuevoMapa.set(act.id, notasData[idx++]);
        });
      });

      setMapaNotas(nuevoMapa);
      setTemas(temasData);
      // Only auto-select when standalone; when embedded the parent sidebar drives selection
      if (!embedded && temasData.length > 0) setTemaSeleccionado(temasData[0]);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error de conexión');
    } finally {
      setLoading(false);
    }
  };

  const statsResumen = useMemo(() => {
    if (!temaSeleccionado || temaSeleccionado.actividades.length === 0) return null;
    const notas = temaSeleccionado.actividades.map(a => mapaNotas.get(a.id) ?? 0);
    const media = notas.reduce((s, n) => s + n, 0) / notas.length;
    const maxNota = Math.max(...notas);
    const minNota = Math.min(...notas);
    const mejorAct = temaSeleccionado.actividades[notas.indexOf(maxNota)];
    const peorAct  = temaSeleccionado.actividades[notas.indexOf(minNota)];
    return { media, maxNota, minNota, mejorAct, peorAct };
  }, [temaSeleccionado, mapaNotas]);

  const renderContent = () => (
    <>
      {error && <div className="stats-error-msg">{error}</div>}
      {loading && <div className="stats-info-msg">Cargando datos...</div>}
      {!loading && !error && (
        <>
          {statsResumen && (
            <div className="stat-cards-row">
              <div className="stat-card">
                <span className="stat-card__label">Nota media</span>
                <span className="stat-card__value">{statsResumen.media.toFixed(1)}</span>
              </div>
              <div className="stat-card">
                <span className="stat-card__label">Mejor nota</span>
                <span className="stat-card__value">{statsResumen.maxNota}</span>
                <span className="stat-card__name">{statsResumen.mejorAct.titulo}</span>
              </div>
              <div className="stat-card">
                <span className="stat-card__label">Peor nota</span>
                <span className="stat-card__value">{statsResumen.minNota}</span>
                <span className="stat-card__name">{statsResumen.peorAct.titulo}</span>
              </div>
            </div>
          )}

          {temaSeleccionado && temaSeleccionado.actividades.length > 0 ? (
            <div className="table-scroll-container">
              <table className="pixel-table">
                <thead>
                  <tr>
                    <th>Posición</th>
                    <th>Actividad</th>
                    <th className="text-center">Nota Media</th>
                  </tr>
                </thead>
                <tbody>
                  {temaSeleccionado.actividades.map((act, index) => (
                    <tr key={act.id}>
                      <td className="text-center">
                        <span className={`rank-badge${index < 3 ? ` rank-badge--${index + 1}` : ''}`}>
                          {index + 1}
                        </span>
                      </td>
                      <td>{act.titulo}</td>
                      <td className="text-center font-bold">{mapaNotas.get(act.id) ?? 0}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            temas.length > 0 && (
              <p className="stats-placeholder">
                {temaSeleccionado ? 'Este tema no tiene actividades' : 'Selecciona un tema'}
              </p>
            )
          )}

          {temas.length === 0 && (
            <p className="msg-vacio">Este curso aún no tiene temas</p>
          )}
        </>
      )}
    </>
  );

  return (
    <div className={embedded ? 'medias-embedded' : 'estadisticas-page'}>
      {!embedded && <NavbarMisCursos />}
      <main className="estadisticas-main">
        {!embedded && (
          <>
            <button className="btn-volver-pixel" onClick={() => navigate(-1)}>← Volver</button>
            <h1 className="estadisticas-titulo-curso">Puntuaciones Medias</h1>
          </>
        )}

        {/* When embedded, parent sidebar handles tema selection — render content only */}
        {embedded ? (
          <div className="estadisticas-yellow-card">
            {renderContent()}
          </div>
        ) : (
          <div className="stats-layout">
            <aside className="stats-sidebar">
              <button className="stats-sidebar-btn stats-sidebar-btn--refresh" onClick={cargarTodo}>
                Actualizar ↻
              </button>
              <hr className="stats-sidebar-divider" />
              <h3 className="stats-sidebar-title">Temas</h3>
              {loading && <p className="stats-sidebar-loading">Cargando...</p>}
              {!loading && temas.map(tema => (
                <button
                  key={tema.id}
                  className={`stats-sidebar-btn${temaSeleccionado?.id === tema.id ? ' stats-sidebar-btn--active' : ''}`}
                  onClick={() => setTemaSeleccionado(tema)}
                >
                  {tema.titulo}
                </button>
              ))}
            </aside>
            <div className="stats-content-area">
              <div className="estadisticas-yellow-card">
                {renderContent()}
              </div>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}