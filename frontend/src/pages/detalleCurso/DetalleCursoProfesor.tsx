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

  <div className="detalle-profesor-banner1">
    Código del curso: <strong>{curso.codigo}</strong>
  </div>

  <div className="detalle-profesor-actions">
    <button className="detalle-editar-btn" onClick={() => navigate(`/cursos/${curso.id}/temas`)}>
      Mapa del curso
    </button>
    <button className="detalle-editar-btn">
      Estadísticas
    </button>
    <button className="detalle-editar-btn" onClick={() => navigate(`/editarCurso/${curso.id}`)}>
      Editar detalles del curso
    </button>
  </div>
</main>
    </div>
  );
}
