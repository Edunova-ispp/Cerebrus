import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './CourseListingPage.css';

interface Curso {
    id?: number;
    titulo: string;
    descripcion?: string;
    imagen?: string;
    codigo?: string;
    visibilidad?: boolean;
}

const CourseListingPage = () => {
  const [cursos, setCursos] = useState<Curso[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    const cargarCursos = async () => {
      try {
        const token = localStorage.getItem('token');
        const rol = localStorage.getItem('role');

        if (!token || !rol) {
          navigate('/auth/login');
          return;
        }

        const response = await fetch('http://localhost:8080/api/cursos', {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`,
          },
        });

        if (!response.ok) {
          throw new Error(`Error al obtener cursos: ${response.status}`);
        }

        const cursosData: Curso[] = await response.json();
        setCursos(cursosData);
        setLoading(false);
      } catch (err) {
        setError('Error al cargar los cursos. Intenta de nuevo más tarde.');
        setLoading(false);
      }
    };

    cargarCursos();
  }, [navigate]);

  const handleCrearCurso = () => {
    navigate('/curso/crear');
  };

  const handleVerCurso = (cursoId: number) => {
    navigate(`/curso/${cursoId}`);
  };

  if (loading) {
    return <div className="page-container"><div className="loading">Cargando cursos...</div></div>;
  }

  return (
    <div className="page-container">
      <div className="courses-header">
        <h1>Mis Cursos</h1>
        <button className="btn-crear-curso" onClick={handleCrearCurso}>
          + Crear Nuevo Curso
        </button>
      </div>

      {error && <div className="error-message">{error}</div>}

      {cursos.length === 0 ? (
        <div className="no-courses-container">
          <div className="no-courses-message">
            <p>No tienes cursos creados</p>
            <button className="btn-crear-curso-secondary" onClick={handleCrearCurso}>
              Crea tu primer curso
            </button>
          </div>
        </div>
      ) : (
        <div className="courses-grid">
          {cursos.map((curso) => (
            <div
              key={curso.id}
              className="course-card"
              onClick={() => handleVerCurso(curso.id!)}
            >
              {curso.imagen && (
                <div className="course-image">
                  <img src={curso.imagen} alt={curso.titulo} />
                </div>
              )}
              <div className="course-info">
                <h3>{curso.titulo}</h3>
                {curso.descripcion && <p className="course-description">{curso.descripcion}</p>}
                <div className="course-code">Código: {curso.codigo}</div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default CourseListingPage;
