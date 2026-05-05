import { useEffect, useId, useMemo, useRef } from 'react';
import './CommentsViewModal.css';

interface Props {
  readonly title: string;
  readonly comment: string | null | undefined;
  readonly onClose: () => void;
}

function getFocusableElements(container: HTMLElement): HTMLElement[] {
  const nodes = container.querySelectorAll<HTMLElement>(
    'a[href], button:not([disabled]), textarea:not([disabled]), input:not([disabled]), select:not([disabled]), [tabindex]:not([tabindex="-1"])',
  );
  return Array.from(nodes).filter((el) => !el.hasAttribute('disabled') && !el.getAttribute('aria-hidden'));
}

export default function CommentsViewModal({ title, comment, onClose }: Props) {
  const titleId = useId();
  const modalRef = useRef<HTMLDivElement | null>(null);
  const closeBtnRef = useRef<HTMLButtonElement | null>(null);
  const previouslyFocused = useRef<HTMLElement | null>(null);

  const normalizedComment = useMemo(() => {
    const raw = typeof comment === 'string' ? comment : '';
    return raw.trim();
  }, [comment]);

  useEffect(() => {
    previouslyFocused.current = document.activeElement instanceof HTMLElement ? document.activeElement : null;
    closeBtnRef.current?.focus();

    const onKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        e.preventDefault();
        onClose();
        return;
      }

      if (e.key !== 'Tab') return;
      const modal = modalRef.current;
      if (!modal) return;

      const focusables = getFocusableElements(modal);
      if (focusables.length === 0) return;

      const first = focusables[0];
      const last = focusables[focusables.length - 1];
      const active = document.activeElement;

      if (e.shiftKey) {
        if (active === first || !modal.contains(active)) {
          e.preventDefault();
          last.focus();
        }
      } else {
        if (active === last) {
          e.preventDefault();
          first.focus();
        }
      }
    };

    document.addEventListener('keydown', onKeyDown);
    return () => {
      document.removeEventListener('keydown', onKeyDown);
      previouslyFocused.current?.focus();
    };
  }, [onClose]);

  return (
    <div
      className="cvm-overlay"
      onMouseDown={(e) => {
        if (e.target === e.currentTarget) onClose();
      }}
    >
      <div
        ref={modalRef}
        className="cvm-modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
      >
        <div className="cvm-header">
          <h3 id={titleId} className="cvm-title">
            {title}
          </h3>
          <button ref={closeBtnRef} className="cvm-close" type="button" onClick={onClose} aria-label="Cerrar">
            ✕
          </button>
        </div>

        <div className="cvm-content">
          {normalizedComment ? (
            <div className="cvm-comment-box">
              <p className="cvm-comment-label">Explicación:</p>
              <p className="cvm-comment-text">{normalizedComment}</p>
            </div>
          ) : (
            <p className="cvm-empty">No hay comentarios disponibles</p>
          )}
        </div>

        <div className="cvm-footer">
          <button className="cvm-btn" type="button" onClick={onClose}>
            Cerrar
          </button>
        </div>
      </div>
    </div>
  );
}
