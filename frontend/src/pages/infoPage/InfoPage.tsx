import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import "./InfoPage.css";
import logo from "../../assets/logo.png";
import libro from "../../assets/props/libro.png";
import dueno from "../../assets/props/dueño.png";
import paraLosProfesIA from "../../assets/gifsInfoPage/paraLosProfesIA.gif";
import paraLosAlumnos from "../../assets/gifsInfoPage/paraLosAlumnos.gif";
import paraElOrganizador from "../../assets/gifsInfoPage/paraElOrganizador.gif";
import planDePrecios from "../../assets/gifsInfoPage/planDePrecios.gif";
import paraLosProfesEstadistica from "../../assets/gifsInfoPage/paraLosProfesEstadistica.gif";
import profCursoYTemas from "../../assets/gifsInfoPage/profCursoYTemas.gif";
import profSelectorActs from "../../assets/gifsInfoPage/profSelectorActs.gif";
import profGestionAlumnos from "../../assets/gifsInfoPage/profGestionAlumnos.gif";
import alumUnirseCurso from "../../assets/gifsInfoPage/alumUnirseCurso.gif";
import alumArenas from "../../assets/gifsInfoPage/alumArenas.gif";
import alumActCarta from "../../assets/gifsInfoPage/alumActCarta.gif";
import alumActCrucigrama from "../../assets/gifsInfoPage/alumActCrucigrama.gif";
import alumActTablero from "../../assets/gifsInfoPage/alumActTablero.gif";
import alumActTeoria from "../../assets/gifsInfoPage/alumActTeoria.gif";

export type UserType = "alumno" | "profesor" | "dueno";

interface InfoPageProps {
  userType: UserType;
}

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

type MediaSlot = { src?: string; alt?: string; placeholder?: string; isGif?: boolean };

const SECTION_MEDIA: Record<string, MediaSlot> = {
  cerebrus:        { src: logo,    alt: "Cerebrus" },
  institucion:     { src: dueno,   alt: "Gestión de institución" },
  profesor:        { src: libro,   alt: "Herramientas para profes" },
  alumno:          { src: alumArenas, alt: "Evolución de las arenas del alumno", isGif: true },
  carta:           { src: alumActCarta, alt: "Actividad Cartas", isGif: true },
  crucigrama:      { src: alumActCrucigrama, alt: "Actividad Crucigrama", isGif: true },
  tablero:         { src: alumActTablero, alt: "Actividad Tablero", isGif: true },
  teoria:          { src: alumActTeoria, alt: "Actividad Teoría", isGif: true },
  codigo_alumno:   { src: alumUnirseCurso, alt: "Unirse a un curso con código como alumno", isGif: true },
  precios:         { placeholder: "Imagen o vídeo ilustrativo de precios" },
  cerebrus_org:    { src: logo, alt: "Cerebrus" },
  plataforma_org:  { src: paraElOrganizador, alt: "Panel de gestión", isGif: true },
  profesores_org:  { src: paraLosProfesIA, alt: "Herramientas para profes con IA", isGif: true },
  alumnos_org:     { src: paraLosAlumnos, alt: "Experiencia del alumno", isGif: true },
  precios_org:     { src: planDePrecios, alt: "Plan de precios", isGif: true },
  estadisticas_org: { src: paraLosProfesEstadistica, alt: "Estadísticas del profesor", isGif: true },
  // profesor sections
  cursos_prof:       { src: profCursoYTemas, alt: "Creación de un curso con temas y actividades", isGif: true },
  actividades_prof:  { src: profSelectorActs, alt: "Selector de los 9 tipos de actividad", isGif: true },
  ia_prof:           { src: paraLosProfesIA, alt: "Generación de actividades con IA", isGif: true },
  estadisticas_prof: { src: paraLosProfesEstadistica, alt: "Panel de estadísticas del profesor", isGif: true },
  alumnos_prof:      { src: profGestionAlumnos, alt: "Gestión de alumnos en el curso", isGif: true },
};

