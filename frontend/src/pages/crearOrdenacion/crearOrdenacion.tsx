import { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import { apiFetch } from '../../utils/api';

export default function CrearOrdenacion() {
  const [titulo, setTitulo] = useState('');
  const [descripcion, setDescripcion] = useState('');
  const [puntuacion, setPuntuacion] = useState('');
  const [imagen, setImagen] = useState('');
  const [respVisible, setRespVisible] = useState(false);
  const [comentariosRespVisible, setComentariosRespVisible] = useState('');
  const [temaId, setTemaId] = useState('');
  const [posicion, setPosicion] = useState('');
  const [valoresRaw, setValoresRaw] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

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

    if (!temaId.trim()) {
      setError('El id del tema es requerido');
      return;
    }

    if (!posicion.trim()) {
      setError('La posición es requerida');
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
          puntuacion: parseInt(puntuacion.trim(), 10) || 0,
          imagen: imagen.trim() || '',
          tema: { id: parseInt(temaId.trim(), 10) },
          respVisible: respVisible,
          comentariosRespVisible: respVisible ? (comentariosRespVisible.trim() || '') : null,
          posicion: parseInt(posicion.trim(), 10),
          valores,
        }),
      });

        alert('¡Actividad de ordenación creada exitosamente!');
        navigate('/misCursos');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al crear la actividad de ordenación');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-100">
      <NavbarMisCursos />
      <div className="container mx-auto px-4 py-8">
        <h1 className="text-3xl font-bold mb-6">Crear Actividad de Ordenación</h1>
        <form onSubmit={handleSubmit} className="bg-white p-6 rounded-lg shadow-md">
          {error && <p className="text-red-500 mb-4">{error}</p>}
          <div className="mb-4">
            <label htmlFor="titulo" className="block text-sm font-medium text-gray-700">Título</label>
            <input
              type="text"
              id="titulo"
              value={titulo}
              onChange={(e) => setTitulo(e.target.value)}
              className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm p-2"
            />
          </div>
          <div className="mb-4">
            <label htmlFor="descripcion" className="block text-sm font-medium text-gray-700">Descripción</label>
            <textarea
              id="descripcion"
              value={descripcion}
              onChange={(e) => setDescripcion(e.target.value)}
              className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm p-2"
            />
          </div>
          <div className="mb-4">
            <label htmlFor="puntuacion" className="block text-sm font-medium text-gray-700">Puntuación</label>
            <input
              type="number"
              id="puntuacion"
              value={puntuacion}
              onChange={(e) => setPuntuacion(e.target.value)}
              className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm p-2"
            />
          </div>
          <div className="mb-4">
            <label htmlFor="imagen" className="block text-sm font-medium text-gray-700">Imagen (URL)</label>
            <input
              type="text"
              id="imagen"
              value={imagen}
              onChange={(e) => setImagen(e.target.value)}
              className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm p-2"
            />
          </div>
          <div className="mb-4">
            <label htmlFor="temaId" className="block text-sm font-medium text-gray-700">Id del tema</label>
            <input
              type="number"
              id="temaId"
              value={temaId}
              onChange={(e) => setTemaId(e.target.value)}
              className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm p-2"
            />
          </div>
          <div className="mb-4">
            <label htmlFor="posicion" className="block text-sm font-medium text-gray-700">Posición</label>
            <input
              type="number"
              id="posicion"
              value={posicion}
              onChange={(e) => setPosicion(e.target.value)}
              className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm p-2"
            />
          </div>
          <div className="mb-4">
            <label htmlFor="valores" className="block text-sm font-medium text-gray-700">Valores (uno por línea)</label>
            <textarea
              id="valores"
              value={valoresRaw}
              onChange={(e) => setValoresRaw(e.target.value)}
              className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm p-2"
              rows={6}
            />
          </div>
          <div className="mb-4">
            <label htmlFor="respVisible" className="block text-sm font-medium text-gray-700">¿Respuesta visible?</label>
            <input
              type="checkbox"
              id="respVisible"
              checked={respVisible}
              onChange={(e) => setRespVisible(e.target.checked)}
              className=""
            />
          </div>
          {respVisible && (
            <div className="mb-4">
              <label htmlFor="comentariosRespVisible" className="block text-sm font-medium text-gray-700">Comentarios de respuesta visibles</label>
              <input
                type="text"
                id="comentariosRespVisible"
                value={comentariosRespVisible}
                onChange={(e) => setComentariosRespVisible(e.target.value)}
                className={`mt-1 block w-full border ${error ? 'border-red-500' : 'border-gray-300'} rounded-md shadow-sm p-2`}
              />
            </div>
          )}
          {loading ? (
            <button type='button' disabled>Loading...</button>
          ) : (
            <button type='submit' disabled={loading} className='bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded'>
                Crear Actividad de Ordenación
            </button>
          )}
        </form>
      </div>
    </div>
  );
}