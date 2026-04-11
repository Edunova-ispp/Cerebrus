import agePhoto from "../../assets/teamMembers/age.png";
import davidPhoto from "../../assets/teamMembers/david.png";
import eloyPhoto from "../../assets/teamMembers/eloy.png";
import fernandoPhoto from "../../assets/teamMembers/fernando.png";
import gigiPhoto from "../../assets/teamMembers/gigi.png";
import iratxePhoto from "../../assets/teamMembers/iratxe.png";
import isaPhoto from "../../assets/teamMembers/isa.png";
import ivanPhoto from "../../assets/teamMembers/ivan.png";
import josePhoto from "../../assets/teamMembers/jose.png";
import juananPhoto from "../../assets/teamMembers/juanan.png";
import lidiaPhoto from "../../assets/teamMembers/lidia.png";
import manuPhoto from "../../assets/teamMembers/manu.png";
import mariaPhoto from "../../assets/teamMembers/maría.png";
import mpinoPhoto from "../../assets/teamMembers/mpino.png";
import rafaPhoto from "../../assets/teamMembers/rafa.png";
import raquelPhoto from "../../assets/teamMembers/raquel.png";
import rubenPhoto from "../../assets/teamMembers/ruben.png";

export type TeamArea =
  | "Backend"
  | "Frontend"
  | "Marketing/Documentación"
  | "QA"
  | "Despliegue/CI/CD";

export interface TeamMember {
  id: string;
  fullName: string;
  role: string;
  photoUrl?: string;
  pageUrl?: string;
}

export interface SocialLink {
  id: string;
  label: string;
  url: string;
}

export const TEAM_AREAS: TeamArea[] = [
  "Backend",
  "Frontend",
  "Marketing/Documentación",
  "QA",
  "Despliegue/CI/CD",
];

// Estructura base para completar con datos reales del equipo.
export const TEAM_MEMBERS_BY_AREA: Record<TeamArea, TeamMember[]> = {
  Backend: [
    {
      id: "backend-lidia-ning-fernandez-casillas",
      fullName: "Lidia Ning Fernández Casillas",
      role: "Coordinadora",
      photoUrl: lidiaPhoto,
    },
    {
      id: "backend-juan-antonio-gonzalez-lucena",
      fullName: "Juan Antonio González Lucena",
      role: "Desarrollador",
      photoUrl: juananPhoto,
    },
    {
      id: "backend-jose-angel-herrera-romero",
      fullName: "José Ángel Herrera Romero",
      role: "Desarrollador",
      photoUrl: josePhoto,
    },
    {
      id: "backend-manuel-toledo-gonzalez",
      fullName: "Manuel Toledo González",
      role: "Desarrollador",
      photoUrl: manuPhoto,
    },
    {
      id: "backend-ruben-lopez-exposito",
      fullName: "Rubén López Expósito",
      role: "Desarrollador",
      photoUrl: rubenPhoto,
    },
  ],
  Frontend: [
    {
      id: "frontend-ivan-fernandez-limarquez",
      fullName: "Iván Fernández Limárquez",
      role: "Coordinador",
      photoUrl: ivanPhoto,
    },
    {
      id: "frontend-raquel-ortega-almiron",
      fullName: "Raquel Ortega Almirón",
      role: "Desarrolladora",
      photoUrl: raquelPhoto,
    },
    {
      id: "frontend-eloy-sancho-cebrero",
      fullName: "Eloy Sancho Cebrero",
      role: "Desarrollador",
      photoUrl: eloyPhoto,
    },
    {
      id: "frontend-maria-auxiliadora-quintana-fernandez",
      fullName: "María Auxiliadora Quintana Fernández",
      role: "Desarrolladora",
      photoUrl: mariaPhoto,
    },
    {
      id: "frontend-rafael-segura-gomez",
      fullName: "Rafael Segura Gómez",
      role: "Desarrollador",
      photoUrl: rafaPhoto,
    },
  ],
  "Marketing/Documentación": [
    {
      id: "marketing-elena-de-los-santos-barrera",
      fullName: "Elena De los Santos Barrera",
      role: "Coordinadora",
      photoUrl: gigiPhoto,
    },
    {
      id: "marketing-maria-del-pino-perez-dominguez",
      fullName: "María del Pino Pérez Domínguez",
      role: "Community Manager",
      photoUrl: mpinoPhoto,
    },
    {
      id: "marketing-angel-sanchez-ruiz",
      fullName: "Ángel Sánchez Ruiz",
      role: "Devrel",
      photoUrl: agePhoto,
    },
  ],
  QA: [
    {
      id: "qa-iratxe-parra-moreno",
      fullName: "Iratxe Parra Moreno",
      role: "QA Automation",
      photoUrl: iratxePhoto,
    },
    {
      id: "qa-isabel-sanchez-castro",
      fullName: "Isabel Sánchez Castro",
      role: "QA Manual",
      photoUrl: isaPhoto,
    },
  ],
  "Despliegue/CI/CD": [
    {
      id: "cicd-fernando-partal-garcia",
      fullName: "Fernando Partal García",
      role: "DEVOPS + Scrum Master",
      photoUrl: fernandoPhoto,
    },
    {
      id: "cicd-david-valencia-toscano",
      fullName: "David Valencia Toscano",
      role: "DEVOPS + Project Manager",
      photoUrl: davidPhoto,
      pageUrl: "https://www.linkedin.com/in/davidvalenciatoscano/",
    },
  ],
};

export const PROJECT_SOCIAL_LINKS: SocialLink[] = [
  {
    id: "youtube",
    label: "Canal de YouTube de Cerebrus",
    url: "https://www.youtube.com/@CerebrusEdu",
  },
  {
    id: "instagram",
    label: "Cuenta de Instagram",
    url: "https://www.instagram.com/cerebrusedu",
  },
];
