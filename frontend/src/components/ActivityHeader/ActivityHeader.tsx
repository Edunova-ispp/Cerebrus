import { useNavigate } from 'react-router-dom';
import mapaIcon from '../../assets/icons/mapa.svg';
import ActivityGuideButton from '../ActivityGuideButton/ActivityGuideButton';
import type { ActivityGuideRole } from '../../utils/activityGuide';
import './ActivityHeader.css';

interface Props {
  title: string;
  subtitle?: string;
  guideType?: string;
  guideRole?: ActivityGuideRole;
}

export default function ActivityHeader({ title, subtitle, guideType, guideRole }: Props) {
  const navigate = useNavigate();

  return (
    <div className="ah-wrapper">
      <div className="ah-top">
        <button className="ah-map-btn" type="button" onClick={() => navigate(-1)}>
          <img src={mapaIcon} alt="Mapa" className="ah-map-icon" />
          <span>Mapa</span>
        </button>
        <div className="ah-title-banner">
          <h1 className="ah-title">{title}</h1>
        </div>
        {guideType && guideRole && <ActivityGuideButton activityType={guideType} role={guideRole} />}
      </div>
      {subtitle && <p className="ah-subtitle">{subtitle}</p>}
    </div>
  );
}
