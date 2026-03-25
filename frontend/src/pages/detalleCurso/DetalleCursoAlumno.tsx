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

  const estadoLabel =
    progreso?.estado === "TERMINADA"
      ? "Completado"
      : progreso?.estado === "EMPEZADA"
        ? "En progreso"
        : "Nuevo";

  const estadoClass =
    progreso?.estado === "TERMINADA"
      ? "detalle-badge--done"
      : progreso?.estado === "EMPEZADA"
        ? "detalle-badge--progress"
        : "detalle-badge--new";

  return (
    <div className="detalle-alumno-page">
      <NavbarMisCursos />

      <main className="detalle-alumno-main">
        <button className="detalle-volver" onClick={() => navigate(-1)}>
          ←
        </button>

        {/* ── Hero card ── */}
        <div className="detalle-hero">
          {/* Badge arriba a la derecha */}
          <span className={`detalle-badge ${estadoClass}`}>
            {estadoLabel}
          </span>

          <div className="detalle-hero-cover">
            {curso.imagen ? (
              <img src={curso.imagen} alt={curso.titulo} />
            ) : (
              <span className="detalle-hero-initials">
                {getInitials(curso.titulo)}
              </span>
            )}
          </div>

          <div className="detalle-hero-body">
            <h1 className="detalle-hero-titulo">{curso.titulo}</h1>
            {curso.descripcion && (
              <p className="detalle-hero-desc">{curso.descripcion}</p>
            )}
          </div>

          {/* Botón de acción en su propia línea */}
          <div className="detalle-hero-action">
            {progreso === null && (
              <span className="detalle-hero-loading">Cargando...</span>
            )}

            {progreso?.estado === "SIN_EMPEZAR" && (
              <button
                className="detalle-hero-btn detalle-hero-btn--comenzar"
                onClick={() => navigate(`/mapa/${curso.id}`)}
              >
                Comenzar curso
              </button>
            )}

            {progreso?.estado === "EMPEZADA" && (
              <button
                className="detalle-hero-btn detalle-hero-btn--continuar"
                onClick={() => navigate(`/mapa/${curso.id}`)}
              >
                ▶ Continuar
              </button>
            )}

            {progreso?.estado === "TERMINADA" && (
              <div
                className="detalle-hero-btn detalle-hero-btn--acabado"
                
                aria-label="Curso completado"
              >
                ✅ Completado
              </div>
            )}
          </div>
        </div>
      </main>
    </div>
  );
}
