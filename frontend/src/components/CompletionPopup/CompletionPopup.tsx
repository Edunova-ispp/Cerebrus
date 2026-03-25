import caballeroImg from '../../assets/props/caballeroGanador.png';
import mounstruoImg from '../../assets/props/mounstroMuerto.png';
import './CompletionPopup.css';

interface Props {
  readonly title: string;
  readonly subtitle?: string;
  readonly onContinue: () => void;
  readonly onCancel?: () => void;
  readonly children?: React.ReactNode;
  readonly showContinueButton?: boolean;
  readonly continueLabel?: string;
}

export default function CompletionPopup({
  title,
  subtitle,
  onContinue,
  onCancel,
  children,
  showContinueButton = true,
  continueLabel = 'Continuar'
}: Props) {
  return (
    <div
      className="cp-done-overlay"
      onMouseDown={(e) => {
        if (e.target === e.currentTarget && onCancel) onCancel();
      }}
    >
      <div className="cp-done-popup">
        <h2 className="cp-done-title">{title}</h2>
        {subtitle && <p className="cp-done-subtitle">{subtitle}</p>}
        {children}
        <div className="cp-done-imgs">
          <img src={caballeroImg} alt="Caballero ganador" className="cp-done-caballero" />
          <img src={mounstruoImg} alt="Monstruo derrotado" className="cp-done-monstruo" />
        </div>
        {showContinueButton && (
          <button className="cp-done-btn" type="button" onClick={onContinue}>{continueLabel}</button>
        )}
      </div>
    </div>
  );
}
