const apiBase = (import.meta.env.VITE_API_URL ?? "").trim().replace(/\/$/, "");
export async function toggleVisibilidadCurso(id: number): Promise<import('../types/curso').Curso> {
  const res = await apiFetch(`${apiBase}/api/cursos/${id}/visibilidad`, { method: "PATCH" });
  return res.json();
}

export async function fetchProgresoAlumno(cursoId: number): Promise<import('../types/curso').ProgresoAlumno> {
  const res = await apiFetch(`${apiBase}/api/cursos/${cursoId}/progreso`);
  return res.json();
}

export async function apiFetch(path: string, options: RequestInit = {}): Promise<Response> {
  const token = localStorage.getItem("token");

  const headers: HeadersInit = {
    "Content-Type": "application/json",
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...(options.headers as Record<string, string> | undefined),
  };

  const normalizedPath = path ? path : `/${path}`;
  const res = await fetch(`${normalizedPath}`, { ...options, headers });

  if (!res.ok) {
    throw new Error(`${options.method ?? "GET"} ${path} → ${res.status}`);
  }

  return res;
}
