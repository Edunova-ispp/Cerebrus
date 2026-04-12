import { useEffect } from 'react';
import './ActivityGuideModal.css';

interface Props {
  readonly open: boolean;
  readonly title: string;
  readonly subtitle?: string;
  readonly content: string;
  readonly onClose: () => void;
}

export default function ActivityGuideModal({ open, title, subtitle, content, onClose }: Props) {
  useEffect(() => {
    if (!open) return;

    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        onClose();
      }
    };

    document.addEventListener('keydown', onKeyDown);
    return () => document.removeEventListener('keydown', onKeyDown);
  }, [open, onClose]);

  if (!open) return null;

  return (
    <div
      className="agm-overlay"
      role="dialog"
      aria-modal="true"
      aria-label={title}
      onMouseDown={(event) => {
        if (event.target === event.currentTarget) {
          onClose();
        }
      }}
    >
      <div className="agm-card" onMouseDown={(event) => event.stopPropagation()}>
        <div className="agm-header">
          <div className="agm-title-wrap">
            <h2 className="agm-title">{title}</h2>
            {subtitle && <p className="agm-subtitle">{subtitle}</p>}
          </div>
          <button type="button" className="agm-close" aria-label="Cerrar ayuda" onClick={onClose}>
            X
          </button>
        </div>
        <div className="agm-content">{content}</div>
      </div>
    </div>
  );
}
