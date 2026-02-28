import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import logo from "../../assets/logo.png";
import "./NavbarMisCursos.css";

// 8-bit pixel art avatar
function PixelAvatar() {
  // Each row is 8 pixels wide. 1 = skin, 2 = hair/dark, 0 = transparent
  const grid = [
    [0,0,2,2,2,2,0,0],
    [0,2,2,2,2,2,2,0],
    [0,2,1,2,1,2,2,0],
    [0,2,1,1,1,1,2,0],
    [0,2,2,1,1,2,2,0],
    [0,0,2,2,2,2,0,0],
    [0,2,2,2,2,2,2,0],
    [2,2,2,2,2,2,2,2],
  ];
  const colors: Record<number, string> = { 0: "transparent", 1: "#F5C5A3", 2: "#4A2C0A" };
  const size = 5;
  return (
    <svg width={8 * size} height={8 * size} viewBox={`0 0 ${8 * size} ${8 * size}`} style={{ imageRendering: "pixelated", display: "block" }}>
      {grid.map((row, r) =>
        row.map((cell, c) =>
          cell === 0 ? null : (
            <rect key={`${r}-${c}`} x={c * size} y={r * size} width={size} height={size} fill={colors[cell]} />
          )
        )
      )}
    </svg>
  );
}

export default function NavbarMisCursos() {
  const navigate = useNavigate();
  const token = localStorage.getItem("token");
  const username = localStorage.getItem("username") || "Usuario";

  useEffect(() => {
    if (!token) {
      navigate("/auth/login");
    }
  }, [navigate, token]);

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("username");
    localStorage.removeItem("role");
    navigate("/");
  };

  return (
    <nav className="navbar">
      <div className="navbar-inner">
        <button
          type="button"
          className="navbar-logo-btn"
          onClick={() => navigate("/")}
        >
          <img src={logo} alt="Cerebrus" className="navbar-logo" />
        </button>
        <div className="navbar-links">
          <button className="navbar-link" onClick={() => navigate("/misCursos")}>
            Mis Cursos
          </button>
          <div className="navbar-link navbar-link--perfil">
            <span className="profile-icon"><PixelAvatar /></span>
            <span className="navbar-username">{username}</span>
          </div>
          <button className="navbar-link navbar-link--logout" onClick={handleLogout}>
            Cerrar sesión
          </button>
        </div>
      </div>
    </nav>
  );
}