const SECTIONS = {
  cerebrus: {
    color: "primary" as const,
    title: "¿Qué es Cerebrus?",
    desc: "Estudia, compite y sube de nivel. Cerebrus convierte cada lección en un reto: gana puntos de experiencia con cada actividad y evoluciona tu arena — desde Cachorro del Umbral hasta Señor de Cerebrus.",
    reverse: false,
  },
  institucion: {
    color: "primary" as const,
    title: "¿Para su institución?",
    desc: "Una nueva forma de aprendizaje adaptada a su organización. Incorpore cursos y formación. Seleccione el número de instructores y alumnos para obtener una suscripción personalizada que se ajuste dinámicamente al tamaño de su organización, optimizando sus recursos a medida que crece.",
    reverse: true,
  },
  profesor: {
    color: "primary" as const,
    title: "¿Para los profes?",
    desc: "Cursos personalizados y temáticos. Optimice sus rutas pedagógicas con la IA de aprendizaje. Nuestra estrategia gamificada le presenta la información de sus contenidos de manera que sus alumnos la asimilen de forma rápida y efectiva.",
    reverse: true,
  },
  alumno: {
    color: "primary" as const,
    title: "¡Estudiar mola!",
    desc: "Cada actividad que completas te da puntos de experiencia. Acumúlalos y sube de arena: desde Cachorro del Umbral hasta Señor de Cerebrus. Cuanto más aprendes, más evoluciona tu personaje.",
    reverse: false,
  },
  carta: {
    color: "primary" as const,
    title: "🃏 Cartas",
    desc: "Voltea cartas y empareja cada pregunta con su respuesta. Las que no encajan vuelven bocabajo. El cronómetro corre desde el principio — encuentra todos los pares cuanto antes.",
    reverse: false,
  },
  crucigrama: {
    color: "primary" as const,
    title: "🧩 Crucigrama",
    desc: "Lee las pistas, escribe las letras y pulsa Comprobar cuando termines. Puedes corregir los errores antes de enviar. Eso sí, si pides ver la solución, la penalización es máxima.",
    reverse: true,
  },
  tablero: {
    color: "primary" as const,
    title: "🎲 Tablero",
    desc: "Mueve a Cerbero por el tablero respondiendo las preguntas de las casillas adyacentes. Cada fallo acumula penalización en la puntuación final — cero errores, puntuación máxima.",
    reverse: false,
  },
  teoria: {
    color: "primary" as const,
    title: "📖 Teoría",
    desc: "Voltea la tarjeta, lee el contenido de tu profe y pulsa \"He terminado de leer\". Sin cronómetro, sin puntuación — solo el conocimiento que necesitas antes de pasar al siguiente reto.",
    reverse: true,
  },
  codigo_alumno: {
    color: "primary" as const,
    title: "🔑 Únete con un código",
    desc: "Tu profe te da un código único. Tú lo introduces en \"Mis Cursos\" y ya eres parte del curso. Sin formularios, sin listas de espera.",
    reverse: true,
  },
  precios: {
    color: "primary" as const,
    title: "💰 Acceso",
    desc: "Tu organización gestiona el acceso. 5 €/alumno al mes — tú solo tienes que entrar y aprender.",
    reverse: true,
  },
  cerebrus_org: {
    color: "primary" as const,
    title: "¿Qué es Cerebrus?",
    desc: "Cerebrus es la plataforma de formación para organizaciones que quieren medir y acelerar el aprendizaje de sus equipos. Itinerarios estructurados, seguimiento en tiempo real y un entorno que motiva a los alumnos a avanzar.",
    reverse: false,
  },
  plataforma_org: {
    color: "primary" as const,
    title: "Panel de gestión",
    desc: "Configure su organización, asigne profesores y alumnos, y controle todo desde un único panel. Alta, baja y gestión de usuarios en tiempo real, sin complicaciones.",
    reverse: true,
  },
  profesores_org: {
    color: "primary" as const,
    title: "Sus profesores",
    desc: "Sus docentes crean cursos con actividades interactivas, creación y corrección automática mediante IA. Menos tiempo corrigiendo, más tiempo enseñando.",
    reverse: false,
  },
  alumnos_org: {
    color: "primary" as const,
    title: "Sus alumnos",
    desc: "Retos, ranking y puntos de experiencia que mantienen a los alumnos motivados. En actividades como Teoría, leen el contenido, estudian la imagen y lo marcan como leído — sin fricciones, sin papeleo.",
    reverse: true,
  },
  precios_org: {
    color: "primary" as const,
    title: "Plan de precios",
    desc: "Profesor: 10 €/mes (7 €/mes a partir de 20).\nAlumno: 5 €/mes (3 €/mes a partir de 50).\nSin alta ni permanencia.",
    reverse: false,
  },
  estadisticas_org: {
    color: "primary" as const,
    title: "Estadísticas para sus profesores",
    desc: "Sus profesores tienen acceso a informes detallados por alumno y por actividad. Identifique en segundos quién va retrasado, qué contenido falla más y dónde centrar la atención.",
    reverse: true,
  },
  // ── Secciones para /infoProfesores ────────────────────────────────────────
  cursos_prof: {
    color: "primary" as const,
    title: "Cursos y temas",
    desc: "Crea un curso, organízalo en temas y añade actividades en el orden que quieras. Código único para que tus alumnos se unan, visibilidad pública o privada — tú decides.",
    reverse: true,
  },
  actividades_prof: {
    color: "primary" as const,
    title: "9 tipos de actividad",
    desc: "Teoría, Test, Ordenación, Marcar en imagen, Tablero, Clasificación, Cartas, Crucigrama y Pregunta abierta. Cada uno con corrección automática para que no tengas que corregir a mano.",
    reverse: false,
  },
  ia_prof: {
    color: "primary" as const,
    title: "Genera actividades con IA",
    desc: "Describe lo que quieres trabajar y la IA crea la actividad completa: preguntas, respuestas y estructura. Compatible con Teoría, Test, Clasificación, Cartas y Tablero.",
    reverse: false,
  },
  estadisticas_prof: {
    color: "primary" as const,
    title: "Estadísticas en profundidad",
    desc: "Nota media, tiempo por actividad, tasa de finalización, histogramas, semáforos de riesgo por alumno. Filtra por nombre, nota o estado y detecta quién necesita atención antes de que sea tarde.",
    reverse: true,
  },
  alumnos_prof: {
    color: "primary" as const,
    title: "Gestión de alumnos",
    desc: "Inscribe alumnos al curso, consulta su progreso y puntos de experiencia, y dales de baja si es necesario. Todo desde el panel del curso, sin burocracia.",
    reverse: false,
  },
};

