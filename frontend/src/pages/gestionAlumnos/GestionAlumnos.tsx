import { useState, useEffect, useCallback } from "react";
import { apiFetch } from "../../utils/api";
import "./GestionAlumnos.css";

interface AlumnoCurso {
  alumnoId: number;
  nombre: string;
  primerApellido: string;
  segundoApellido: string | null;
  nombreUsuario: string;
  correoElectronico: string;
  puntos: number;
  fechaInscripcion: string;
}

interface Props {
  readonly cursoId: string;
  readonly embedded?: boolean;
}

const apiBase = (import.meta.env.VITE_API_URL ?? "").trim().replace(/\/$/, "");

export default function GestionAlumnos({ cursoId, embedded }: Props) {
  const [alumnos, setAlumnos] = useState<AlumnoCurso[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [confirmId, setConfirmId] = useState<number | null>(null);
  const [successMsg, setSuccessMsg] = useState("");
  const [search, setSearch] = useState("");

  const fetchAlumnos = useCallback(async () => {
    try {
      setLoading(true);
      setError("");
      const res = await apiFetch(`${apiBase}/api/inscripciones/curso/${cursoId}/alumnos`);
      const data: AlumnoCurso[] = await res.json();
      setAlumnos(data);
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : "Error al cargar los alumnos");
    } finally {
      setLoading(false);
    }
  }, [cursoId]);

  useEffect(() => {
    fetchAlumnos();
  }, [fetchAlumnos]);

  const handleExpulsar = async (alumnoId: number) => {
    try {
      await apiFetch(`${apiBase}/api/inscripciones/curso/${cursoId}/alumnos/${alumnoId}`, {
        method: "DELETE",
      });
      setAlumnos((prev) => prev.filter((a) => a.alumnoId !== alumnoId));
      setConfirmId(null);
      setSuccessMsg("Alumno expulsado correctamente");
      setTimeout(() => setSuccessMsg(""), 3000);
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : "Error al expulsar al alumno");
      setConfirmId(null);
    }
  };

  const filtered = alumnos.filter((a) => {
    const q = search.toLowerCase();
    return (
      a.nombre.toLowerCase().includes(q) ||
      a.primerApellido.toLowerCase().includes(q) ||
      (a.segundoApellido ?? "").toLowerCase().includes(q) ||
      a.nombreUsuario.toLowerCase().includes(q) ||
      a.correoElectronico.toLowerCase().includes(q)
    );
  });

  const confirmAlumno = alumnos.find((a) => a.alumnoId === confirmId);

  return (
    <div className={embedded ? "ga-embedded" : "ga-page"}>
      <h2 className="ga-title">Gestión de alumnos</h2>
      <p className="ga-subtitle">{alumnos.length} alumno{alumnos.length !== 1 ? "s" : ""} inscrito{alumnos.length !== 1 ? "s" : ""}</p>

      {successMsg && <div className="ga-toast ga-toast--ok">{successMsg}</div>}
      {error && <div className="ga-toast ga-toast--err">{error}</div>}

      <div className="ga-toolbar">
        <input
          className="ga-search"
          type="text"
          placeholder="Buscar alumno…"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
      </div>

      {loading ? (
        <p className="ga-loading">Cargando alumnos…</p>
      ) : filtered.length === 0 ? (
        <p className="ga-empty">
          {search ? "No se encontraron alumnos con esa búsqueda." : "No hay alumnos inscritos en este curso."}
        </p>
      ) : (
        <div className="ga-table-wrap">
          <table className="ga-table">
            <thead>
              <tr>
                <th>Nombre completo</th>
                <th>Usuario</th>
                <th>Email</th>
                <th>Puntos</th>
                <th>Inscripción</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map((a) => (
                <tr key={a.alumnoId}>
                  <td>
                    {a.nombre} {a.primerApellido}
                    {a.segundoApellido ? ` ${a.segundoApellido}` : ""}
                  </td>
                  <td>{a.nombreUsuario}</td>
                  <td>{a.correoElectronico}</td>
                  <td>
                    <span className="ga-badge ga-badge--puntos">{a.puntos}</span>
                  </td>
                  <td>{new Date(a.fechaInscripcion).toLocaleDateString("es-ES")}</td>
                  <td className="ga-actions-cell">
                    <button
                      className="ga-btn ga-btn--danger"
                      type="button"
                      onClick={() => setConfirmId(a.alumnoId)}
                    >
                      Expulsar
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Confirmation modal */}
      {confirmId !== null && confirmAlumno && (
        <div
          className="ga-overlay"
          onClick={(e) => e.target === e.currentTarget && setConfirmId(null)}
          onKeyDown={(e) => {
            if (e.key === 'Enter' || e.key === ' ' || e.key === 'Escape') {
              setConfirmId(null);
            }
          }}
          role="button"
          tabIndex={0}
          aria-label="Cerrar modal de confirmacion"
        >
          <div className="ga-modal">
            <h3 className="ga-modal-title">Confirmar expulsión</h3>
            <p className="ga-modal-text">
              ¿Estás seguro de que quieres expulsar a{" "}
              <strong>
                {confirmAlumno.nombre} {confirmAlumno.primerApellido}
              </strong>{" "}
              de este curso? Esta acción no se puede deshacer.
            </p>
            <div className="ga-modal-actions">
              <button
                className="ga-btn ga-btn--secondary"
                type="button"
                onClick={() => setConfirmId(null)}
              >
                Cancelar
              </button>
              <button
                className="ga-btn ga-btn--danger"
                type="button"
                onClick={() => handleExpulsar(confirmId)}
              >
                Expulsar
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
