import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import './CourseDetailPage.css';

interface CourseDetail {
  titulo: string;
  descripcion: string;
  imagen: string;
  codigo?: string;
}

const CourseDetailPage = () => {
  const { cursoId } = useParams();
  const navigate = useNavigate();
  const token = localStorage.getItem('token');

  const [curso, setCurso] = useState<CourseDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    cargarDetallesCurso();
  }, [cursoId]);

  const cargarDetallesCurso = async () => {
    try {
      if (!cursoId) {
        setError('ID de curso no válido');
        setLoading(false);
        return;
      }

      const response = await fetch(`http://localhost:8080/api/cursos/${parseInt(cursoId)}/detalles`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`,
        },
      });

      if (!response.ok) {
        throw new Error(`Error al obtener detalles: ${response.status}`);
      }

      const detalles: string[] = await response.json();
      
      const cursoData: CourseDetail = {
        titulo: detalles[0] || '',
        descripcion: detalles[1] || '',
        imagen: detalles[2] || '',
        codigo: detalles[3],
      };
      
      setCurso(cursoData);
      setLoading(false);
    } catch (err: any) {
      if (err.message.includes('404')) {
        setError('Curso no encontrado');
      } else if (err.message.includes('403')) {
        setError('No tienes permiso para ver este curso');
      } else {
        setError('Error al cargar los detalles del curso');
      }
      setLoading(false);
    }
  };

  const handleEditar = () => {
    navigate(`/curso/${cursoId}/editar`);
  };

  const handleVolver = () => {
    navigate('/cursos');
  };

  if (loading) {
    return <div className="page-container"><div className="loading">Cargando detalles del curso...</div></div>;
  }

  if (error) {
    return (
      <div className="page-container">
        <div className="error-message">{error}</div>
        <button className="btn-volver" onClick={handleVolver}>
          Volver a Cursos
        </button>
      </div>
    );
  }

  if (!curso) {
    return (
      <div className="page-container">
        <div className="error-message">No se pudo cargar el curso</div>
        <button className="btn-volver" onClick={handleVolver}>
          Volver a Cursos
        </button>
      </div>
    );
  }

  return (
    <div className="page-container">
      <div className="detail-header">
        <button className="btn-back" onClick={handleVolver}>
          ← Volver
        </button>
        <button className="btn-editar" onClick={handleEditar}>
          ✏️ Editar
        </button>
      </div>

      <div className="detail-content">
        {curso.imagen && (
          <div className="detail-image">
            <img src={curso.imagen} alt={curso.titulo} />
          </div>
        )}

        <div className="detail-info">
          <h1>{curso.titulo}</h1>
          
          {curso.codigo && (
            <div className="detail-code">
              <strong>Código del Curso:</strong> {curso.codigo}
            </div>
          )}

          {curso.descripcion && (
            <div className="detail-description">
              <h2>Descripción</h2>
              <p>{curso.descripcion}</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default CourseDetailPage;