const SECTIONS_BY_TYPE: Record<UserType, { key: keyof typeof SECTIONS; reverseOverride?: boolean }[]> = {
  dueno:    [
    { key: "cerebrus_org",    reverseOverride: false },
    { key: "plataforma_org",  reverseOverride: true  },
    { key: "profesores_org",  reverseOverride: false },
    { key: "estadisticas_org",reverseOverride: true  },
    { key: "alumnos_org",     reverseOverride: false },
    { key: "precios_org",     reverseOverride: true  },
  ],
  profesor: [
    { key: "cerebrus",         reverseOverride: false },
    { key: "cursos_prof",      reverseOverride: true  },
    { key: "actividades_prof", reverseOverride: false },
    { key: "ia_prof",          reverseOverride: true  },
    { key: "estadisticas_prof",reverseOverride: false },
    { key: "alumnos_prof",     reverseOverride: true  },
  ],
  alumno:   [
    { key: "cerebrus",     reverseOverride: false },
    { key: "codigo_alumno",reverseOverride: true  },
    { key: "alumno",       reverseOverride: false },
    { key: "carta",        reverseOverride: true  },
    { key: "crucigrama",   reverseOverride: false },
    { key: "tablero",      reverseOverride: true  },
    { key: "teoria",       reverseOverride: false },
  ],
};



// ── GifPlayer ─────────────────────────────────────────────────────────────

function GifPlayer({
  src, alt, isActive, onActivate, onFullscreen,
}: {
  src: string; alt: string; isActive: boolean;
  onActivate: () => void; onFullscreen: () => void;
}) {
  const [thumbnail, setThumbnail] = useState<string | null>(null);
  const imgRef = useRef<HTMLImageElement>(null);

  // Capture first frame as thumbnail whenever src changes
  useEffect(() => {
    let cancelled = false;
    const img = new Image();
    img.onload = () => {
      if (cancelled) return;
      try {
        const canvas = document.createElement('canvas');
        canvas.width = img.naturalWidth;
        canvas.height = img.naturalHeight;
        canvas.getContext('2d')?.drawImage(img, 0, 0);
        setThumbnail(canvas.toDataURL());
      } catch {
        setThumbnail(src);
      }
    };
    img.src = src;
    return () => { cancelled = true; };
  }, [src]);

  return (
    <div className={`gif-player${isActive ? ' gif-player--active' : ''}`}>
      {/* Animated GIF — only mounted when active */}
      {isActive && (
        <div className="info-media-wrapper">
          <img ref={imgRef} src={src} alt={alt} className="info-section-image" />
          <button
            className="info-media-fullscreen-btn"
            onClick={onFullscreen}
            aria-label="Ver en pantalla completa"
          >⛶</button>
        </div>
      )}
      {/* Frozen first frame — shown when inactive */}
      {!isActive && (
        <div className="gif-player__frozen" onClick={onActivate}>
          {thumbnail
            ? <img src={thumbnail} alt={alt} className="info-section-image" />
            : <img src={src} alt={alt} className="info-section-image" />
          }
          <div className="gif-player__overlay">
            <span className="gif-player__play">▶</span>
          </div>
        </div>
      )}
    </div>
  );
}

// ── InfoPage ───────────────────────────────────────────────────────────────

