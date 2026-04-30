import './IaNoPuedeCorregirScreen.css';

interface Props {
  readonly onContinue: () => void;
  readonly allowRetry: boolean;
}

export default function IaNoPuedeCorregirScreen({ onContinue, allowRetry }: Props) {
  return (
    <div className="ia-no-corr-overlay">
      <div className="ia-no-corr-popup">
        <h2 className="ia-no-corr-title">¡LA IA NO PUEDE CORREGIR!</h2>
        <p className="ia-no-corr-desc">
          La IA no puede corregir tu actividad ahora mismo. ¡Pero no te preocupes! <strong>Tus respuestas quedan guardadas.</strong>
        </p>
        <p className="ia-no-corr-desc">
          Por ahora obtienes la mitad de la nota y de la puntuación máxima, pero esto es solo <strong>temporal</strong>. 
        </p>
        <p className="ia-no-corr-desc">
          {allowRetry ? (
            <strong>Puedes intentarlo de nuevo más tarde o esperar a que el profesor corrija tu intento.</strong>
          ) : (
            <strong>El profesor corregirá tu intento.</strong>
          )}
        </p>

        <div className="ia-no-corr-actions">
          <button type="button" className="ia-no-corr-btn ia-no-corr-btn-primary" onClick={onContinue}>
            Continuar
          </button>
        </div>
      </div>
    </div>
  );
}
