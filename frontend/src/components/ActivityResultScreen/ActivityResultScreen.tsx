import caballeroImg from '../../assets/props/caballeroGanador.png';
import mounstruoImg from '../../assets/props/mounstroMuerto.png';
import './ActivityResultScreen.css';

export interface ActivityResultConfig {
  readonly showScore: boolean;
  readonly allowRetry: boolean;
  readonly showCorrectAnswer: boolean;
  readonly showStudentAnswer: boolean;
}

interface Props {
  readonly title: string;
  readonly score?: number;
  readonly maxScore?: number;
  readonly grade?: number;
  readonly detail?: string;
  readonly config: ActivityResultConfig;
  readonly onContinue: () => void;
  readonly onRetry?: () => void;
  readonly onViewStudentAnswer?: () => void;
  readonly onViewCorrectAnswer?: () => void;
  readonly onCancel?: () => void;
}

export default function ActivityResultScreen({
  title,
  score = 0,
  maxScore = 100,
  grade,
  detail,
  config,
  onContinue,
  onRetry,
  onViewStudentAnswer,
  onViewCorrectAnswer,
  onCancel,
}: Props) {
  const notaSobre10 = typeof grade === 'number'
    ? grade
    : maxScore > 0
      ? Math.round((score / maxScore) * 100) / 10
      : 0;

  return (
    <div
      className="ars-overlay"
      onMouseDown={(e) => {
        if (e.target === e.currentTarget && onCancel) onCancel();
      }}
    >
      <div className="ars-popup">
        <h2 className="ars-title">{title}</h2>

        {/* Mostrar puntuación si está configurado */}
        {config.showScore && (
          <div className="ars-score-section">
            <p className="ars-score-label">Tu puntuación:</p>
            <div className="ars-score-display">
              <span className="ars-score-value">{score}</span>
              <span className="ars-score-max">/ {maxScore}</span>
            </div>
            <div className="ars-score-grade">Nota: {notaSobre10.toFixed(1)} / 10</div>
            {detail && <div className="ars-score-detail">{detail}</div>}
          </div>
        )}

        {/* Imágenes de celebración */}
        <div className="ars-imgs">
          <img src={caballeroImg} alt="Caballero ganador" className="ars-caballero" />
          <img src={mounstruoImg} alt="Monstruo derrotado" className="ars-monstruo" />
        </div>

        {/* Botones de acciones */}
        <div className="ars-actions">
          {/* Botón Ver mi respuesta */}
          {config.showStudentAnswer && onViewStudentAnswer && (
            <button className="ars-btn ars-btn-secondary" type="button" onClick={onViewStudentAnswer}>
              Ver mi respuesta
            </button>
          )}

          {/* Botón Ver respuesta correcta */}
          {config.showCorrectAnswer && onViewCorrectAnswer && (
            <button className="ars-btn ars-btn-secondary" type="button" onClick={onViewCorrectAnswer}>
              Ver respuesta correcta
            </button>
          )}

          {/* Botón Repetir */}
          {config.allowRetry && onRetry && (
            <button className="ars-btn ars-btn-secondary" type="button" onClick={onRetry}>
              Repetir actividad
            </button>
          )}

          {/* Botón Continuar */}
          <button className="ars-btn ars-btn-primary" type="button" onClick={onContinue}>
            Continuar
          </button>
        </div>
      </div>
    </div>
  );
}
