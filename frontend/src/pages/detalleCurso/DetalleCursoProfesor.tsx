import { useNavigate } from "react-router-dom";
import NavbarMisCursos from "../../components/NavbarMisCursos/NavbarMisCursos";
import type { Curso } from "../../types/curso";
import "./DetalleCursoProfesor.css";

interface Props {
  readonly curso: Curso;
}

export default function DetalleCursoProfesor({ curso }: Props) {
  const navigate = useNavigate();

  return (
    <div className="detalle-profesor-page">
      <NavbarMisCursos />

      <main className="detalle-profesor-main">
        <button className="detalle-volver" onClick={() => navigate(`/misCursos`)}>
          ← Volver
        </button>

        <div className="detalle-profesor-banner">
          Código del curso: <strong>{curso.codigo}</strong>
        </div>

        <div className="detalle-profesor-actions-container">
          
          <button
            className="detalle-action-btn mapa-btn"
            onClick={() => navigate(`/mapa/${curso.id}`)}
          >
            Mapa del curso
          </button>

          <button
            className="detalle-action-btn estadisticas-btn"
            onClick={() => navigate(`/estadisticas/${curso.id}`)}
          >
            Estadísticas
          </button>

          <button
            className="detalle-action-btn editar-btn"
            onClick={() => navigate(`/editarCurso/${curso.id}`)}
          >
            Editar curso
          </button>
          
        </div>
      </main>
    </div>
  );
}