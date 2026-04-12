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
    if (line.startsWith('## ')) {
      const activityName = normalizeKey(line.slice(3));
      if (!activityName) {
        currentType = null;
        currentRole = null;
        continue;
      }

      currentType = activityName;
      if (!guides[currentType]) {
        guides[currentType] = {};
      }
      currentRole = null;
      continue;
    }

    if (line.startsWith('### ')) {
      const rawRole = normalizeKey(line.slice(4));
      if (rawRole !== 'maestro' && rawRole !== 'alumno') {
        currentRole = null;
        continue;
      }

      currentRole = rawRole;
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
