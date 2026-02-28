export interface Curso {
  id: number;
  titulo: string;
  descripcion: string | null;
  imagen: string | null;
  codigo: string;
  visibilidad: boolean;
}

export interface InscripcionResumen {
  cursoId: number;
  puntos: number;
}

/** Decodifica el JWT y te mira que rol tiene y te lo saca */
export function getCurrentUserRoles(): string[] {
  const token = localStorage.getItem("token");
  if (!token) return [];
  try {
    const payload = JSON.parse(atob(token.split(".")[1]));
    const raw = payload.authorities ?? payload.roles ?? [];
    return Array.isArray(raw) ? raw : [];
  } catch {
    return [];
  }
}

/** Decodifica el JWT y devuelve el payload completo (sub, roles, exp, ...) */
export function getCurrentUserInfo(): Record<string, unknown> | null {
  const token = localStorage.getItem("token");
  if (!token) return null;
  try {
    return JSON.parse(atob(token.split(".")[1]));
  } catch {
    return null;
  }
}
