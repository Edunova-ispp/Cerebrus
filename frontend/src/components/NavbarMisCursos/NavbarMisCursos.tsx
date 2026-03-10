import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import logo from "../../assets/icons/logoCebrerusTopbar.png";
import misCursosIcon from "../../assets/icons/misCursos.svg";
import perfilIcon from "../../assets/icons/perfil.svg";
import "./NavbarMisCursos.css";

export default function NavbarMisCursos() {
  const navigate = useNavigate();
  const token = localStorage.getItem("token");

  useEffect(() => {
    if (!token) {
      navigate("/auth/login");
    }
  }, [navigate, token]);

  return (
    <nav className="navbar">
      <div className="navbar-inner">
        <button
          type="button"
          className="navbar-logo-btn"
          onClick={() => navigate("/misCursos")}
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
          <button className="navbar-link" onClick={() => navigate("/misCursos")}>
            <img src={misCursosIcon} alt="" className="navbar-icon" />
            Mis Cursos
          </button>
          <button className="navbar-link" onClick={() => navigate("/perfil")}>
            <img src={perfilIcon} alt="" className="navbar-icon" />
            Perfil
          </button>
        </div>
      </div>
    </nav>
  );
}