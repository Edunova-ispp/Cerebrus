import { useState } from "react";
import { useNavigate } from "react-router-dom";
import NavbarMisCursos from "../../components/NavbarMisCursos/NavbarMisCursos";
import type { Curso } from "../../types/curso";
import "./DetalleCursoAlumno.css";

// puntos: 0 = sin empezar | 1-99 = en progreso | 100 = acabado
type CursoProgreso = "sin-empezar" | "en-progreso" | "acabado";

function getProgreso(puntos: number): CursoProgreso {
  if (puntos >= 100) return "acabado";
  if (puntos > 0)    return "en-progreso";
  return "sin-empezar";
}

function getInitials(titulo: string): string {
  return titulo
    .split(" ")
    .slice(0, 2)
    .map((w) => w[0]?.toUpperCase() ?? "")
    .join("");
}

interface Props {
  readonly curso: Curso;
  readonly puntos: number;
}

export default function DetalleCursoAlumno({ curso, puntos }: Props) {
  const navigate = useNavigate();
  const progreso = getProgreso(puntos);
  const [modalVisible, setModalVisible] = useState<boolean>(progreso === "acabado");

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

        {progreso === "sin-empezar" && (
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

        {progreso === "en-progreso" && (
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

        {progreso === "acabado" && (
          <div className="detalle-alumno-accion detalle-alumno-accion--acabado">
            Este curso ya está acabado
          </div>
        )}
      </main>

      {modalVisible && (
        <div className="detalle-modal-overlay" onClick={() => setModalVisible(false)}>
          <div className="detalle-modal" onClick={(e) => e.stopPropagation()}>
            <p className="detalle-modal__title">¡Has acabado el curso!</p>
            <p className="detalle-modal__puntos">Has conseguido {puntos} puntos</p>
            <button className="detalle-modal__close" onClick={() => setModalVisible(false)}>
              Cerrar
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
