import { useEffect, useMemo, useState } from 'react';
import { apiFetch } from './api';

export interface RepeticionesActividadDTO {
  repeticionesMedia: number;
  repeticionesMinima: number;
  repeticionesMaxima: number;
}

type RepeticionesPorActividad = Record<string, RepeticionesActividadDTO>;

function toNumberId(value: string | number | undefined | null): number | null {
  if (value == null) return null;
  const num = typeof value === 'number' ? value : Number.parseInt(String(value), 10);
  return Number.isFinite(num) ? num : null;
}

export function useActividadIntentosTerminados(params: {
  cursoId?: string | number;
  temaId?: string | number;
  actividadId?: number;
  enabled?: boolean;
}): {
  hasIntentosTerminados: boolean | null;
  loading: boolean;
  error: string | null;
} {
  const { cursoId, temaId, actividadId, enabled = true } = params;

  const cursoIdNum = useMemo(() => toNumberId(cursoId), [cursoId]);
  const temaIdNum = useMemo(() => toNumberId(temaId), [temaId]);

  const [hasIntentosTerminados, setHasIntentosTerminados] = useState<boolean | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!enabled) return;

    if (!cursoIdNum || !temaIdNum || !actividadId) {
      setHasIntentosTerminados(null);
      setError(null);
      return;
    }

    let cancelled = false;

    async function run() {
      setLoading(true);
      setError(null);

      try {
        const apiBase = (import.meta.env.VITE_API_URL ?? '').trim().replace(/\/$/, '');
        const res = await apiFetch(
          `${apiBase}/api/estadisticas/cursos/${cursoIdNum}/temas/${temaIdNum}/repeticiones-actividades`
        );

        const json = (await res.json()) as RepeticionesPorActividad;
        const dto = json?.[String(actividadId)];
        const max = Number(dto?.repeticionesMaxima ?? 0);
        const has = Number.isFinite(max) && max > 0;

        if (!cancelled) setHasIntentosTerminados(has);
      } catch (e) {
        const msg = e instanceof Error ? e.message : 'No se pudo comprobar si existen intentos.';
        if (!cancelled) {
          setHasIntentosTerminados(null);
          setError(msg);
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    run();

    return () => {
      cancelled = true;
    };
  }, [actividadId, cursoIdNum, enabled, temaIdNum]);

  return { hasIntentosTerminados, loading, error };
}
