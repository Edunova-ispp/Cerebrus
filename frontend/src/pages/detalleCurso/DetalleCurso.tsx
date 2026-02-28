import { useEffect, useState } from "react";
import { useLocation, useParams, Navigate } from "react-router-dom";
import { getCurrentUserRoles } from "../../types/curso";
import type { Curso, InscripcionResumen } from "../../types/curso";
import { apiFetch, fetchMisInscripciones } from "../../utils/api";
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
  const [puntos, setPuntos] = useState<number>(0);
  const [loading, setLoading] = useState(true);
  const [notFound, setNotFound] = useState(false);

  useEffect(() => {
    const load = async () => {
      try {
        const [cursoData, inscripciones] = await Promise.all([
          // Si vino del navigation state lo usamos directamente, si no hacemos fetch
          stateFromNav
            ? Promise.resolve(stateFromNav)
            : apiFetch("/api/cursos")
                .then((r) => r.json())
                .then((data: Curso[]) => data.find((c) => String(c.id) === id) ?? null),
          // Sólo los alumnos tienen inscripciones
          isMaestro
            ? Promise.resolve([] as InscripcionResumen[])
            : fetchMisInscripciones().catch(() => [] as InscripcionResumen[]),
        ]);

        if (!cursoData) {
          setNotFound(true);
          return;
        }

        setCurso(cursoData);

        const insc = (inscripciones as InscripcionResumen[]).find(
          (i) => String(i.cursoId) === id
        );
        setPuntos(insc?.puntos ?? 0);
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

  return <DetalleCursoAlumno curso={curso} puntos={puntos} />;
}
