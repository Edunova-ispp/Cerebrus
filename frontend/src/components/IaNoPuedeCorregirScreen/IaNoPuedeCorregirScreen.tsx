import './IaNoPuedeCorregirScreen.css';

interface Props {
  readonly onContinue: () => void;
}

export default function IaNoPuedeCorregirScreen({ onContinue }: Props) {
  return (
    <div className="ia-no-corr-overlay">
      <div className="ia-no-corr-popup">
        <h2 className="ia-no-corr-title">¡LA IA NO PUEDE CORREGIR!</h2>
        <p className="ia-no-corr-desc">
          La IA no puede corregir tu actividad ahora mismo. ¡Pero no te preocupes! Podrás continuar avanzando por el mapa. 
        </p>
        <p className="ia-no-corr-desc">
          Por ahora obtienes la mitad de la nota y de la puntuación máxima, pero esto es solo temporal. 
        </p>
        <p className="ia-no-corr-desc">
          <strong>Inténtalo más tarde o espera a que tu profesor corrija tu actividad.</strong>
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
