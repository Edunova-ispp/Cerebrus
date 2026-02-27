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
  readonly curso: Curso;
  readonly isMaestro?: boolean;
  readonly onToggleVisibilidad?: (id: number) => void;
}

export default function CursoCard({ curso, isMaestro = false, onToggleVisibilidad }: CursoCardProps) {
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
      {isMaestro && (
        <div className="curso-card__maestro-info">
          <div className="curso-card__maestro-row">
            <span className="curso-card__maestro-label">Código:</span>
            <span className="curso-card__maestro-value curso-card__codigo">{curso.codigo}</span>
          </div>
          <div className="curso-card__maestro-row">
            <span className="curso-card__maestro-label">Visible:</span>
            <label className="curso-card__toggle">
              <input
                type="checkbox"
                checked={curso.visibilidad}
                onChange={() => onToggleVisibilidad?.(curso.id)}
              />
              <span className="curso-card__toggle-slider" />
            </label>
          </div>
        </div>
      )}
    </motion.div>
  );
}