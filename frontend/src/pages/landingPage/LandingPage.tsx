import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import "./LandingPage.css";
import logo from "../../assets/logo.png";
import maguito from "../../assets/props/maguito.png";
import dragon from "../../assets/props/dragon.png";
import libro from "../../assets/props/libro.png";
import dueno from "../../assets/props/dueño.png";
import ProfileIcon from "../../assets/icons/profile.svg?react";

// Alternating primary/secondary, except 'u' which uses accent
const TITLE = [
  { key: "c-1", char: "C", cls: "primary" },
  { key: "e-1", char: "e", cls: "secondary" },
  { key: "r-1", char: "r", cls: "primary" },
  { key: "e-2", char: "e", cls: "secondary" },
  { key: "b-1", char: "b", cls: "primary" },
  { key: "r-2", char: "r", cls: "secondary" },
  { key: "u-1", char: "u", cls: "accent" },
  { key: "s-1", char: "s", cls: "accent" },
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
  const [isLoggedIn, setIsLoggedIn] = useState(!!localStorage.getItem("token"));

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("username");
    localStorage.removeItem("role");
    setIsLoggedIn(false);
  };

  return (
    <div className="landing-page">

      {/* Botones de la esquina superior derecha */}
      <div className="landing-top-buttons">
        {isLoggedIn && (
          <button className="landing-login-btn" onClick={() => navigate("/misCursos")}>
            Mis Cursos
          </button>
        )}
        {!isLoggedIn && (
          <button className="landing-login-btn" onClick={() => navigate("/auth/login")}>
            Login
            <ProfileIcon className="landing-login-icon" aria-hidden />
          </button>
        )}
      </div>

      {/* Header row */}
      <div className="landing-header">
        <h1 className="landing-title">
          {TITLE.map((t) => (
            <span key={t.key} className={`title-char ${t.cls}`}>
              {t.char}
            </span>
          ))}
        </h1>
        <img src={logo} alt="Cerebrus mascot" className="landing-logo" />
      </div>

      {/* Cards row */}
      <div className="landing-cards">
        {cards.map((card) => (
          <div
            key={card.route}
            className="landing-card-wrapper"
            onClick={() => navigate(card.route)}
            onKeyDown={(e) => { if (e.key === 'Enter' || e.key === ' ') navigate(card.route); }}
            role="button"
            tabIndex={0}
            style={{ cursor: "pointer" }}
          >
            <div className="landing-card-images">
              {card.images.map((src) => (
                <img key={src} src={src} alt="" />
              ))}
            </div>
            <div className={`landing-card ${card.cardCls}`}>
              <h2>{card.title}</h2>
              <p>{card.desc}</p>
            </div>
          </div>
        ))}
      </div>
      
    {/* FOOTER LEGAL */}
      <footer className="landing-footer">
        <Link to="/terminos">Política de Privacidad y Términos de Servicio</Link>
        <Link className="landing-edunova-link" to="/edunova">Made with ❤ by EduNova</Link>
      </footer>
    </div> 
  );
};

export default LandingPage;