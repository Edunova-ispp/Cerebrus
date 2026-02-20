import { useNavigate } from "react-router-dom";
import "./LandingPage.css";
import logo from "../../assets/logo.png";
import maguito from "../../assets/props/maguito.png";
import dragon from "../../assets/props/dragon.png";
import libro from "../../assets/props/libro.png";
import dueno from "../../assets/props/dueño.png";

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
    route: "/loginAlumnos",
  },
  {
    images: [libro, dragon],
    cardCls: "secondary",
    title: "¿Eres un Maestro?",
    desc: "Crea misiones épicas, diseña mapas de aprendizaje y guía a tus alumnos a la sabiduría.",
    route: "/loginProfesores",
  },
  {
    images: [dueno],
    cardCls: "orange",
    title: "¿Eres un Dueño?",
    desc: "Administra tu organización, gestiona instructores y miembros para conquistar tus objetivos.",
    route: "/loginDueños",
  },
];

function LandingPage() {
  const navigate = useNavigate();

  return (
    <div className="landing-page">
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
