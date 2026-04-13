import { useMemo, useState } from 'react';
import ActivityGuideModal from '../ActivityGuideModal/ActivityGuideModal';
import { getActivityGuide } from '../../utils/activityGuide';
import type { ActivityGuideRole } from '../../utils/activityGuide';
import './ActivityGuideButton.css';

interface Props {
  readonly activityType: string;
  readonly role: ActivityGuideRole;
  readonly buttonLabel?: string;
}

const ACTIVITY_LABELS: Record<string, string> = {
  general: 'General',
  teoria: 'Teoria',
  test: 'Tipo test',
  ordenacion: 'Ordenacion',
  'marcar-imagen': 'Marcar en imagen',
  tablero: 'Tablero',
  clasificacion: 'Clasificacion',
  carta: 'Carta',
  crucigrama: 'Crucigrama',
  'pregunta-abierta': 'Pregunta abierta',
};

function formatActivityLabel(activityType: string): string {
  const normalized = activityType.trim().toLowerCase();
  if (ACTIVITY_LABELS[normalized]) {
    return ACTIVITY_LABELS[normalized];
  }

  const clean = normalized.replace(/[-_]+/g, ' ').trim();
  return clean.charAt(0).toUpperCase() + clean.slice(1);
}

export default function ActivityGuideButton({ activityType, role, buttonLabel = 'Ayuda' }: Props) {
  const [open, setOpen] = useState(false);
  const guide = useMemo(() => getActivityGuide(activityType, role), [activityType, role]);

  const roleLabel = role === 'maestro' ? 'Maestro' : 'Alumno';
  const activityLabel = useMemo(() => formatActivityLabel(activityType), [activityType]);

  return (
    <>
      <button type="button" className={`agb-btn agb-btn--${role}`} onClick={() => setOpen(true)}>
        {buttonLabel}
      </button>
      <ActivityGuideModal
        open={open}
        role={role}
        title={`Tutorial para ${roleLabel}`}
        subtitle={`Actividad: ${activityLabel}`}
        content={guide}
        onClose={() => setOpen(false)}
      />
    </>
  );
}
