import { useNavigate } from "react-router-dom";
import "./LandingPage.css";
import logo from "../../assets/logo.png";
import maguito from "../../assets/props/maguito.png";
import dragon from "../../assets/props/dragon.png";
import libro from "../../assets/props/libro.png";
import dueno from "../../assets/props/dueño.png";
import ProfileIcon from "../../assets/icons/profile.svg?react";

// Alternating primary/secondary, except 'u' which uses accent
const TITLE = [
  { char: "C", cls: "primary" },
  { char: "e", cls: "secondary" },
  { char: "r", cls: "primary" },
  { char: "e", cls: "secondary" },
  { char: "b", cls: "primary" },
  { char: "r", cls: "secondary" },
  { char: "u", cls: "accent" },
  { char: "s", cls: "primary" },
];

const cards = [
  {
    images: [maguito],
    cardCls: "primary",
    title: "¿Eres un aventurero?",
    desc: "¡Únete a un curso, completa desafíos y sube de nivel tus conocimientos!",
    route: "/infoAlumnos",
  },
  {
    images: [libro, dragon],
    cardCls: "secondary",
    title: "¿Eres un Maestro?",
    desc: "Crea misiones épicas, diseña mapas de aprendizaje divertidos y guía a tus alumnos a la sabiduría.",
    route: "/infoProfesores",
  },
  {
    images: [dueno],
    cardCls: "orange",
    title: "¿Eres un Dueño?",
    desc: "Administra tu organización, gestiona instructores y miembros para conquistar tus objetivos.",
    route: "/infoDueños",
  },
];

function LandingPage() {
  const navigate = useNavigate();

  return (
    <div className="landing-page">
      
      {/* Contenedor para agrupar los botones de la esquina superior */}
      <div className="landing-top-buttons" style={{ display: "flex", justifyContent: "flex-end", gap: "10px", padding: "10px" }}>
        <button className="landing-login-btn" onClick={() => navigate("/auth/login")}>
          Login
          <ProfileIcon className="landing-login-icon" aria-hidden />
        </button>

        {/* Nuevo botón de Cerrar sesión */}
        <button className="landing-login-btn" onClick={() => navigate("/auth/logout")} style={{ backgroundColor: "#ff4d4d", color: "white" }}>
          Cerrar sesión
        </button>
      </div>

      {/* Header row */}
      <div className="landing-header">
        <h1 className="landing-title">
          {TITLE.map((t, i) => (
            <span key={i} className={`title-char ${t.cls}`}>
              {t.char}
            </span>
          ))}
        </h1>
        <img src={logo} alt="Cerebrus mascot" className="landing-logo" />
      </div>

      {/* Cards row */}
      <div className="landing-cards">
        {cards.map((card, i) => (
          <div key={i} className="landing-card-wrapper" onClick={() => navigate(card.route)} style={{ cursor: "pointer" }}>
            <div className="landing-card-images">
              {card.images.map((src, j) => (
                <img key={j} src={src} alt="" />
              ))}
            </div>
            <div className={`landing-card ${card.cardCls}`}>
              <h2>{card.title}</h2>
              <p>{card.desc}</p>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

export default LandingPage;