import guidesMarkdown from '../content/activity-guides.md?raw';

export type ActivityGuideRole = 'maestro' | 'alumno';

export type ActivityGuideType =
  | 'general'
  | 'teoria'
  | 'test'
  | 'ordenacion'
  | 'marcar-imagen'
  | 'tablero'
  | 'clasificacion'
  | 'carta'
  | 'crucigrama'
  | 'pregunta-abierta';

type GuideMap = Record<string, Partial<Record<ActivityGuideRole, string>>>;

function normalizeKey(value: string): string {
  return value.trim().toLowerCase();
}

function parseGuides(markdown: string): GuideMap {
  const lines = markdown.split(/\r?\n/);
  const guides: GuideMap = {};

  let currentType: string | null = null;
  let currentRole: ActivityGuideRole | null = null;

  for (const line of lines) {
    const activityMatch = line.match(/^##\s+(.+)$/);
    if (activityMatch) {
      currentType = normalizeKey(activityMatch[1]);
      if (!guides[currentType]) {
        guides[currentType] = {};
      }
      currentRole = null;
      continue;
    }

    const roleMatch = line.match(/^###\s+(maestro|alumno)\s*$/i);
    if (roleMatch) {
      currentRole = normalizeKey(roleMatch[1]) as ActivityGuideRole;
      const previous = guides[currentType ?? 'general']?.[currentRole] ?? '';
      if (!guides[currentType ?? 'general']) {
        guides[currentType ?? 'general'] = {};
      }
      guides[currentType ?? 'general'][currentRole] = previous;
      continue;
    }

    if (!currentType || !currentRole) {
      continue;
    }

    const current = guides[currentType][currentRole] ?? '';
    guides[currentType][currentRole] = `${current}${line}\n`;
  }

  return guides;
}

const parsedGuides = parseGuides(guidesMarkdown);

export function getActivityGuide(type: string, role: ActivityGuideRole): string {
  const normalizedType = normalizeKey(type);
  const exact = parsedGuides[normalizedType]?.[role]?.trim();
  if (exact) return exact;

  const fallback = parsedGuides.general?.[role]?.trim();
  if (fallback) return fallback;

  return 'Contenido de ayuda no disponible por ahora.';
}
