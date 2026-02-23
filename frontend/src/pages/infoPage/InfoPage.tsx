import { useNavigate } from "react-router-dom";
import "./InfoPage.css";
import logo from "../../assets/logo.png";

export type UserType = "alumno" | "profesor" | "dueno";

interface InfoPageProps {
  userType: UserType;
}

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

// Video camera icon as inline SVG
function VideoIcon() {
  return (
    <svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
      <path d="M17 10.5V7a1 1 0 0 0-1-1H4a1 1 0 0 0-1 1v10a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1v-3.5l4 4v-11l-4 4z" />
    </svg>
  );
}

const SECTIONS = {
  cerebrus: {
    color: "primary" as const,
    title: "¿Qué es Cerebrus?",
    desc: "Cerebrus es una plataforma de gestión del aprendizaje diseñada para optimizar la formación de equipos. Mediante un entorno gamificado, permite estructurar itinerarios formativos, reconocer el compromiso y fomentar el crecimiento.",
    reverse: false,
  },
  institucion: {
    color: "orange" as const,
    title: "¿Para su institución?",
    desc: "Una nueva forma de aprendizaje adaptada a su organización. Incorpore cursos y formación. Seleccione el número de instructores y alumnos para obtener una suscripción personalizada que se ajuste dinámicamente al tamaño de su organización, optimizando sus recursos a medida que crece.",
    reverse: true,
  },
  profesor: {
    color: "secondary" as const,
    title: "¿Para los profes?",
    desc: "Cursos personalizados y temáticos. Optimice sus rutas pedagógicas con la IA de aprendizaje. Nuestra estrategia gamificada le presenta la información de sus contenidos de manera que sus alumnos la asimilen de forma rápida y efectiva.",
    reverse: false,
  },
  alumno: {
    color: "primary" as const,
    title: "¿Para los alumnos?",
    desc: "Aprendizaje divertido y fresco. Convierta el estudio en una aventura épica. Supere retos con sus compañeros, gane puntos de experiencia y suba de nivel sus conocimientos en un entorno diseñado para que aprender por tus objetivos sea su videojuego favorito.",
    reverse: true,
  },
};

const SECTIONS_BY_TYPE: Record<UserType, (keyof typeof SECTIONS)[]> = {
  dueno:    ["cerebrus", "institucion", "profesor", "alumno"],
  profesor: ["cerebrus", "profesor"],
  alumno:   ["cerebrus", "alumno"],
};



function InfoPage({ userType }: InfoPageProps) {
  const navigate = useNavigate();
  const sections = SECTIONS_BY_TYPE[userType];

  return (
    <div className="info-page">
      <button className="info-back-btn" onClick={() => navigate("/")}>
        Volver
      </button>

      <div className="info-header">
        <h1 className="info-title">
          {TITLE.map((t, i) => (
            <span key={i} className={`title-char ${t.cls}`}>
              {t.char}
            </span>
          ))}
        </h1>
        <p className="info-subtitle">Gamifica el aprendizaje</p>
      </div>

      <div className="info-sections">
        {sections.map((key) => {
          const s = SECTIONS[key];
          return (
            <div key={key} className={`info-section${s.reverse ? " reverse" : ""}`}>
              <div className={`info-text-card ${s.color}`}>
                <h2>{s.title}</h2>
                <p>{s.desc}</p>
              </div>
              <div className="info-video-placeholder">
                <VideoIcon />
              </div>
            </div>
          );
        })}
      </div>

      <div className="info-bottom">
        <button className="info-cta-btn" onClick={() => navigate("/login")}>
          Comenzar aventura
        </button>
        <img src={logo} alt="Cerebrus mascot" className="info-bottom-logo" />
      </div>
    </div>
  );
}

export default InfoPage;
