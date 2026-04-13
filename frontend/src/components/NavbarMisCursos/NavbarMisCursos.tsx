import { useCallback, useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import logo from "../../assets/icons/logoCebrerusTopbar.png";
import misCursosIcon from "../../assets/icons/misCursos.svg";
import perfilIcon from "../../assets/icons/perfil.svg";
import trofeoIcon from "../../assets/icons/Trofeo.svg";
import { getCurrentUserRoles } from "../../types/curso";
import "./NavbarMisCursos.css";

export default function NavbarMisCursos() {
  const navigate = useNavigate();
  const token = localStorage.getItem("token");
  const roles = getCurrentUserRoles();
  const isMaestro = roles.some((r) => r.toUpperCase().includes("MAESTRO"));
  const isOrganizacion = roles.some((r) => r.toUpperCase().includes("ORGANIZACION"));
  const isAlumno = roles.some((r) => r.toUpperCase().includes("ALUMNO"));
  const homeRoute = isOrganizacion ? "/suscripcion" : "/misCursos";

  /* ── Auto-hide on scroll ── */
  const [visible, setVisible] = useState(true);
  const lastY = useRef(0);
  const hideTimer = useRef<ReturnType<typeof setTimeout> | null>(null);

  const show = useCallback(() => {
    setVisible(true);
    if (hideTimer.current) clearTimeout(hideTimer.current);
    hideTimer.current = setTimeout(() => {
      // Don't hide if at the top
      if (window.scrollY > 60) setVisible(false);
    }, 2500);
  }, []);

  useEffect(() => {
    const onScroll = () => {
      const y = window.scrollY;
      const atTop = y < 60;
      const atBottom =
        window.innerHeight + y >= document.documentElement.scrollHeight - 10;

      if (atTop || atBottom) {
        setVisible(true);
        if (hideTimer.current) clearTimeout(hideTimer.current);
      } else if (y < lastY.current) {
        show();
      } else if (y > lastY.current + 4) {
        setVisible(false);
      }
      lastY.current = y;
    };

    const onMouseMove = (e: MouseEvent) => {
      if (e.clientY < 80) show();
    };

    window.addEventListener("scroll", onScroll, { passive: true });
    window.addEventListener("mousemove", onMouseMove, { passive: true });
    return () => {
      window.removeEventListener("scroll", onScroll);
      window.removeEventListener("mousemove", onMouseMove);
      if (hideTimer.current) clearTimeout(hideTimer.current);
    };
  }, [show]);

  useEffect(() => {
    if (!token) {
      navigate("/auth/login");
    }
  }, [navigate, token]);

  return (
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
            </div>
          </div>
        </nav>
      </div>
      <div className="navbar-spacer" />
    </>
  );
}