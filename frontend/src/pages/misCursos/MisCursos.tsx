import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import NavbarMisCursos from "../../components/NavbarMisCursos/NavbarMisCursos";
import CursoCard from "../../components/CursoCard/CursoCard";
import { apiFetch, toggleVisibilidadCurso } from "../../utils/api";
import type { Curso } from "../../types/curso";
import { getCurrentUserRoles, getCurrentUserInfo } from "../../types/curso";
import "./MisCursos.css";

// Datos de placeholder para cuando no hay sesión activa.
// El useEffect ya está preparado: si hay token en localStorage carga datos reales, si no usa estos mocks.
const MOCK_CURSOS: Curso[] = [
  { id: 1, titulo: "Placeholder - No login", descripcion: "Cálculo y álgebra lineal", imagen: null, codigo: "MAT01", visibilidad: true },
  { id: 2, titulo: "Historia del Arte", descripcion: "Del Renacimiento al Barroco", imagen: null, codigo: "ARTE01", visibilidad: true },
  { id: 3, titulo: "Programación Web", descripcion: "HTML, CSS y JavaScript", imagen: null, codigo: "WEB01", visibilidad: true },
  { id: 4, titulo: "Biología Celular", descripcion: "Estructura y función celular", imagen: null, codigo: "BIO01", visibilidad: true },
  { id: 5, titulo: "Inglés B2", descripcion: "Nivel intermedio-alto", imagen: null, codigo: "ENG01", visibilidad: true },
  { id: 6, titulo: "Física Cuántica", descripcion: "Introducción a la mecánica cuántica", imagen: null, codigo: "FIS01", visibilidad: true },
];

export default function MisCursos() {
  const [cursos, setCursos] = useState<Curso[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [codigoCurso, setCodigoCurso] = useState("");
  const [joinLoading, setJoinLoading] = useState(false);
  const [joinError, setJoinError] = useState<string | null>(null);
  const [joinSuccess, setJoinSuccess] = useState<string | null>(null);

  const navigate = useNavigate();
  const token = localStorage.getItem("token");
  const isMaestro = getCurrentUserRoles().some((r) =>
    r.toUpperCase().includes("MAESTRO")
  );

  const loadCursos = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await apiFetch("/api/cursos");
      const data = await res.json();
      setCursos(Array.isArray(data) ? data : []);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Error cargando cursos");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (token) {
      const userInfo = getCurrentUserInfo();
      console.log("✅ Sesión activa:", userInfo);
      loadCursos();
    } else {
      console.log("⚠️ Sin token — mostrando cursos mock");
      setCursos(MOCK_CURSOS);
      setLoading(false);
    }
  }, [token]);

  const handleJoin = async () => {
    const codigo = codigoCurso.trim();
    if (!codigo) return;
    setJoinLoading(true);
    setJoinError(null);
    setJoinSuccess(null);
    try {
      await apiFetch(
        `/api/inscripciones/inscribe?codigoCurso=${encodeURIComponent(codigo)}`,  
        { method: "POST" }
      );
      setJoinSuccess("¡Te has unido al curso correctamente!");
      setCodigoCurso("");
      await loadCursos();
    } catch {
      setJoinError("No se pudo unir al curso. Revisa el código.");
    } finally {
      setJoinLoading(false);
    }
  };

  const handleToggleVisibilidad = async (id: number) => {
    try {
      const updated = await toggleVisibilidadCurso(id);
      setCursos((prev) =>
        prev.map((c) => (c.id === updated.id ? updated : c))
      );
    } catch {
      // Si falla, no se actualiza el estado (el toggle vuelve a su posición anterior)
    }
  };

  return (
    <div className="mis-cursos-page">
      <NavbarMisCursos />

      <main className="mis-cursos-main">
        <div className="mis-cursos-header">
          <h1 className="mis-cursos-title">MIS CURSOS</h1>

          {!isMaestro && (
            <div className="mis-cursos-join">
              <label className="join-label" htmlFor="codigoCursoInput">Código del curso:</label>
              <input
                id="codigoCursoInput"
                className="join-input"
                type="text"
                value={codigoCurso}
                onChange={(e) => setCodigoCurso(e.target.value)}
                onKeyDown={(e) => e.key === "Enter" && handleJoin()}
              />
              <button
                className="join-btn"
                onClick={handleJoin}
                disabled={joinLoading}
              >
                {joinLoading ? "..." : "Unirse"}
              </button>
            </div>
          )}
          {isMaestro && (
            <button
              className="join-btn"
              onClick={() => navigate("/crearCurso")}
            >
              + Crear nuevo curso
            </button>
          )}
        </div>

        {joinError && <p className="mis-cursos-feedback mis-cursos-feedback--error">{joinError}</p>}
        {joinSuccess && <p className="mis-cursos-feedback mis-cursos-feedback--success">{joinSuccess}</p>}

        {(() => {
          if (loading) return <p className="mis-cursos-empty">Cargando cursos...</p>;
          if (error) return <p className="mis-cursos-empty mis-cursos-feedback--error">{error}</p>;
          if (cursos.length === 0) return <p className="mis-cursos-empty">No tienes cursos todavía.</p>;
          return (
            <div className="mis-cursos-grid">
              {cursos.map((curso) => (
                <CursoCard
                  key={curso.id}
                  curso={curso}
                  isMaestro={isMaestro}
                  onToggleVisibilidad={handleToggleVisibilidad}
                />
              ))}
            </div>
          );
        })()}
      </main>
    </div>
  );
}