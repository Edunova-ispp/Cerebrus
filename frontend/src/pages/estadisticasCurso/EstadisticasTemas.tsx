import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import './EstadisticasCurso.css';

interface Tema {
  id: number;
  titulo: string;
}

interface EstadisticasTemaDTO {
  temaCompletadoPorTodos: boolean | null;
  notaMediaTema: number | null;
  tiempoMedioTema: number | null;
  notaMaximaTema: number | null;
  notaMinimaTema: number | null;
}

export default function EstadisticasTemas() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [temas, setTemas] = useState<Tema[]>([]);
  const [mapaEstadisticas, setMapaEstadisticas] = useState<Map<number, EstadisticasTemaDTO>>(new Map());
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    cargarTodo();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  const notaMediaMostrada = (stats: EstadisticasTemaDTO | undefined) => {
    const valor = stats?.notaMediaTema;
    if (typeof valor !== 'number' || !Number.isFinite(valor)) return 0;
    return Math.round(valor * 100) / 100;
  };

  const formatearTiempoMinutos = (minutos: number | null | undefined): string => {
    if (typeof minutos !== 'number' || !Number.isFinite(minutos) || minutos <= 0) return '0 mins';
    const redondeado = Math.round(minutos);
    if (redondeado === 1) return '1 min';
    return `${redondeado} mins`;
  };

  const completadoPorTodosMostrado = (stats: EstadisticasTemaDTO | undefined) => {
    const valor = stats?.temaCompletadoPorTodos;
    if (valor === null || valor === undefined) return 'N/A';
    return valor ? 'Sí' : 'No';
  };

  const cargarTodo = async () => {
    setLoading(true);
    setError('');

    try {
      const token = localStorage.getItem('token');
      const apiBase = (import.meta.env.VITE_API_URL ?? '').trim().replace(/\/$/, '');

      const [resTemas, resStats] = await Promise.all([
        fetch(`${apiBase}/api/temas/curso/${id}/maestro`, {
          headers: { Authorization: `Bearer ${token}` },
        }),
        fetch(`${apiBase}/api/estadisticas/cursos/${id}/estadisticas-temas`, {
          headers: { Authorization: `Bearer ${token}` },
        }),
      ]);

      if (!resTemas.ok) throw new Error('Error al obtener la estructura del curso');
      if (!resStats.ok) throw new Error('Error al obtener estadísticas de temas');

      const temasData = await resTemas.json();
      // El endpoint /api/temas/curso/{id}/maestro devuelve Tema[] (con más campos). Aquí solo necesitamos id/titulo.
      const temasNormalizados: Tema[] = (temasData as any[]).map((t) => ({
        id: t.id,
        titulo: t.titulo,
      }));

      const statsData: Record<string, EstadisticasTemaDTO> = await resStats.json();
      const nuevoMapa = new Map<number, EstadisticasTemaDTO>();
      Object.entries(statsData).forEach(([temaId, stats]) => {
        nuevoMapa.set(Number(temaId), stats);
      });

      setTemas(temasNormalizados);
      setMapaEstadisticas(nuevoMapa);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error de conexión');
      setTemas([]);
      setMapaEstadisticas(new Map());
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="estadisticas-page">
      <NavbarMisCursos />
      <main className="estadisticas-main">
        <button className="btn-volver-pixel" onClick={() => navigate(-1)}>
          ←
        </button>
        <h1 className="estadisticas-titulo-curso">Temas del Curso</h1>

        {loading && <p className="msg-placeholder">Cargando datos...</p>}
        {error && (
          <p className="msg-placeholder" style={{ color: 'red' }}>
            {error}
          </p>
        )}

        {!loading && !error && (
          <section className="panel-actividades" style={{ width: '95%', maxWidth: '1100px' }}>
            {temas.length === 0 ? (
              <p className="msg-vacio">Este curso aun no tiene temas</p>
            ) : (
              <>
                <div className="table-scroll-container">
                  <table className="pixel-table">
                    <thead>
                      <tr>
                        <th>Nº</th>
                        <th>Tema</th>
                        <th>Nota Media</th>
                        <th>Nota Máx.</th>
                        <th>Nota Mín.</th>
                        <th>Tiempo Medio</th>
                        <th>Completado por todos</th>
                      </tr>
                    </thead>
                    <tbody>
                      {temas.map((tema, index) => {
                        const stats = mapaEstadisticas.get(tema.id);
                        return (
                          <tr key={tema.id}>
                            <td className="text-center">{index + 1}</td>
                            <td>{tema.titulo}</td>
                            <td className="text-center font-bold">{notaMediaMostrada(stats)}</td>
                            <td className="text-center font-bold">{stats?.notaMaximaTema ?? 0}</td>
                            <td className="text-center font-bold">{stats?.notaMinimaTema ?? 0}</td>
                            <td className="text-center font-bold">{formatearTiempoMinutos(stats?.tiempoMedioTema)}</td>
                            <td className="text-center font-bold">{completadoPorTodosMostrado(stats)}</td>
                          </tr>
                        );
                      })}
                    </tbody>
                  </table>
                </div>
              </>
            )}
          </section>
        )}

        <div className="botones-footer">
          <button className="btn-medias-pixel" onClick={cargarTodo}>
            Actualizar ↻
          </button>
          <button
            className="btn-medias-pixel"
            onClick={() => navigate(`/estadisticas/${id}/temas/graficas`)}
          >
            Gráficas
          </button>
        </div>
      </main>
    </div>
  );
}
