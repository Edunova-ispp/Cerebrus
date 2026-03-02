import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import './EstadisticasCurso.css';

interface Actividad {
  id: number;
  titulo: string;
}

interface MediaActividad {
  actividadId: number;
  numero: number;
  titulo: string;
  notaMedia: number;
}

interface Tema {
  id: number;
  titulo: string;
  actividades: Actividad[];
}

export default function MediasCurso() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [cursoNombre, setCursoNombre] = useState('');
  const [temas, setTemas] = useState<Tema[]>([]);
  const [temaSeleccionado, setTemaSeleccionado] = useState<Tema | null>(null);
  const [mediasActividades, setMediasActividades] = useState<MediaActividad[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    cargarEstructuraInicial();
  }, [id]);

  // Cada vez que el usuario cambie de tema, cargamos las notas de ESE tema
  useEffect(() => {
    if (temaSeleccionado) {
      cargarNotasDelTema();
    }
  }, [temaSeleccionado]);

  const cargarEstructuraInicial = async () => {
    setLoading(true);
    setError('');
    try {
      const token = localStorage.getItem('token');
      const apiBase = (import.meta.env.VITE_API_URL ?? "").trim().replace(/\/$/, "");

      // 1. Obtener detalles del curso (para el título)
      const cursoRes = await fetch(`${apiBase}/api/cursos/${id}/detalles`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      const cursoData = await cursoRes.json();
      setCursoNombre(Array.isArray(cursoData) ? cursoData[0] : (cursoData.titulo || 'Curso'));

      // 2. Obtener los temas del maestro (usando el nuevo controlador DTO)
      const temasRes = await fetch(`${apiBase}/api/temas/curso/${id}/maestro`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (!temasRes.ok) throw new Error("No se pudieron cargar los temas");
      
      const temasData = await temasRes.json();
      const temasFormateados = temasData.map((t: any) => ({
        id: t.id,
        titulo: t.titulo,
        actividades: t.actividades || []
      }));

      setTemas(temasFormateados);
      if (temasFormateados.length > 0) {
        setTemaSeleccionado(temasFormateados[0]);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al cargar el curso');
    } finally {
      setLoading(false);
    }
  };

  const cargarNotasDelTema = async () => {
    if (!temaSeleccionado) return;
    try {
      const token = localStorage.getItem('token');
      const apiBase = (import.meta.env.VITE_API_URL ?? "").trim().replace(/\/$/, "");

      // Llamada al nuevo endpoint filtrado por temaId
      const notasRes = await fetch(`${apiBase}/api/cursos/${id}/NotasMedias/${temaSeleccionado.id}`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });

      if (!notasRes.ok) throw new Error("Error al obtener notas del tema");
      const notasArray: number[] = await notasRes.json();

      // Mapeamos las actividades del tema con las notas recibidas (que vienen en el mismo orden)
      const medias = temaSeleccionado.actividades.map((actividad, index) => ({
        actividadId: actividad.id,
        numero: index + 1,
        titulo: actividad.titulo,
        notaMedia: notasArray[index] ?? 0
      }));

      setMediasActividades(medias);
    } catch (err) {
      console.error("Error cargando notas:", err);
    }
  };

  if (loading) return (
    <div className="estadisticas-page">
      <NavbarMisCursos />
      <main className="estadisticas-main"><div className="estadisticas-loading">Cargando curso...</div></main>
    </div>
  );

  return (
    <div className="estadisticas-page">
      <NavbarMisCursos />
      <main className="estadisticas-main">
        <button className="btn-volver-pixel" onClick={() => navigate(-1)}>← Volver</button>

        <div className="medias-header">
          <h1 className="estadisticas-titulo-curso">
            {cursoNombre}
            {temaSeleccionado && <span className="tema-subtitle"> - {temaSeleccionado.titulo}</span>}
          </h1>
        </div>

        {error && <div className="stats-error-msg">{error}</div>}

        <div className="layout-medias">
          <aside className="panel-temas-pixel">
            <h3>Temas</h3>
            <ul className="lista-temas">
              {temas.map((tema) => (
                <li
                  key={tema.id}
                  className={`item-tema ${temaSeleccionado?.id === tema.id ? 'activo' : ''}`}
                  onClick={() => setTemaSeleccionado(tema)}
                >
                  {tema.titulo}
                </li>
              ))}
            </ul>
          </aside>

          <section className="panel-medias-pixel">
            {mediasActividades.length === 0 ? (
              <p className="msg-placeholder">No hay actividades puntuadas en este tema</p>
            ) : (
              <div className="table-scroll-container">
                <table className="pixel-table">
                  <thead>
                    <tr>
                      <th>Nª</th>
                      <th>Actividad</th>
                      <th>Nota Media</th>
                    </tr>
                  </thead>
                  <tbody>
                    {mediasActividades.map((m) => (
                      <tr key={m.actividadId}>
                        <td className="text-center">{m.numero}</td>
                        <td>{m.titulo}</td>
                        <td className="text-center">{m.notaMedia.toFixed(2)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </section>
        </div>

        <button className="btn-medias-pixel" onClick={cargarNotasDelTema}>Actualizar ↻</button>
      </main>
    </div>
  );
}