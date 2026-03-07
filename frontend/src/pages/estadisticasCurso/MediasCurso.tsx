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
  const [mapaNotas, setMapaNotas] = useState<Map<number, number>>(new Map());
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    cargarTodo();
  }, [id]);

  const cargarTodo = async () => {
    setLoading(true);
    setError('');
    try {
      const token = localStorage.getItem('token');
      const apiBase = (import.meta.env.VITE_API_URL ?? "").trim().replace(/\/$/, "");

      // Peticiones al backend
      const [resEstructura, resNotas] = await Promise.all([
        fetch(`${apiBase}/api/temas/curso/${id}/maestro`, {
          headers: { 'Authorization': `Bearer ${token}` }
        }),
        fetch(`${apiBase}/api/cursos/${id}/NotasMedias`, {
          headers: { 'Authorization': `Bearer ${token}` }
        })
      ]);

      if (!resEstructura.ok || !resNotas.ok) throw new Error("Error al obtener datos");
      
      const temasData: Tema[] = await resEstructura.json();
      const notasData: number[] = await resNotas.json(); // List<Integer> del backend

      // Sincronización: El backend calcula las notas recorriendo las actividades únicas
      // Creamos un mapa: ActividadID -> NotaMedia
      const nuevoMapa = new Map<number, number>();
      let notaIdx = 0;

      // Recorremos la estructura de temas para asignar las notas en orden
      temasData.forEach(tema => {
        tema.actividades.forEach(act => {
          if (notaIdx < notasData.length) {
            nuevoMapa.set(act.id, notasData[notaIdx]);
            notaIdx++;
          }
        });
      });

      setMapaNotas(nuevoMapa);
      setTemas(temasData);
      if (temasData.length > 0) setTemaSeleccionado(temasData[0]);

    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error de conexión');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="estadisticas-page">
      <NavbarMisCursos />
      <main className="estadisticas-main">
        <button className="btn-volver-pixel" onClick={() => navigate(-1)}>← Volver</button>
        <h1 className="estadisticas-titulo-curso">Medias del Curso</h1>

        {loading && <p className="msg-placeholder">Cargando datos...</p>}
        {error && <p className="msg-placeholder" style={{ color: 'red' }}>{error}</p>}

        <div className="layout-estadisticas">
          {/* PANEL IZQUIERDO: TEMAS */}
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
                    onKeyDown={(e) => { if (e.key === 'Enter' || e.key === ' ') setTemaSeleccionado(tema); }}
                    role="button"
                    tabIndex={0}
                  >
                    {tema.titulo}
                  </li>
                ))}
              </ul>
            )}
          </aside>

          {/* PANEL DERECHO: ACTIVIDADES */}
          <section className="panel-actividades">
            {temaSeleccionado ? (
              <>
                <h3>{temaSeleccionado.titulo}</h3>
                <table className="pixel-table">
                  <thead>
                    <tr>
                      <th>Nº</th>
                      <th>Actividad</th>
                      <th>Nota Media</th>
                    </tr>
                  </thead>
                  <tbody>
                    {temaSeleccionado.actividades.map((act, index) => (
                      <tr key={act.id}>
                        <td className="text-center">{index + 1}</td>
                        <td>{act.titulo}</td>
                        <td className="text-center font-bold">
                          {mapaNotas.get(act.id) ?? 0}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </>
            ) : (
              <p className="msg-placeholder">Selecciona un tema</p>
            )}
          </section>
        </div>

        <div className="botones-footer">
          <button className="btn-medias-pixel" onClick={cargarTodo}>Actualizar ↻</button>
        </div>
      </main>
    </div>
  );
}