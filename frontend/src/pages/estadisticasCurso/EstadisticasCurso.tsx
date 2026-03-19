import React, { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import './EstadisticasCurso.css';

interface EstadisticaAlumno {
  nombre: string;
  idAlumno?: number; 
  puntos: number;
  actividadesRealizadas: number;
  tiempoTotal: number;
}

interface OpcionDesplegable {
  id: number;
  nombre: string;
}

interface EstadisticasCursoDTO {
  cursoCompletadoPorTodos: boolean | null;
  notaMediaCurso: number | null;
  tiempoMedioCurso: number | null;
  notaMaximaCurso: number | null;
  notaMinimaCurso: number | null;
}

function formatearTiempo(minutosTotales: number): string {
  if (!minutosTotales || minutosTotales === 0) return '0 mins';
  if (minutosTotales === 1) return '1 min';
  return `${minutosTotales} mins`;
}

function formatearNumero2Dec(valor: number | null | undefined): string {
  if (typeof valor !== 'number' || !Number.isFinite(valor)) return '0';
  return String(Math.round(valor * 100) / 100);
}

function formatearTiempoCurso(minutos: number | null | undefined): string {
  if (typeof minutos !== 'number' || !Number.isFinite(minutos) || minutos <= 0) return '0 mins';
  const redondeado = Math.round(minutos);
  return formatearTiempo(redondeado);
}

export default function EstadisticasCurso() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [estadisticas, setEstadisticas] = useState<EstadisticaAlumno[]>([]);
  const [estadisticasCurso, setEstadisticasCurso] = useState<EstadisticasCursoDTO | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [modalAbierto, setModalAbierto] = useState<'tema' | 'actividad' | null>(null);
  const [opcionesDisponibles, setOpcionesDisponibles] = useState<OpcionDesplegable[]>([]);
  const [opcionSeleccionada, setOpcionSeleccionada] = useState<string>('');
  const [cargandoOpciones, setCargandoOpciones] = useState(false);

  useEffect(() => {
    cargarEstadisticas();
  }, [id]);

  const cargarEstadisticas = async () => {
    setLoading(true);
    setError('');
    try {
      const token = localStorage.getItem('token');
      const apiBase = (import.meta.env.VITE_API_URL ?? "").trim().replace(/\/$/, "");

      // Hacemos 3 llamadas: Puntos, Actividades y la lista completa de tiempos (con límite 1000 para que vengan todos)
      const [puntosRes, actividadesRes, tiemposRes, cursoRes] = await Promise.all([
        fetch(`${apiBase}/api/estadisticas/cursos/${id}/puntos`, { headers: { 'Authorization': `Bearer ${token}` } }),
        fetch(`${apiBase}/api/estadisticas/cursos/${id}/actividades-completadas`, { headers: { 'Authorization': `Bearer ${token}` } }),
        fetch(`${apiBase}/api/estadisticas/cursos/${id}/alumnos-rapidos-lentos?limite=1000`, { headers: { 'Authorization': `Bearer ${token}` } }),
        fetch(`${apiBase}/api/estadisticas/cursos/${id}/estadisiticas-curso`, { headers: { 'Authorization': `Bearer ${token}` } }),
      ]);

      if (!puntosRes.ok || !actividadesRes.ok) throw new Error('Error al cargar datos principales');

      const puntosData = await puntosRes.json();
      const actividadesData = await actividadesRes.json();
      const tiemposData = tiemposRes.ok ? await tiemposRes.json() : null;
      const cursoData: EstadisticasCursoDTO | null = cursoRes.ok ? await cursoRes.json() : null;

      const alumnosMap = new Map<string, EstadisticaAlumno>();

      // 1. Procesar puntos
      Object.entries(puntosData).forEach(([nombre, puntos]) => {
        alumnosMap.set(nombre, { nombre, puntos: puntos as number, actividadesRealizadas: 0, tiempoTotal: 0 });
      });

      // 2. Procesar actividades completadas
      Object.entries(actividadesData).forEach(([nombre, acts]) => {
        if (!alumnosMap.has(nombre)) {
          alumnosMap.set(nombre, { nombre, puntos: 0, actividadesRealizadas: 0, tiempoTotal: 0 });
        }
        alumnosMap.get(nombre)!.actividadesRealizadas = acts as number;
      });

      // 3. Procesar tiempos desde el endpoint de tu compañero
      if (tiemposData && tiemposData.masRapidos) {
        tiemposData.masRapidos.forEach((t: any) => {
          if (!alumnosMap.has(t.nombreAlumno)) {
            alumnosMap.set(t.nombreAlumno, { nombre: t.nombreAlumno, puntos: 0, actividadesRealizadas: 0, tiempoTotal: 0 });
          }
          // El backend de tu compañero nos da el tiempo exacto en minutos
          alumnosMap.get(t.nombreAlumno)!.tiempoTotal = t.tiempoMinutos || 0;
          alumnosMap.get(t.nombreAlumno)!.idAlumno = t.alumnoId;
        });
      }

      setEstadisticas(Array.from(alumnosMap.values()));
      setEstadisticasCurso(cursoData);

    } catch (err) {
      setError((err as Error).message || 'Error cargando las estadísticas');
      setEstadisticasCurso(null);
    } finally {
      setLoading(false);
    }
  };

  const metricasTiempo = useMemo(() => {
    if (estadisticas.length === 0) return null;
    const alumnosConTiempo = estadisticas.filter(est => est.tiempoTotal > 0);
    if (alumnosConTiempo.length === 0) return null;

    let totalMinutos = 0;
    let masRapido = alumnosConTiempo[0];
    let masLento = alumnosConTiempo[0];

    alumnosConTiempo.forEach(est => {
      totalMinutos += est.tiempoTotal;
      if (est.tiempoTotal < masRapido.tiempoTotal) masRapido = est;
      if (est.tiempoTotal > masLento.tiempoTotal) masLento = est;
    });

    return { media: totalMinutos / alumnosConTiempo.length, masRapido, masLento };
  }, [estadisticas]);

  // --- LÓGICA DEL MODAL DE SELECCIÓN CON EL ENDPOINT DEL MAESTRO ---
  const abrirModal = async (tipo: 'tema' | 'actividad') => {
    setModalAbierto(tipo);
    setCargandoOpciones(true);
    setOpcionSeleccionada('');
    setOpcionesDisponibles([]);

    try {
      const token = localStorage.getItem('token');
      const apiBase = (import.meta.env.VITE_API_URL ?? "").trim().replace(/\/$/, "");
      
      const res = await fetch(`${apiBase}/api/temas/curso/${id}/maestro`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      
      if (!res.ok) throw new Error('No se pudo obtener la lista de temas');
      const temasData = await res.json(); 

let opcionesNuevas: OpcionDesplegable[] = [];

      if (tipo === 'tema') {
        opcionesNuevas = temasData.map((t: any) => {
          // Magia aquí: si viene anidado usa t.tema, si viene plano usa t directamente
          const temaObj = t.tema || t; 
          return {
            id: temaObj.id,
            nombre: temaObj.titulo || temaObj.nombre || `Tema ${temaObj.id}`
          };
        });
     } else {
  const acts: OpcionDesplegable[] = [];
  temasData.forEach((t: any) => {
    const listaActs = t.actividades || t.actividadesDTO || [];
    
    listaActs.forEach((a: any) => {
      const actObj = a.actividad || a;
      const tipoAct = (actObj.tipo || "").toUpperCase();
      const tituloAct = (actObj.titulo || actObj.nombre || "").toLowerCase();
      
      const esTeoria = tipoAct === 'TEORIA' || tituloAct.includes('teoría') || tituloAct.includes('teoria');

      if (!esTeoria) {
        acts.push({
          id: actObj.id,
          nombre: actObj.titulo || actObj.nombre || `Actividad ${actObj.id}`
        });
      }
    });
  });
  opcionesNuevas = acts;
}

      setOpcionesDisponibles(opcionesNuevas);
      if (opcionesNuevas.length > 0) {
        setOpcionSeleccionada(opcionesNuevas[0].id.toString());
      }
    } catch (err) {
      console.error(err);
      alert("No se pudo cargar la lista. Revisa la consola y la URL del fetch.");
      setModalAbierto(null);
    } finally {
      setCargandoOpciones(false);
    }
  };

  const confirmarNavegacion = () => {
    if (!opcionSeleccionada) return;
    if (modalAbierto === 'tema') {
      navigate(`/estadisticas/temas/${opcionSeleccionada}`);
    } else if (modalAbierto === 'actividad') {
      navigate(`/estadisticas/actividades/${opcionSeleccionada}`);
    }
    setModalAbierto(null);
  };

  const cursoIndicadoresContent = !loading && !error ? (
    <div className="curso-indicadores" style={{ marginBottom: '16px' }}>
      <div className="curso-indicador">
        <div className="curso-indicador-label">Nota media</div>
        <div className="curso-indicador-value">{formatearNumero2Dec(estadisticasCurso?.notaMediaCurso)}</div>
      </div>
      <div className="curso-indicador">
        <div className="curso-indicador-label">Nota máx.</div>
        <div className="curso-indicador-value">{estadisticasCurso?.notaMaximaCurso ?? 0}</div>
      </div>
      <div className="curso-indicador">
        <div className="curso-indicador-label">Nota mín.</div>
        <div className="curso-indicador-value">{estadisticasCurso?.notaMinimaCurso ?? 0}</div>
      </div>
      <div className="curso-indicador">
        <div className="curso-indicador-label">Tiempo medio</div>
        <div className="curso-indicador-value">{formatearTiempoCurso(estadisticasCurso?.tiempoMedioCurso)}</div>
      </div>
    </div>
  ) : null;

  let estadisticasContent: React.ReactNode;
  if (loading) {
    estadisticasContent = <div className="stats-info-msg">Cargando...</div>;
  } else if (error) {
    estadisticasContent = <div className="stats-error-msg">{error}</div>;
  } else {
    estadisticasContent = (
      <>
        {metricasTiempo && (
          <div className="estadisticas-tiempo-resumen" style={{ display: 'flex', justifyContent: 'space-around', marginBottom: '20px', padding: '15px', backgroundColor: 'rgba(255,255,255,0.5)', borderRadius: '8px' }}>
            <div className="text-center">
              <strong>Tiempo medio</strong><br/>
              {formatearTiempo(metricasTiempo.media)}
            </div>
            <div className="text-center">
              <strong>Alumno más rápido</strong><br/>
              {metricasTiempo.masRapido.nombre} ({formatearTiempo(metricasTiempo.masRapido.tiempoTotal)})
            </div>
            <div className="text-center">
              <strong>Alumno más lento</strong><br/>
              {metricasTiempo.masLento.nombre} ({formatearTiempo(metricasTiempo.masLento.tiempoTotal)})
            </div>
          </div>
        )}

        <div className="table-scroll-container">
          <table className="pixel-table">
            <thead>
              <tr>
                <th>Alumno</th>
                <th>Puntos</th>
                <th>Nº actividades</th>
                <th>Tiempo Total</th>
              </tr>
            </thead>
            <tbody>
              {estadisticas.map((stat, index) => (
                <tr key={index}>
                  <td>{stat.nombre}</td>
                  <td className="text-center">{stat.puntos}</td>
                  <td className="text-center">{stat.actividadesRealizadas}</td>
                  <td className="text-center">{formatearTiempo(stat.tiempoTotal)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </>
    );
  }

return (
    <div className="estadisticas-page">
      <NavbarMisCursos />
      
      <main className="estadisticas-main" style={{ maxWidth: '1200px' }}>
        {/* Botón Volver arriba a la izquierda */}
        <div style={{ width: '100%', display: 'flex', marginBottom: '10px' }}>
          <button className="btn-volver-pixel" onClick={() => navigate(-1)}>
            ← Volver
          </button>
        </div>

        <h1 className="estadisticas-titulo-curso">Estadísticas del Curso</h1>

        {/* --- CONTENEDOR PRINCIPAL EN PARALELO --- */}
        <div style={{ 
          display: 'flex', 
          flexDirection: 'row', 
          width: '100%', 
          gap: '30px', 
          alignItems: 'flex-start',
          marginTop: '20px'
        }}>
          
          {/* COLUMNA IZQUIERDA: Botones de navegación y Actualizar */}
          <div style={{ 
            display: 'flex', 
            flexDirection: 'column', 
            gap: '20px', 
            minWidth: '250px' 
          }}>
            <button 
              className="btn-medias-pixel" 
              onClick={cargarEstadisticas}
              style={{ width: '100%', backgroundColor: '#FFF' }}
            >
              Actualizar ↻
            </button>
            
            <hr style={{ width: '100%', border: '1px solid #ccc' }} />

            <button className="btn-medias-pixel" onClick={() => navigate(`/estadisticas/${id}/actividades`)} style={{ width: '100%' }}>
              Actividades
            </button>
            <button className="btn-medias-pixel" onClick={() => navigate(`/estadisticas/${id}/temas`)} style={{ width: '100%' }}>
              Temas
            </button>
            <button className="btn-medias-pixel" onClick={() => abrirModal('tema')} style={{ width: '100%', backgroundColor: '#4a90e2', color: 'white' }}>
              Tiempos por Tema
            </button>
            <button className="btn-medias-pixel" onClick={() => abrirModal('actividad')} style={{ width: '100%', backgroundColor: '#e67e22', color: 'white' }}>
              Tiempos por Actividad
            </button>
          </div>
          <div style={{ flex: 1, margin: 0, width: 'auto', display: 'flex', flexDirection: 'column', gap: '12px' }}>
            {cursoIndicadoresContent}
            <div className="estadisticas-yellow-card" style={{ flex: 1, margin: 0, width: 'auto' }}>
              {estadisticasContent}
            </div>
          </div>

        </div>
      </main>
      {modalAbierto && (
        <div className="modal-overlay" style={{ position: 'fixed', top: 0, left: 0, width: '100%', height: '100%', backgroundColor: 'rgba(0,0,0,0.6)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1000 }}>
          <div className="modal-content" style={{ backgroundColor: '#fff', padding: '20px', borderRadius: '8px', border: '4px solid #333', minWidth: '300px' }}>
            <h2 style={{ marginTop: 0 }}>
              Selecciona {modalAbierto === 'tema' ? 'un Tema' : 'una Actividad'}
            </h2>
            
            {cargandoOpciones ? (
              <p>Cargando opciones...</p>
            ) : opcionesDisponibles.length === 0 ? (
              <p>No hay datos.</p>
            ) : (
              <select 
                value={opcionSeleccionada} 
                onChange={(e) => setOpcionSeleccionada(e.target.value)}
                style={{ width: '100%', padding: '10px', fontSize: '16px', marginBottom: '20px' }}
              >
                {opcionesDisponibles.map(op => (
                  <option key={op.id} value={op.id}>{op.nombre}</option>
                ))}
              </select>
            )}

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
              <button className="btn-medias-pixel" onClick={() => setModalAbierto(null)} style={{ backgroundColor: '#ccc' }}>Cancelar</button>
              <button className="btn-medias-pixel" onClick={confirmarNavegacion} disabled={!opcionSeleccionada || cargandoOpciones}>Ver</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}