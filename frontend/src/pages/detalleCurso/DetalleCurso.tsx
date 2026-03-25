import { useEffect, useState } from "react";
import { useLocation, useParams, Navigate } from "react-router-dom";
import { getCurrentUserRoles } from "../../types/curso";
import type { Curso, ProgresoAlumno } from "../../types/curso";
import { apiFetch } from "../../utils/api";
import DetalleCursoProfesor from "./DetalleCursoProfesor";
import DetalleCursoAlumno from "./DetalleCursoAlumno";

interface LocationState {
  curso?: Curso;
}

export default function DetalleCurso() {
  const { id } = useParams<{ id: string }>();
  const location = useLocation();
  const stateFromNav = (location.state as LocationState | null)?.curso;

  const isMaestro = getCurrentUserRoles().some((r) =>
    r.toUpperCase().includes("MAESTRO")
  );

  const [curso, setCurso] = useState<Curso | null>(stateFromNav ?? null);
  const [progreso, setProgreso] = useState<ProgresoAlumno | null>(null);
  const [loading, setLoading] = useState(true);
  const [notFound, setNotFound] = useState(false);

  useEffect(() => {
    const load = async () => {
      try {
        const apiBase = (import.meta.env.VITE_API_URL ?? "").trim().replace(/\/$/, "");
        const cursoData = stateFromNav
          ? stateFromNav
          : await apiFetch(`${apiBase}/api/cursos`)
              .then((r) => r.json())
              .then((data: Curso[]) => data.find((c) => String(c.id) === id) ?? null);

        if (!cursoData) {
          setNotFound(true);
          return;
        }

        setCurso(cursoData);

        if (!isMaestro) {
          let prog: ProgresoAlumno | null = null;
          try {
            const apiBase2 = (import.meta.env.VITE_API_URL ?? "").trim().replace(/\/$/, "");
            const res = await fetch(`${apiBase2}/api/cursos/${id}/progreso`, {
              headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${localStorage.getItem("token") ?? ""}`,
              },
            });
            const raw = await res.json();
            prog = raw as ProgresoAlumno;
          } catch (e) {
            console.error("[Progreso] Error al obtener progreso:", e);
          }
          setProgreso(prog);
        }
      } catch {
        setNotFound(true);
      } finally {
        setLoading(false);
      }
    };

    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  if (loading) {
    return (
      <div style={{ display: "flex", alignItems: "center", justifyContent: "center", minHeight: "100vh", fontFamily: "'Pixelify Sans', sans-serif", fontSize: "1.5rem" }}>
        Cargando...
      </div>
    );
  }

  if (notFound || !curso) {
    return <Navigate to="/misCursos" replace />;
  }

  if (isMaestro) {
    return <DetalleCursoProfesor curso={curso} />;
  }

  return <DetalleCursoAlumno curso={curso} progreso={progreso} />;
}
