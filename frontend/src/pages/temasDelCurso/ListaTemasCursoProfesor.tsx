import { useNavigate, useParams } from "react-router-dom";
import { useEffect, useState } from "react";
import NavbarMisCursos from "../../components/NavbarMisCursos/NavbarMisCursos";
import { apiFetch } from "../../utils/api";
import { getCurrentUserRoles, type Curso, type Tema } from "../../types/curso";
import "./ListaTemasCursoProfesor.css";

interface Props {
  readonly curso?: Curso;
  readonly embedded?: boolean;
  readonly onCrearTema?: () => void;
  readonly onEditarTema?: (temaId: number) => void;
  readonly onCrearActividad?: (temaId: number) => void;
  readonly onEditarActividad?: (temaId: number, actividadId: number) => void;
}

export default function ListaTemasCursoProfesor({ curso: cursoProp, embedded, onCrearTema, onEditarTema, onCrearActividad, onEditarActividad }: Props) {
  const { id } = useParams<{ id: string }>(); // ← lee el id de la URL
  const [curso, setCurso] = useState<Curso | null>(cursoProp ?? null);
  const [temas, setTemas] = useState<Tema[]>([]);
  const [expandedTemas, setExpandedTemas] = useState<Set<number>>(new Set());
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const apiBase = (import.meta.env.VITE_API_URL ?? "").trim().replace(/\/$/, "");
  const isMaestro = getCurrentUserRoles().some((r) =>
    r.toUpperCase().includes("MAESTRO")
  );

  // Si no llegó curso como prop, lo carga por el id de la URL
  useEffect(() => {
    if (cursoProp) {
      setCurso(cursoProp);
      return;
    }
    if (!id) return;
    setLoading(true);
    apiFetch(`${apiBase}/api/cursos`)
      .then((r) => r.json())
      .then((data: Curso[]) => {
        const encontrado = data.find((c) => String(c.id) === id) ?? null;
        if (!encontrado) {
          setError("Curso no encontrado o no pertenece al maestro");
        }
        setCurso(encontrado);
      })
      .catch(() => setError("Error cargando el curso"))
      .finally(() => setLoading(false));
  }, [id, cursoProp]);

  // Carga temas cuando ya tenemos el curso
  useEffect(() => {
    if (!curso) return;
    setLoading(true);
    setError(null);
    apiFetch(`${apiBase}/api/temas/curso/${curso.id}/maestro`)
      .then((r) => r.json())
      .then((data) => {
        const lista: Tema[] = Array.isArray(data) ? data : [];
        setTemas(lista);
      })
      .catch((e) => setError(e instanceof Error ? e.message : "Error cargando temas"))
      .finally(() => setLoading(false));
  }, [curso]);

  const toggleExpanded = (temaId: number) => {
    setExpandedTemas(prev => {
      const newSet = new Set(prev);
      if (newSet.has(temaId)) {
        newSet.delete(temaId);
      } else {
        newSet.add(temaId);
      }
      return newSet;
    });
  };

  const navigate = useNavigate();

  const handleEliminarTema = async (temaId: number) => {
    try {
      await apiFetch(`${apiBase}/api/temas/${temaId}`, { method: 'DELETE' });
      // Elimina el tema del estado local
      setTemas(prev => prev.filter(t => t.id !== temaId));
      setExpandedTemas(prev => {
        const newSet = new Set(prev);
        newSet.delete(temaId);
        return newSet;
      });
    } catch (e) {
      setError(e instanceof Error ? e.message : "Error al eliminar el tema");
    }
  };

  const handleEliminarActividad = async (actividadId: number) => {
    try {
      await apiFetch(`${apiBase}/api/actividades/delete/${actividadId}`, { method: 'DELETE' });
      setTemas(prev => prev.map(tema => ({
        ...tema,
        actividades: tema.actividades?.filter(a => a.id !== actividadId) ?? []
      })));
    } catch (e) {
      setError(e instanceof Error ? e.message : "Error al eliminar la actividad");
    }
  };

  const actividades = (temaId: number) => {
    const tema = temas.find(t => t.id === temaId);
    return tema?.actividades ?? [];
  };

  return (
    <div className={embedded ? 'ltp-embedded' : 'ltp-page'}>
      {!embedded && <NavbarMisCursos />}

      <main className="ltp-main">
        {!embedded && (
          <button className="ltp-volver" onClick={() => navigate(`/cursos/${id ?? curso?.id}`)}>
            ← 
          </button>
        )}

        {!embedded && <h1 className="ltp-titulo">{curso?.titulo}</h1>}

        {loading && <p className="ltp-estado">Cargando temas...</p>}
        {error && <p className="ltp-estado ltp-estado--error">{error}</p>}

        {!loading && !error && (
          <div className="ltp-temas-lista">
            {temas.length === 0 ? (
              <p className="ltp-vacio">No hay temas todavía</p>
            ) : (
              temas.map((tema) => {
                const isExpanded = expandedTemas.has(tema.id);
                const actividadesTema = actividades(tema.id);
                return (
                  <div key={tema.id} className="ltp-tema-bloque">
                    {/* Header del tema */}
                    <div className="ltp-tema-header">
                      <button
                        className="ltp-tema-expandir"
                        onClick={() => toggleExpanded(tema.id)}
                        title={isExpanded ? "Contraer" : "Expandir"}
                      >
                        <span className={`ltp-tema-icono ${isExpanded ? 'expandido' : ''}`}>▶</span>
                        <span className="ltp-tema-titulo">{tema.titulo}</span>
                      </button>
                      <div className="ltp-tema-acciones">
                        <button className="ltp-btn-icono" title="Editar" onClick={(e) => { e.stopPropagation(); isMaestro && (onEditarTema ? onEditarTema(tema.id) : navigate(`/cursos/${id ?? curso?.id}/temas/${tema.id}/editar`)); }}>✎</button>
                        <button className="ltp-btn-icono" title="Borrar" onClick={(e) => { e.stopPropagation(); isMaestro ? handleEliminarTema(tema.id) : undefined; }}>🗑</button>
                      </div>
                    </div>

                    {/* Contenido desplegable */}
                    {isExpanded && (
                      <div className="ltp-tema-contenido">
                        {actividadesTema.length === 0 ? (
                          <p className="ltp-vacio-actividades">No hay actividades en este tema</p>
                        ) : (
                          <div className="ltp-actividades-lista">
                            {actividadesTema.map((act) => (
                              <div
                                key={act.id}
                                className="ltp-actividad-item"
                                onClick={() => isMaestro && (onEditarActividad ? onEditarActividad(tema.id, act.id) : navigate(`/cursos/${id ?? curso?.id}/temas/${tema.id}/actividades/${act.id}/editar`))}
                                onKeyDown={(e) => { if ((e.key === 'Enter' || e.key === ' ') && isMaestro) (onEditarActividad ? onEditarActividad(tema.id, act.id) : navigate(`/cursos/${id ?? curso?.id}/temas/${tema.id}/actividades/${act.id}/editar`)); }}
                                role="button"
                                tabIndex={0}
                              >
                                <span className="ltp-actividad-titulo">{act.titulo}</span>
                                <div className="ltp-actividad-acciones">
                                  <button
                                    className="ltp-btn-icono-sm"
                                    title="Borrar"
                                    onClick={(e) => {
                                      e.stopPropagation();
                                      isMaestro ? handleEliminarActividad(act.id) : undefined;
                                    }}
                                  >
                                    🗑
                                  </button>
                                </div>
                              </div>
                            ))}
                          </div>
                        )}
                        <button
                          className="ltp-btn-añadir-actividad"
                          onClick={() => isMaestro && (onCrearActividad ? onCrearActividad(tema.id) : navigate(`/cursos/${id ?? curso?.id}/temas/${tema.id}/actividades/crear`))}
                        >
                          + Añadir actividad
                        </button>
                      </div>
                    )}
                  </div>
                );
              })
            )}
            <button className="ltp-btn-añadir" onClick={() => isMaestro && (onCrearTema ? onCrearTema() : navigate(`/cursos/${id ?? curso?.id}/temas/crear`))}>+ Añadir tema</button>
          </div>
        )}
      </main>
    </div>
  );
}
