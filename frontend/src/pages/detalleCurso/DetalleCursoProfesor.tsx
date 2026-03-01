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
        <button className="detalle-volver" onClick={() => navigate(-1)}>
          ← Volver
        </button>

        <div className="detalle-profesor-banner">
          Código del curso: <strong>{curso.codigo}</strong>
        </div>

        <div className="detalle-profesor-row">
          <div className="detalle-placeholder-box">Mapa del curso</div>
          <div className="detalle-placeholder-box">Estadísticas</div>
        </div>

        <div className="detalle-profesor-actions">
          <button
            className="detalle-editar-btn"
            onClick={() => navigate(`/editarCurso/${curso.id}`)}
          >
            Editar
          </button>
        </div>
      </main>
    </div>
  );
}
