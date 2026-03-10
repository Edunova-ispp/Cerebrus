import { useNavigate } from "react-router-dom";
import NavbarMisCursos from "../../components/NavbarMisCursos/NavbarMisCursos";
import "./Perfil.css";

export default function Perfil() {
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("username");
    localStorage.removeItem("role");
    navigate("/");
  };

  return (
    <>
      <NavbarMisCursos />
      <div className="perfil-container">
        <h1 className="perfil-title">En construcción :)</h1>
        <button className="perfil-logout-btn" onClick={handleLogout}>
          Cerrar sesión
        </button>
      </div>
    </>
  );
}
