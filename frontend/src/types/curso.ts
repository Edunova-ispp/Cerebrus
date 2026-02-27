export interface Curso {
  id: number;
  titulo: string;
  descripcion: string | null;
  imagen: string | null;
  codigo: string;
  visibilidad: boolean;
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
