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

export default function MediasCurso() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  
  const [temas, setTemas] = useState<Tema[]>([]);
  const [temaSeleccionado, setTemaSeleccionado] = useState<Tema | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    cargarDatosEstructurales();
  }, [id]);

  const cargarDatosEstructurales = async () => {
    setLoading(true);
    setError('');
    try {
      const token = localStorage.getItem('token');
      const apiBase = (import.meta.env.VITE_API_URL ?? "").trim().replace(/\/$/, "");

      // Cargamos los temas y sus actividades usando el endpoint de maestro
      const res = await fetch(`${apiBase}/api/temas/curso/${id}/maestro`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });

      if (!res.ok) throw new Error("Error al obtener la estructura del curso");
      
      const temasData = await res.json();
      
      if (temasData && temasData.length > 0) {
        setTemas(temasData);
        setTemaSeleccionado(temasData[0]); // Seleccionamos el primero por defecto
      } else {
        setTemas([]);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al cargar datos');
    } finally {
      setLoading(false);
    }
  };

  if (loading) return (
    <div className="estadisticas-page">
      <NavbarMisCursos />
      <main className="estadisticas-main">
        <div className="stats-info-msg">Cargando estructura del curso...</div>
      </main>
    </div>
  );

  return (
    <div className="estadisticas-page">
      <NavbarMisCursos />
      
      <main className="estadisticas-main">
        <button className="btn-volver-pixel" onClick={() => navigate(-1)}>
          ← Volver
        </button>

        <h1 className="estadisticas-titulo-curso">Contenido del Curso</h1>

        {error && <div className="stats-error-msg">{error}</div>}

        <div className="layout-estadisticas">
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
                    onClick={() => setTemaSeleccionado(tema)}
                  >
                    {tema.titulo}
                  </li>
                ))}
              </ul>
            )}
          </aside>

          <section className="panel-actividades">
            {!temaSeleccionado ? (
              <div className="msg-placeholder">Selecciona un tema para ver sus actividades</div>
            ) : (
              <>
                <h3>Actividades de: {temaSeleccionado.titulo}</h3>
                {temaSeleccionado.actividades.length === 0 ? (
                  <p className="msg-placeholder">No hay actividades en este tema aún.</p>
                ) : (
                  <ul className="lista-actividades-simple">
                    {temaSeleccionado.actividades.map((act, index) => (
                      <li key={act.id} className="item-actividad-pixel">
                        <span className="badge-numero">{index + 1}</span>
                        {act.titulo}
                      </li>
                    ))}
                  </ul>
                )}
              </>
            )}
          </section>
        </div>

        {/* CONTENEDOR DE BOTONES CON SEPARACIÓN */}
        <div className="botones-footer">
          <button className="btn-medias-pixel" onClick={cargarDatosEstructurales}>
            Actualizar ↻
          </button>
        </div>
      </main>
    </div>
  );
}