function InfoPage({ userType }: InfoPageProps) {
  const navigate = useNavigate();
  const sections = SECTIONS_BY_TYPE[userType];
  const isPro = userType === "dueno" || userType === "profesor";
  const [lightbox, setLightbox] = useState<{ src: string; alt: string } | null>(null);

  const gifKeys = sections.map(s => s.key).filter(k => SECTION_MEDIA[k]?.isGif);
  const [activeGifKey, setActiveGifKey] = useState<string | null>(gifKeys[0] ?? null);

  return (
    <div className={`info-page${isPro ? " info-page--pro" : ""}`}>
      <button className="info-back-btn" onClick={() => navigate("/")}>
        ←
      </button>

      

      <div className="info-header">
        <h1 className="info-title">
          {TITLE.map((t) => (
            <span key={t.key} className={`title-char ${t.cls}`}>
              {t.char}
            </span>
          ))}
        </h1>
        <p className="info-subtitle">Gamifica el aprendizaje</p>
      </div>

      <div className="info-sections">
        {sections.map(({ key, reverseOverride }, idx) => {
          const s = SECTIONS[key];
          const reverse = reverseOverride ?? s.reverse;
          const textFrom  = reverse ? 80 : -80;
          const videoFrom = reverse ? -80 : 80;
          const textCardColor = isPro
            ? (idx % 2 === 0 ? "pro-blue" : "pro-white")
            : (idx % 2 === 0 ? "primary" : "secondary");
          const media = SECTION_MEDIA[key];
          return (
            <div key={key} className={`info-section${reverse ? " reverse" : ""}${media?.isGif && activeGifKey === key ? " info-section--gif-active" : ""}`}>
              <motion.div
                className={`info-text-card ${textCardColor}`}
                initial={{ opacity: 0, x: textFrom }}
                whileInView={{ opacity: 1, x: 0 }}
                viewport={{ once: true, amount: 0.3 }}
                transition={{ duration: 0.5, delay: idx * 0.05, ease: "easeOut" }}
              >
                <h2>{s.title}</h2>
                {s.desc.split("\n").map((line, i) => (
                  <p key={i}>{line}</p>
                ))}
              </motion.div>
              <motion.div
                className="info-video-placeholder"
                initial={{ opacity: 0, x: videoFrom }}
                whileInView={{ opacity: 1, x: 0 }}
                viewport={{ once: true, amount: 0.3 }}
                transition={{ duration: 0.5, delay: idx * 0.05 + 0.1, ease: "easeOut" }}
              >
                {media?.isGif && media.src ? (
                  <GifPlayer
                    src={media.src}
                    alt={media.alt ?? key}
                    isActive={activeGifKey === key}
                    onActivate={() => setActiveGifKey(key)}
                    onFullscreen={() => setLightbox({ src: media.src!, alt: media.alt! })}
                  />
                ) : media?.src ? (
                  <div className="info-media-wrapper">
                    <img
                      src={media.src}
                      alt={media.alt}
                      className="info-section-image"
                      loading="lazy"
                    />
                    <button
                      className="info-media-fullscreen-btn"
                      onClick={() => setLightbox({ src: media.src!, alt: media.alt! })}
                      aria-label="Ver en pantalla completa"
                    >⛶</button>
                  </div>
                ) : (
                  <div className="info-media-placeholder">
                    <span className="info-media-placeholder__icon">🎬</span>
                    <span className="info-media-placeholder__label">
                      {media?.placeholder ?? "Imagen o vídeo"}
                    </span>
                  </div>
                )}
              </motion.div>
            </div>
          );
        })}
      </div>

      <div className="info-bottom">
        <motion.button
          className={`info-cta-btn${isPro ? " info-cta-btn--pro" : ""}`}
          onClick={() => navigate("/auth/login")}
          initial={{ opacity: 0, y: 40 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true, amount: 0.5 }}
          transition={{ duration: 0.7, ease: "easeOut" }}
          whileHover={{ scale: 1.06 }}
          whileTap={{ scale: 0.97 }}
        >
          {userType === "dueno" ? "Solicitar acceso" : userType === "profesor" ? "Entrar" : "Comenzar aventura"}
        </motion.button>
        <motion.img
          src={logo}
          alt="Cerebrus mascot"
          className="info-bottom-logo"
          initial={{ opacity: 0, y: 30 }}
          whileInView={{ opacity: 1, y: 0 }}
          animate={{ scale: [1, 1.06, 1] }}
          viewport={{ once: true, amount: 0.5 }}
          transition={{
            opacity: { duration: 0.7, delay: 0.15, ease: "easeOut" },
            y:       { duration: 0.7, delay: 0.15, ease: "easeOut" },
            scale:   { duration: 4.5, repeat: Infinity, ease: "easeInOut" },
          }}
        />
      </div>

      {lightbox && (
        <div className="info-lightbox" onClick={() => setLightbox(null)}>
          <img src={lightbox.src} alt={lightbox.alt} className="info-lightbox__img" />
        </div>
      )}
    </div>
  );
}

export default InfoPage;
