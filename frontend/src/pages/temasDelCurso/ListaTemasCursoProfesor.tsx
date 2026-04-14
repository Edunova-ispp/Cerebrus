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
  const [temaSeleccionado, setTemaSeleccionado] = useState<Tema | null>(null);
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
        if (lista.length > 0) setTemaSeleccionado(lista[0]);
      })
      .catch((e) => setError(e instanceof Error ? e.message : "Error cargando temas"))
      .finally(() => setLoading(false));
  }, [curso]);

  const navigate = useNavigate();

  const handleEliminarTema = async (temaId: number) => {
    try {
      await apiFetch(`${apiBase}/api/temas/${temaId}`, { method: 'DELETE' });
      // Elimina el tema del estado local
      setTemas(prev => {
        const nuevaLista = prev.filter(t => t.id !== temaId);
        // Si el tema borrado era el seleccionado, selecciona el primero
        if (temaSeleccionado?.id === temaId) {
          setTemaSeleccionado(nuevaLista[0] ?? null);
        }
        return nuevaLista;
      });
    } catch (e) {
      setError(e instanceof Error ? e.message : "Error al eliminar el tema");
    }
  };

  const handleEliminarActividad = async (actividadId: number) => {
  try {
    await apiFetch(`${apiBase}/api/actividades/delete/${actividadId}`, { method: 'DELETE' });
    setTemaSeleccionado(prev => {
      if (!prev) return prev;
      return {
        ...prev,
        actividades: prev.actividades?.filter(a => a.id !== actividadId) ?? []
      };
    });
  } catch (e) {
    setError(e instanceof Error ? e.message : "Error al eliminar la actividad");
  }
};

  const actividades = temaSeleccionado?.actividades ?? [];

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
          <div className="ltp-paneles">
            {/* Panel izquierdo: Temas */}
            <div className="ltp-panel">
              <div className="ltp-lista">
                {temas.length === 0 ? (
                  <p className="ltp-vacio">No hay temas todavía</p>
                ) : (
                  temas.map((tema) => (
                    <div
                      key={tema.id}
                      className={`ltp-item${
                        temaSeleccionado?.id === tema.id ? " ltp-item--activo" : ""
                      }`}
                      onClick={() => setTemaSeleccionado(tema)}
                      onKeyDown={(e) => { if (e.key === 'Enter' || e.key === ' ') setTemaSeleccionado(tema); }}
                      role="button"
                      tabIndex={0}
                    >
                      <span className="ltp-item-titulo">{tema.titulo}</span>
                      <div className="ltp-item-acciones">
                        <button
                          className="ltp-btn-icono"
                          title="Editar"
                          onClick={(e) => {
                            e.stopPropagation();
                            if (!isMaestro) return;
                            if (onEditarTema) {
                              onEditarTema(tema.id);
                            } else {
                              navigate(`/cursos/${id ?? curso?.id}/temas/${tema.id}/editar`);
                            }
                          }}
                        >
                          ✎
                        </button>
                        <button
                          className="ltp-btn-icono"
                          title="Borrar"
                          onClick={(e) => {
                            e.stopPropagation();
                            if (isMaestro) {
                              handleEliminarTema(tema.id);
                            }
                          }}
                        >
                          🗑
                        </button>
                      </div>
                    </div>
                  ))
                )}
              </div>
              <button
                className="ltp-btn-añadir"
                onClick={() => {
                  if (!isMaestro) return;
                  if (onCrearTema) {
                    onCrearTema();
                  } else {
                    navigate(`/cursos/${id ?? curso?.id}/temas/crear`);
                  }
                }}
              >
                + Añadir tema
              </button>
            </div>

            {/* Panel derecho: Actividades */}
<div className="ltp-panel">
  {temaSeleccionado ? (
    <>
      <div className="ltp-lista">
        {actividades.length === 0 ? (
          <p className="ltp-vacio">No hay actividades en este tema</p>
        ) : (
          actividades.map((act) => (
            <div key={act.id} className="ltp-item">
              <span className="ltp-item-titulo">{act.titulo}</span>
              <div className="ltp-item-acciones">
                <button 
                  className="ltp-btn-icono" 
                  title="Editar" 
                  onClick={(e) => { 
                    e.stopPropagation(); 
                    if (!isMaestro) return;
                    if (onEditarActividad) {
                      onEditarActividad(temaSeleccionado.id, act.id);
                    } else {
                      navigate(`/cursos/${id ?? curso?.id}/temas/${temaSeleccionado.id}/actividades/${act.id}/editar`);
                    }
                  }}
                >
                  ✎
                </button>
                <button 
                  className="ltp-btn-icono" 
                  title="Borrar" 
                  onClick={(e) => { 
                    e.stopPropagation(); 
                    if (isMaestro) {
                      handleEliminarActividad(act.id);
                    }
                  }}
                >
                  🗑
                </button>
              </div>
            </div>
          ))
        )}
      </div>
      <button 
        className="ltp-btn-añadir" 
        onClick={() => {
          if (!isMaestro) return;
          if (onCrearActividad) {
            onCrearActividad(temaSeleccionado.id);
          } else {
            navigate(`/cursos/${id ?? curso?.id}/temas/${temaSeleccionado.id}/actividades/crear`);
          }
        }}
      >
        + Añadir actividad
      </button>
    </>
  ) : (
    <div className="ltp-vacio-seleccion">
      <p className="ltp-vacio">Crea un tema para crear sus actividades</p>
    </div>
  )}
</div>
          </div>
        )}
      </main>
    </div>
  );
}
