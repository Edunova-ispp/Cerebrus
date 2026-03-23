import caballeroImg from '../../assets/props/caballeroGanador.png';
import mounstruoImg from '../../assets/props/mounstroMuerto.png';
import './CompletionPopup.css';

interface Props {
  readonly title: string;
  readonly subtitle?: string;
  readonly onContinue: () => void;
  readonly children?: React.ReactNode;
}

export default function CompletionPopup({ title, subtitle, onContinue, children }: Props) {
  return (
    <div className="cp-done-overlay">
      <div className="cp-done-popup">
        <h2 className="cp-done-title">{title}</h2>
        {subtitle && <p className="cp-done-subtitle">{subtitle}</p>}
        {children}
        <div className="cp-done-imgs">
          <img src={caballeroImg} alt="Caballero ganador" className="cp-done-caballero" />
          <img src={mounstruoImg} alt="Monstruo derrotado" className="cp-done-monstruo" />
        </div>
        <button className="cp-done-btn" type="button" onClick={onContinue}>Continuar</button>
      </div>
    </div>
  );
}
