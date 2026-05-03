import { useState, useEffect, useCallback, type ChangeEvent } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { apiFetch } from "../../utils/api";
import "./InscribirAlumnos.css";
import "../../components/NavbarMisCursos/NavbarMisCursos.css";
import { getCurrentUserRoles } from "../../types/curso";
import misCursosIcon from "../../assets/icons/misCursos.svg";
import perfilIcon from "../../assets/icons/perfil.svg";
import trofeoIcon from "../../assets/icons/Trofeo.svg";
import logo from "../../assets/icons/logoCebrerusTopbar.png";


interface Alumno {
  id: number;
  nombre: string;
  primerApellido: string;
  segundoApellido: string | null;
  correoElectronico: string;
}

interface AlumnosPage {
  alumnos: Alumno[];
  numeroTotal: number;
  numeroPagina: number;
  totalPaginas: number;
  esUltimaPagina: boolean;
}

const apiBase = (import.meta.env.VITE_API_URL ?? "").trim().replace(/\/$/, "");

export default function InscribirAlumnos() {
  const navigate = useNavigate();
  const [visible, setVisible] = useState(true);
  const { cursoId } = useParams<{ cursoId: string }>();
  const token = localStorage.getItem("token");
  const roles = getCurrentUserRoles();
  const isMaestro = roles.some((r) => r.toUpperCase().includes("MAESTRO"));
  const isOrganizacion = roles.some((r) => r.toUpperCase().includes("ORGANIZACION"));
  const isAlumno = roles.some((r) => r.toUpperCase().includes("ALUMNO"));
  const homeRoute = isOrganizacion ? "/suscripcion" : "/misCursos";
  const [alumnos, setAlumnos] = useState<Alumno[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [successMsg, setSuccessMsg] = useState("");
  const [search, setSearch] = useState("");
  const [pagina, setPagina] = useState(0);
  const [totalPaginas, setTotalPaginas] = useState(1);
  const [selectedIds, setSelectedIds] = useState<Set<number>>(new Set());
  const [inscribiendo, setInscribiendo] = useState(false);

  const handleLogout = useCallback(() => {
    localStorage.removeItem("token");
    localStorage.removeItem("username");
    localStorage.removeItem("role");
    navigate("/");
  }, [navigate]);

  const fetchAlumnos = useCallback(async () => {
    if (!cursoId) return;
    try {
      setLoading(true);
      setError("");
      const url = search
        ? `${apiBase}/api/maestros/alumnos-disponibles/buscar?curso=${cursoId}&q=${encodeURIComponent(search)}&pagina=${pagina}&tamanio=10`
        : `${apiBase}/api/maestros/alumnos-disponibles?curso=${cursoId}&pagina=${pagina}&tamanio=10`;
      
      const res = await apiFetch(url);
      const data: AlumnosPage = await res.json();
      setAlumnos(data.alumnos);
      setTotalPaginas(data.totalPaginas);
      setPagina(data.numeroPagina);
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : "Error al cargar los alumnos");
    } finally {
      setLoading(false);
    }
  }, [cursoId, search, pagina]);

  useEffect(() => {
    fetchAlumnos();
  }, [fetchAlumnos]);

  const toggleAlumno = (id: number) => {
    const newSelected = new Set(selectedIds);
    if (newSelected.has(id)) {
      newSelected.delete(id);
    } else {
      newSelected.add(id);
    }
    setSelectedIds(newSelected);
  };

  const toggleTodos = () => {
    const newSelected = new Set(selectedIds);
    const todosEnPagina = alumnos.length > 0 && alumnos.every(a => newSelected.has(a.id));

    if (todosEnPagina) {
      alumnos.forEach(a => newSelected.delete(a.id));
    } else {
      alumnos.forEach(a => newSelected.add(a.id));
    }
    setSelectedIds(newSelected);
  };

  const handleInscribir = async () => {
    if (selectedIds.size === 0) return;
    try {
      setInscribiendo(true);
      setError("");
      const res = await apiFetch(`${apiBase}/api/maestros/inscribir-alumnos?curso=${cursoId}`, {
        method: "POST",
        body: JSON.stringify({ alumnoIds: Array.from(selectedIds) }),
      });

      if (res.ok) {
        setSuccessMsg(`¡${selectedIds.size} alumnos inscritos correctamente!`);
        setSelectedIds(new Set());
        await fetchAlumnos();
      }
    } catch (e: unknown) {
      setError("Error al inscribir alumnos");
    } finally {
      setInscribiendo(false);
    }
  };

  const handleBuscar = (e: ChangeEvent<HTMLInputElement>) => {
    setSearch(e.target.value);
    setPagina(0);
  };

  return (
    <>
    <>
      <div className={`navbar-wrap${visible ? "" : " navbar-wrap--hidden"}`}>
        <nav className={`navbar${isMaestro ? ' navbar--maestro' : ''}${isOrganizacion ? ' navbar--maestro' : ''}`}>
          <div className="navbar-inner">
            <button
              type="button"
              className="navbar-logo-btn"
              onClick={() => navigate(homeRoute)}
            >
              <img src={logo} alt="Cerebrus" className="navbar-logo" />
              <span className="navbar-title">
                <span className="navbar-char primary">C</span>
                <span className="navbar-char secondary">e</span>
                <span className="navbar-char primary">r</span>
                <span className="navbar-char secondary">e</span>
                <span className="navbar-char primary">b</span>
                <span className="navbar-char secondary">r</span>
                <span className="navbar-char accent">u</span>
                <span className="navbar-char accent">s</span>
              </span>
            </button>
            <div className="navbar-links">
              {!isOrganizacion && (
                <button type="button" className="navbar-link" onClick={() => navigate("/misCursos")}>
                  <img src={misCursosIcon} alt="" className="navbar-icon" />
                  <span>Mis Cursos</span>
                </button>
              )}
              {isAlumno && (
                <button type="button" className="navbar-link" onClick={() => navigate("/puntos")}>
                  <img src={trofeoIcon} alt="" className="navbar-icon" />
                  <span>Puntos</span>
                </button>
              )}
              {isOrganizacion && (
                <button type="button" className="navbar-link" onClick={() => navigate("/suscripcion")}>
                  <span>Suscripción</span>
                </button>
              )}
              {isOrganizacion && (
                <button type="button" className="navbar-link" onClick={() => navigate("/gestion-usuarios")}>
                  <span>Usuarios</span>
                </button>
              )}
              <button type="button" className="navbar-link" onClick={() => navigate("/perfil")}>
                <img src={perfilIcon} alt="" className="navbar-icon" />
                <span>Perfil</span>
              </button>
              <button type="button" className="navbar-link navbar-link--logout" onClick={handleLogout}>
              <svg className="navbar-icon navbar-icon--logout" viewBox="0 0 24 24" aria-hidden="true" focusable="false" style={{ transform: 'scaleX(-1)' }}>
                <path
                  fill="currentColor"
                  d="M14 3H19C19.5523 3 20 3.44772 20 4V20C20 20.5523 19.5523 21 19 21H14V19H18V5H14V3ZM10.2929 7.29289L11.7071 8.70711L9.41421 11H17V13H9.41421L11.7071 15.2929L10.2929 16.7071L6.58579 13L5.17157 12L6.58579 11L10.2929 7.29289Z"
                />
              </svg>
                <span>Cerrar sesión</span>
              </button>
            </div>
          </div>
        </nav>
      </div>
      <div className="navbar-spacer" />
    </>

    <div className="ga-container">
      <h2 className="ga-title">Inscribir Alumnos</h2>
      <p className="ga-subtitle">Selecciona los alumnos para añadir al curso</p>

      {successMsg && <div className="ga-toast ga-toast--ok">{successMsg}</div>}
      {error && <div className="ga-toast ga-toast--err">{error}</div>}

      <div className="ga-toolbar">
        <input 
          className="ga-search" 
          placeholder="Buscar alumno por nombre o email..." 
          value={search} 
          onChange={handleBuscar} 
        />
        <span className="ga-count-label">
          {selectedIds.size} seleccionados
        </span>
      </div>

      <div className="ga-table-wrap">
        <table className="ga-table">
          <thead>
            <tr>
              <th className="ga-col-check">
                <input 
                  type="checkbox" 
                  checked={alumnos.length > 0 && alumnos.every(a => selectedIds.has(a.id))}
                  onChange={toggleTodos}
                />
              </th>
              <th style={{ width: '45%' }}>Nombre Completo</th>
              <th style={{ width: '45%' }}>Correo Electrónico</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr><td colSpan={3} style={{textAlign: 'center', padding: '40px'}}>Cargando alumnos...</td></tr>
            ) : alumnos.length === 0 ? (
              <tr><td colSpan={3} style={{textAlign: 'center', padding: '40px'}}>No se encontraron alumnos</td></tr>
            ) : (
              alumnos.map(a => (
                <tr key={a.id} className={selectedIds.has(a.id) ? "ga-row--selected" : ""}>
                  <td className="ga-col-check">
                    <input 
                      type="checkbox" 
                      checked={selectedIds.has(a.id)} 
                      onChange={() => toggleAlumno(a.id)}
                    />
                  </td>
                  <td style={{ fontWeight: 500 }}>
                    {`${a.nombre} ${a.primerApellido} ${a.segundoApellido ?? ""}`.trim()}
                  </td>
                  <td style={{ color: '#64748b' }}>
                    {a.correoElectronico}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      <div className="ga-pagination">
        <button className="ga-btn ga-btn--secondary btn-small" disabled={pagina === 0} onClick={() => setPagina(pagina - 1)}>Anterior</button>
        <span className="ga-page-info">Página {pagina + 1} de {totalPaginas}</span>
        <button className="ga-btn ga-btn--secondary btn-small" disabled={pagina >= totalPaginas - 1} onClick={() => setPagina(pagina + 1)}>Siguiente</button>
      </div>

      <div className="ga-actions">
        <button className="ga-btn ga-btn--secondary" onClick={() => navigate(-1)}>
          ← Volver
        </button>
        
        <div className="ga-btn-group">
          <button className="ga-btn ga-btn--secondary" onClick={() => navigate(-1)}>
            Cancelar
          </button>
          <button 
            className="ga-btn ga-btn--primary"
            onClick={handleInscribir} 
            disabled={selectedIds.size === 0 || inscribiendo}
          >
            {inscribiendo ? "Inscribiendo..." : `Inscribir ${selectedIds.size} alumnos`}
          </button>
        </div>
      </div>
    </div>
    </>
  );
}