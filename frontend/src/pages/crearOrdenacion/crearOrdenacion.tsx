import { useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import { apiFetch } from '../../utils/api';
import './CrearOrdenacion.css';

const TIPOS = ['Teoría', 'Tipo test', 'Poner en orden'];

export default function CrearOrdenacion() {
  const [titulo, setTitulo] = useState('');
  const [descripcion, setDescripcion] = useState('');
  const [puntuacion, setPuntuacion] = useState('');
  const [imagen, setImagen] = useState('');
  const [respVisible, setRespVisible] = useState(false);
  const [comentariosRespVisible, setComentariosRespVisible] = useState('');
  const [posicion, setPosicion] = useState('');
  const [valoresRaw, setValoresRaw] = useState('');
  const [ordenItems, setOrdenItems] = useState<string[]>(['']);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const { id: cursoId, temaId: temaId } = useParams<{ id: string; temaId: string }>();

  const valores = useMemo(() => {
    return valoresRaw
      .split(/\r?\n/)
      .map((v) => v.trim())
      .filter(Boolean);
  }, [valoresRaw]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (!titulo.trim()) {
      setError('El título de la actividad de ordenación es requerido');
      return;
    }

    if (!puntuacion.trim()) {
      setError('La puntuación de la actividad de ordenación es requerida');
      return;
    }

    const puntuacionNum = Number.parseInt(puntuacion.trim(), 10);
    if (Number.isNaN(puntuacionNum)) {
      setError('La puntuación debe ser un número válido');
      return;
    }

    if (!temaId) {
      setError('Falta el id del tema en la URL');
      return;
    }

    const temaIdNum = Number.parseInt(temaId, 10);
    if (Number.isNaN(temaIdNum)) {
      setError('El id del tema debe ser un número válido');
      return;
    }

    if (!cursoId) {
      setError('Falta el id del curso en la URL');
      return;
    }

    const cursoIdNum = Number.parseInt(cursoId, 10);
    if (Number.isNaN(cursoIdNum)) {
      setError('El id del curso debe ser un número válido');
      return;
    }

    if (!posicion.trim()) {
      setError('La posición es requerida');
      return;
    }

    const posicionNum = Number.parseInt(posicion.trim(), 10);
    if (Number.isNaN(posicionNum)) {
      setError('La posición debe ser un número válido');
      return;
    }

    if (valores.length === 0) {
      setError('Debes añadir al menos un valor (uno por línea)');
      return;
    }

    setLoading(true);

    try {
      await apiFetch('/api/ordenaciones', {
        method: 'POST',
        body: JSON.stringify({
          titulo: titulo.trim(),
          descripcion: descripcion.trim() || '',
          puntuacion: puntuacionNum,
          imagen: imagen.trim() || '',
          tema: { id: temaIdNum },
          respVisible,
          comentariosRespVisible: respVisible ? (comentariosRespVisible.trim() || null) : null,
          posicion: posicionNum,
          valores,
        }),
      });

        alert('¡Actividad de ordenación creada exitosamente!');
        navigate(`/cursos/${cursoIdNum}/temas`);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al crear la actividad de ordenación');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="ca-page">
      <NavbarMisCursos />
      <main className="ca-main">
          <div className="ca-sidebar">
            <button className="ca-sidebar-btn" onClick={() => navigate(`/cursos/${cursoId}/temas`)}>
              Volver al Mapa
            </button>
            {TIPOS.map((tipo) => (
              <button key={tipo} className="ca-sidebar-btn">
                {tipo}
              </button>
            ))}
          </div>

          {/* Zona derecha */}
            <div className="ca-contenido">
              <p className="ca-proximamente">
                Selecciona un tipo de actividad
              </p>
                <form onSubmit={handleSubmit}>
                {error && <p>{error}</p>}

                {/* Recuadro superior */}
                <div
                  className="ca-contenedor-blanco"
                >
                  {/* Izquierda: título + descripción */}
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                    <div>
                      <label>Título</label>
                      <input
                        type="text"
                        id="titulo"
                        value={titulo}
                        onChange={(e) => setTitulo(e.target.value)}
                        className='.ca-contenedor'
                      />
                    </div>

                    <div>
                      <label>Descripción</label>
                      <textarea
                        id="descripcion"
                        value={descripcion}
                        onChange={(e) => setDescripcion(e.target.value)}
                        rows={3}
                        className='.ca-contenedor'
                      />
                    </div>
                  </div>

                  {/* Derecha: puntuación, correcciones visibles, IA */}
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                    <div>
                      <label>Puntuación</label>
                      <input
                        type="number"
                        id="puntuacion"
                        value={puntuacion}
                        onChange={(e) => setPuntuacion(e.target.value)}
                        className='.ca-contenedor'
                      />
                    </div>

                    <div>
                      <label>Imagen (URL)</label>
                      <input
                        type="text"
                        id="imagen"
                        value={imagen}
                        onChange={(e) => setImagen(e.target.value)}
                        className='.ca-contenedor'
                      />
                    </div>

                    <div>
                      <label>Posición</label>
                      <input
                        type="number"
                        id="posicion"
                        value={posicion}
                        onChange={(e) => setPosicion(e.target.value)}
                        className='.ca-contenedor'
                      />
                    </div>

                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                      <input
                        type="checkbox"
                        id="respVisible"
                        checked={respVisible}
                        onChange={(e) => setRespVisible(e.target.checked)}
                      />
                      <label htmlFor="respVisible">Correcciones visibles</label>
                    </div>

                    {respVisible && (
                      <div>
                        <label>Comentarios</label>
                        <input
                          type="text"
                          id="comentariosRespVisible"
                          value={comentariosRespVisible}
                          onChange={(e) => setComentariosRespVisible(e.target.value)}
                          className='.ca-contenedor'
                        />
                      </div>
                    )}

                    <button type="button">Generar con IA</button>
                  </div>
                </div>

                {/* Recuadro inferior: orden */}
                <div
                  className="ca-contenedor-blanco"
                >
                  {/* Selector palabras / imágenes */}
                  <div style={{ display: 'flex', gap: '12px' }}>
                    <button type="button">Palabras</button>
                    <button type="button">Imágenes</button>
                  </div>

                  {/* Inputs ordenados */}
                  <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px' }}>
                    {ordenItems.map((v, i) => (
                      <div key={i} style={{ border: '1px solid black', padding: '8px' }}>
                        <input
                          type="text"
                          placeholder={`Elemento ${i + 1}`}
                          value={v}
                          onChange={(e) => {
                            const copia = [...ordenItems];
                            copia[i] = e.target.value;
                            setOrdenItems(copia);
                            setValoresRaw(copia.join('\n'));
                          }}
                          onKeyDown={(e) => {
                            if (e.key === 'Backspace' && v === '' && ordenItems.length > 1) {
                              const copia = ordenItems.filter((_, idx) => idx !== i);
                              setOrdenItems(copia);
                              setValoresRaw(copia.join('\n'));
                            }
                          }}
                        />
                      </div>
                    ))}

                    <button
                      type="button"
                      onClick={() => {
                        const nuevo = [...ordenItems, ''];
                        setOrdenItems(nuevo);
                        setValoresRaw(nuevo.join('\n'));
                      }}
                    >
                      +
                    </button>
                  </div>
                </div>

                {/* Botón guardar */}
                <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
                  {loading ? (
                    <button type="button" disabled>
                      Guardando...
                    </button>
                  ) : (
                    <button type="submit" className="ca-btn-guardar">Guardar</button>
                  )}
                </div>
              </form>
            </div>
      </main>
    </div>
  );
}