import { useState } from "react";
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
  readonly onCardClick?: () => void;
  readonly onEliminar?: (id: number) => void;
}

export default function CursoCard({ curso, isMaestro = false, onToggleVisibilidad, onCardClick, onEliminar }: CursoCardProps) {
  const [modalOpen, setModalOpen] = useState(false);
  const [copied, setCopied] = useState(false);

  const handleCopy = () => {
    navigator.clipboard.writeText(curso.codigo);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <>
      {modalOpen && (
        <div className="codigo-modal-overlay" onClick={() => setModalOpen(false)}>
          <div className="codigo-modal" onClick={(e) => e.stopPropagation()}>
            <p className="codigo-modal__label">Código del curso</p>
            <p className="codigo-modal__value">{curso.codigo}</p>
            <button className="codigo-modal__copy-btn" onClick={handleCopy}>
              {copied ? '✓ Copiado' : '⎘ Copiar'}
            </button>
            <button className="codigo-modal__close-btn" onClick={() => setModalOpen(false)}>✕</button>
          </div>
        </div>
      )}
      <motion.div
        className="curso-card"
        whileHover={{ scale: 1.05 }}
        transition={{ type: "spring", stiffness: 300, damping: 20 }}
        onClick={onCardClick}
        style={{ cursor: onCardClick ? "pointer" : undefined }}
      >
      {isMaestro && onEliminar && (
        <button
          className="curso-card__delete-btn"
          onClick={(e) => {
          e.stopPropagation();
          if (confirm("¿Estás seguro de que quieres borrar este curso?")) {
            onEliminar(curso.id);
          }
        }}
      >
    ✕
  </button>
)}
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
            <div className="curso-card__codigo-group">
              <span className="curso-card__maestro-value curso-card__codigo">{curso.codigo}</span>
              <button
                className="curso-card__expand-btn"
                onClick={(e) => { e.stopPropagation(); setModalOpen(true); }}
                aria-label="Ver código completo"
              >
                ⛶
              </button>
            </div>
          </div>
          <div className="curso-card__maestro-row">
            <span className="curso-card__maestro-label">Visible:</span>
            <label
              className="curso-card__toggle"
              aria-label="Visibilidad del curso"
              onClick={(e) => e.stopPropagation()}
              onKeyDown={(e) => e.stopPropagation()}
            >
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
    </>
  );
}