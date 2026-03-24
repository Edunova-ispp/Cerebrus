import { useState, useCallback } from "react";
import NavbarMisCursos from "../../components/NavbarMisCursos/NavbarMisCursos";
import type { Curso } from "../../types/curso";
import ListaTemasCursoProfesor from "../temasDelCurso/ListaTemasCursoProfesor";
import EstadisticasCurso from "../estadisticasCurso/EstadisticasCurso";
import EditarCurso from "../editarCurso/EditarCurso";
import CrearTema from "../crearTema/CrearTema";
import EditarTema from "../editarTema/EditarTema";
import CrearActividad from "../crearActividad/crearActividad";
import EditarActividad from "../editarActividad/EditarActividad";
import "./DetalleCursoProfesor.css";

type View =
  | { tab: "mapa" }
  | { tab: "mapa"; action: "crearTema" }
  | { tab: "mapa"; action: "editarTema"; temaId: number }
  | { tab: "mapa"; action: "crearActividad"; temaId: number }
  | { tab: "mapa"; action: "editarActividad"; temaId: number; actividadId: number }
  | { tab: "estadisticas" }
  | { tab: "editar" };

interface Props {
  readonly curso: Curso;
}

const TABS: { key: "mapa" | "estadisticas" | "editar"; label: string }[] = [
  { key: "mapa", label: "Mapa del curso" },
  { key: "estadisticas", label: "Estadísticas" },
  { key: "editar", label: "Editar curso" },
];

export default function DetalleCursoProfesor({ curso }: Props) {
  const [view, setView] = useState<View>({ tab: "mapa" });

  const goToMapa = useCallback(() => setView({ tab: "mapa" }), []);

  const renderContent = () => {
    switch (view.tab) {
      case "estadisticas":
        return <EstadisticasCurso cursoId={String(curso.id)} embedded />;
      case "editar":
        return <EditarCurso cursoId={String(curso.id)} embedded />;
      case "mapa": {
        if ("action" in view) {
          switch (view.action) {
            case "crearTema":
              return <CrearTema cursoIdProp={String(curso.id)} embedded onDone={goToMapa} />;
            case "editarTema":
              return <EditarTema cursoIdProp={String(curso.id)} temaIdProp={String(view.temaId)} embedded onDone={goToMapa} />;
            case "crearActividad":
              return <CrearActividad temaIdProp={String(view.temaId)} cursoIdProp={String(curso.id)} embedded onDone={goToMapa} />;
            case "editarActividad":
              return <EditarActividad actividadIdProp={String(view.actividadId)} temaIdProp={String(view.temaId)} cursoIdProp={String(curso.id)} embedded onDone={goToMapa} />;
          }
        }
        return (
          <ListaTemasCursoProfesor
            curso={curso}
            embedded
            onCrearTema={() => setView({ tab: "mapa", action: "crearTema" })}
            onEditarTema={(temaId) => setView({ tab: "mapa", action: "editarTema", temaId })}
            onCrearActividad={(temaId) => setView({ tab: "mapa", action: "crearActividad", temaId })}
            onEditarActividad={(temaId, actividadId) => setView({ tab: "mapa", action: "editarActividad", temaId, actividadId })}
          />
        );
      }
    }
  };

  return (
    <div className="detalle-profesor-page">
      <NavbarMisCursos />

      <main className="detalle-profesor-layout">
        {/* Sidebar */}
        <aside className="detalle-sidebar">
          <div className="detalle-sidebar-codigo">
            Código: <strong>{curso.codigo}</strong>
          </div>

          <nav className="detalle-sidebar-nav">
            {TABS.map((tab) => {
              const isInAction = "action" in view;
              const isBack = tab.key === "mapa" && isInAction;
              const isActive = view.tab === tab.key && !isBack;
              return (
                <button
                  key={tab.key}
                  type="button"
                  className={`detalle-sidebar-btn${isActive ? " detalle-sidebar-btn--active" : ""}${isBack ? " detalle-sidebar-btn--back" : ""}`}
                  //onClick={() => setView({ tab: tab.key })}
                >
                  {tab.label}
                  {isBack && <span className="detalle-sidebar-btn__back-hint">Pulsa para volver</span>}
                </button>
              );
            })}
          </nav>
        </aside>

        {/* Content area */}
        <section className="detalle-content">
          {renderContent()}
        </section>
      </main>
    </div>
  );
}