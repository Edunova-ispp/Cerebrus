import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import './CourseFormPage.css';

interface CrearCursoRequest {
  titulo: string;
  descripcion?: string;
  imagen?: string;
}

const CourseFormPage = () => {
  const { cursoId } = useParams();
  const navigate = useNavigate();
  const isEditing = !!cursoId;

  const [titulo, setTitulo] = useState('');
  const [descripcion, setDescripcion] = useState('');
  const [imagen, setImagen] = useState('');
  const [loading, setLoading] = useState(isEditing);
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const token = localStorage.getItem('token');

  useEffect(() => {
    if (isEditing && cursoId) {
      cargarDatosCurso();
    }
  }, [cursoId, isEditing]);

  const cargarDatosCurso = async () => {
    try {
      const response = await fetch(`http://localhost:8080/api/cursos/${parseInt(cursoId!)}/detalles`, {
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
      if (detalles.length >= 1) {
        setTitulo(detalles[0]);
      }
      if (detalles.length >= 2) {
        setDescripcion(detalles[1]);
      }
      if (detalles.length >= 3) {
        setImagen(detalles[2]);
      }
      setLoading(false);
    } catch (err) {
      setError('Error al cargar los datos del curso');
      setLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!titulo.trim()) {
      setError('El título es obligatorio');
      return;
    }

    setSubmitting(true);
    setError('');

    try {
      const request: CrearCursoRequest = {
        titulo: titulo.trim(),
        descripcion: descripcion.trim(),
        imagen: imagen.trim(),
      };

      const url = isEditing && cursoId
        ? `http://localhost:8080/api/cursos/curso/${parseInt(cursoId)}`
        : 'http://localhost:8080/api/cursos/curso';

      const method = isEditing ? 'PUT' : 'POST';

      const response = await fetch(url, {
        method,
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`,
        },
        body: JSON.stringify(request),
      });

      if (!response.ok) {
        throw new Error(`Error al guardar curso: ${response.status}`);
      }

      navigate('/cursos');
    } catch (err: any) {
      setError(err.message || 'Error al guardar el curso');
      setSubmitting(false);
    }
  };

  if (loading) {
    return <div className="page-container"><div className="loading">Cargando datos del curso...</div></div>;
  }

  return (
    <div className="page-container">
      <div className="form-header">
        <h1>{isEditing ? 'Editar Curso' : 'Crear Nuevo Curso'}</h1>
      </div>

      {error && <div className="error-message">{error}</div>}

      <form onSubmit={handleSubmit} className="course-form">
        <div className="form-group">
          <label htmlFor="titulo">
            Título <span className="required">*</span>
          </label>
          <input
            id="titulo"
            type="text"
            value={titulo}
            onChange={(e) => setTitulo(e.target.value)}
            placeholder="Ingresa el título del curso"
            disabled={submitting}
            required
          />
        </div>

        <div className="form-group">
          <label htmlFor="descripcion">Descripción</label>
          <textarea
            id="descripcion"
            value={descripcion}
            onChange={(e) => setDescripcion(e.target.value)}
            placeholder="Ingresa la descripción del curso"
            disabled={submitting}
            rows={5}
          />
        </div>

        <div className="form-group">
          <label htmlFor="imagen">Imagen (URL)</label>
          <input
            id="imagen"
            type="text"
            value={imagen}
            onChange={(e) => setImagen(e.target.value)}
            placeholder="URL de la imagen del curso"
            disabled={submitting}
          />
          {imagen && (
            <div className="image-preview">
              <img src={imagen} alt="Vista previa" />
            </div>
          )}
        </div>

        <div className="form-actions">
          <button
            type="submit"
            className="btn-submit"
            disabled={submitting}
          >
            {submitting ? 'Guardando...' : 'Guardar Curso'}
          </button>
          <button
            type="button"
            className="btn-cancel"
            onClick={() => navigate('/cursos')}
            disabled={submitting}
          >
            Cancelar
          </button>
        </div>
      </form>
    </div>
  );
};

export default CourseFormPage;
