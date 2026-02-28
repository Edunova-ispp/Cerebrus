import { useNavigate } from "react-router-dom";
import NavbarMisCursos from "../../components/NavbarMisCursos/NavbarMisCursos";
import type { Curso, ProgresoAlumno } from "../../types/curso";
import "./DetalleCursoAlumno.css";

function getInitials(titulo: string): string {
  return titulo
    .split(" ")
    .slice(0, 2)
    .map((w) => w[0]?.toUpperCase() ?? "")
    .join("");
}

interface Props {
  readonly curso: Curso;
  readonly progreso: ProgresoAlumno | null;
}

export default function DetalleCursoAlumno({ curso, progreso }: Props) {
  const navigate = useNavigate();

  return (
    <div className="detalle-alumno-page">
      <NavbarMisCursos />

      <main className="detalle-alumno-main">
        <button className="detalle-volver" onClick={() => navigate(-1)}>
          ← Volver
        </button>

        <div className="detalle-alumno-cover">
          {curso.imagen ? (
            <img src={curso.imagen} alt={curso.titulo} />
          ) : (
            <span className="detalle-alumno-initials">{getInitials(curso.titulo)}</span>
          )}
        </div>

        <div className="detalle-alumno-info">
          <h1 className="detalle-alumno-titulo">{curso.titulo}</h1>
          {curso.descripcion && (
            <p className="detalle-alumno-descripcion">{curso.descripcion}</p>
          )}
        </div>

        {progreso === null && (
          <div className="detalle-alumno-accion">
            Cargando...
          </div>
        )}

        {progreso?.estado === "SIN_EMPEZAR" && (
          <div
            className="detalle-alumno-accion detalle-alumno-accion--comenzar"
            role="button"
            tabIndex={0}
            onClick={() => navigate(`/mapa/${curso.id}`)}
            onKeyDown={(e) => e.key === "Enter" && navigate(`/mapa/${curso.id}`)}
          >
            Comenzar
          </div>
        )}

        {progreso?.estado === "EMPEZADA" && (
          <div
            className="detalle-alumno-accion detalle-alumno-accion--continuar"
            role="button"
            tabIndex={0}
            onClick={() => navigate(`/mapa/${curso.id}`)}
            onKeyDown={(e) => e.key === "Enter" && navigate(`/mapa/${curso.id}`)}
          >
            Continuar
          </div>
        )}

        {progreso?.estado === "TERMINADA" && (
          <div className="detalle-alumno-accion detalle-alumno-accion--acabado">
            Has acabado el curso. Has conseguido ___ puntos.
          </div>
        )}
      </main>
    </div>
  );
}
