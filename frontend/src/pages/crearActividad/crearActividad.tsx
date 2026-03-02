import { useNavigate, useParams } from 'react-router-dom';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import './crearActividad.css';

const TIPOS = ['Teoría', 'Tipo test', 'Poner en orden'];

export default function CrearActividad() {
  const { id: cursoId } = useParams<{ id: string }>();
  const navigate = useNavigate();

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

        <div className="ca-contenido">
          <p className="ca-proximamente">Selecciona un tipo de actividad</p>
          <button className="ca-btn-guardar" >
            Guardar
          </button>
        </div>
      </main>
    </div>
  );
}