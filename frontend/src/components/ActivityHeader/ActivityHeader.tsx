import { useNavigate } from 'react-router-dom';
import mapaIcon from '../../assets/icons/mapa.svg';
import './ActivityHeader.css';

interface Props {
  title: string;
  subtitle?: string;
}

export default function ActivityHeader({ title, subtitle }: Props) {
  const navigate = useNavigate();

  return (
    <div className="ah-top">
      <button className="ah-map-btn" type="button" onClick={() => navigate(-1)}>
        <img src={mapaIcon} alt="Mapa" className="ah-map-icon" />
        <span>Mapa</span>
      </button>
      <div className="ah-title-banner">
        <h1 className="ah-title">{title}</h1>
        {subtitle && <p className="ah-subtitle">{subtitle}</p>}
      </div>
    </div>
  );
}
