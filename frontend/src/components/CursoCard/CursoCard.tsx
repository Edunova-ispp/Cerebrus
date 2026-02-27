import { motion } from "framer-motion";
import type { Curso } from "../../types/curso";
import "./CursoCard.css";

function getInitials(titulo: string): string {
  return titulo
    .split(" ")
    .slice(0, 2)
    .map((w) => w[0]?.toUpperCase() ?? "")
    .join("");
}

interface CursoCardProps {
  curso: Curso;
}

export default function CursoCard({ curso }: CursoCardProps) {
  return (
    <motion.div
      className="curso-card"
      whileHover={{ scale: 1.05 }}
      transition={{ type: "spring", stiffness: 300, damping: 20 }}
    >
      <div className="curso-card__cover">
        {curso.imagen ? (
          <img
            src={curso.imagen}
            alt={curso.titulo}
            className="curso-card__img"
          />
        ) : (
          <div className="curso-card__fallback">
            <span className="curso-card__initials">{getInitials(curso.titulo)}</span>
          </div>
        )}
      </div>
      <div className="curso-card__footer">
        <span className="curso-card__titulo">{curso.titulo}</span>
      </div>
    </motion.div>
  );
}