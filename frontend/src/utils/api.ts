const API_BASE = () => (import.meta.env.VITE_API_URL ?? "").trim().replace(/\/$/, "");

export async function apiFetch(path: string, options: RequestInit = {}): Promise<Response> {
  const token = localStorage.getItem("token");

  const headers: HeadersInit = {
    "Content-Type": "application/json",
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...(options.headers ?? {}),
  };

  const normalizedPath = path.startsWith("/") ? path : `/${path}`;
  const res = await fetch(`${API_BASE()}${normalizedPath}`, { ...options, headers });

  if (!res.ok) {
    throw new Error(`${options.method ?? "GET"} ${path} → ${res.status}`);
  }

  return res;
}
