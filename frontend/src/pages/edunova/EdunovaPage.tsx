import { Link, useNavigate } from "react-router-dom";
import "./EdunovaPage.css";
import {
  PROJECT_SOCIAL_LINKS,
  TEAM_AREAS,
  TEAM_MEMBERS_BY_AREA,
  type TeamMember,
} from "./edunovaData";

function YouTubeIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden className="edunova-social-icon">
      <path
        fill="currentColor"
        d="M23.5 6.2a3.07 3.07 0 0 0-2.16-2.16C19.43 3.5 12 3.5 12 3.5s-7.43 0-9.34.54A3.07 3.07 0 0 0 .5 6.2 32.7 32.7 0 0 0 0 12a32.7 32.7 0 0 0 .5 5.8 3.07 3.07 0 0 0 2.16 2.16c1.91.54 9.34.54 9.34.54s7.43 0 9.34-.54a3.07 3.07 0 0 0 2.16-2.16A32.7 32.7 0 0 0 24 12a32.7 32.7 0 0 0-.5-5.8M9.6 15.54V8.46L15.85 12z"
      />
    </svg>
  );
}

function InstagramIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden className="edunova-social-icon">
      <path
        fill="currentColor"
        d="M7.8 2h8.4A5.8 5.8 0 0 1 22 7.8v8.4a5.8 5.8 0 0 1-5.8 5.8H7.8A5.8 5.8 0 0 1 2 16.2V7.8A5.8 5.8 0 0 1 7.8 2m-.2 2A3.6 3.6 0 0 0 4 7.6v8.8A3.6 3.6 0 0 0 7.6 20h8.8a3.6 3.6 0 0 0 3.6-3.6V7.6A3.6 3.6 0 0 0 16.4 4zm9.65 1.5a1.25 1.25 0 1 1-1.25 1.25 1.25 1.25 0 0 1 1.25-1.25M12 7a5 5 0 1 1-5 5 5 5 0 0 1 5-5m0 2a3 3 0 1 0 3 3 3 3 0 0 0-3-3"
      />
    </svg>
  );
}

function SocialIcon({ id }: { id: string }) {
  if (id === "youtube") {
    return <YouTubeIcon />;
  }

  if (id === "instagram") {
    return <InstagramIcon />;
  }

  return null;
}

function getInitials(name: string) {
  const parts = name
    .trim()
    .split(" ")
    .filter(Boolean)
    .slice(0, 2);

  return parts.map((part) => part[0]?.toUpperCase() ?? "").join("") || "EN";
}

function isExternalUrl(url: string) {
  return /^https?:\/\//i.test(url);
}

function MemberCard({ member }: { member: TeamMember }) {
  const cardContent = (
    <>
      {member.photoUrl ? (
        <img
          src={member.photoUrl}
          alt={`Foto de ${member.fullName}`}
          className="edunova-member-photo"
        />
      ) : (
        <div className="edunova-member-photo edunova-member-photo--placeholder" aria-hidden>
          {getInitials(member.fullName)}
        </div>
      )}
      <h3>{member.fullName}</h3>
      <p>{member.role}</p>
    </>
  );

  if (!member.pageUrl) {
    return <article className="edunova-member-card">{cardContent}</article>;
  }

  if (isExternalUrl(member.pageUrl)) {
    return (
      <a
        className="edunova-member-card edunova-member-card--link"
        href={member.pageUrl}
        target="_blank"
        rel="noreferrer"
        aria-label={`Abrir página de ${member.fullName}`}
      >
        {cardContent}
      </a>
    );
  }

  return (
    <Link
      className="edunova-member-card edunova-member-card--link"
      to={member.pageUrl}
      aria-label={`Abrir página de ${member.fullName}`}
    >
      {cardContent}
    </Link>
  );
}

function EdunovaPage() {
  const navigate = useNavigate();

  return (
    <main className="edunova-page">
      <nav className="edunova-top-nav" aria-label="Navegación de salida">
        <button type="button" className="edunova-back-btn" onClick={() => navigate(-1)}>
          ← Volver
        </button>
      </nav>

      <header className="edunova-hero">
        <p className="edunova-eyebrow">Equipo del Proyecto</p>
        <h1>EduNova</h1>
        <p>
          ¡Conoce al equipo que impulsa Cerebrus!
        </p>
      </header>

      <section className="edunova-socials" aria-label="Redes sociales de Cerebrus">
        <h2>Redes sociales</h2>
        <div className="edunova-socials-list">
          {PROJECT_SOCIAL_LINKS.map((social) => (
            <a
              key={social.id}
              href={social.url}
              target="_blank"
              rel="noreferrer"
              className="edunova-social-link"
            >
              <SocialIcon id={social.id} />
              {social.label}
            </a>
          ))}
        </div>
      </section>

      <section className="edunova-areas" aria-label="Áreas del equipo">
        {TEAM_AREAS.map((area) => {
          const members = TEAM_MEMBERS_BY_AREA[area];

          return (
            <article key={area} className="edunova-area-block">
              <h2>{area}</h2>
              <div className="edunova-members-grid">
                {members.length > 0 ? (
                  members.map((member) => <MemberCard key={member.id} member={member} />)
                ) : (
                  <p className="edunova-empty">Sin miembros cargados todavía.</p>
                )}
              </div>
            </article>
          );
        })}
      </section>

      <footer className="edunova-footer-nav">
        <Link to="/">Volver a inicio</Link>
      </footer>
    </main>
  );
}

export default EdunovaPage;
