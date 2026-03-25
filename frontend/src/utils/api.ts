const apiBase = (import.meta.env.VITE_API_URL ?? "").trim().replace(/\/$/, "");

function getFriendlyStatusMessage(status: number): string {
  switch (status) {
    case 400:
      return 'La solicitud no es válida. Revisa los datos e inténtalo de nuevo.';
    case 401:
      return 'Tu sesión ha caducado o no es válida. Inicia sesión de nuevo.';
    case 403:
      return 'No tienes permisos para realizar esta acción.';
    case 404:
      return 'No se encontró el recurso solicitado.';
    case 409:
      return 'No se pudo completar la acción por un conflicto de datos.';
    case 422:
      return 'Hay datos inválidos en el formulario. Revisa los campos e inténtalo de nuevo.';
    case 429:
      return 'Se han realizado demasiadas solicitudes. Espera un momento e inténtalo de nuevo.';
    case 500:
      return 'Ha ocurrido un error interno. Inténtalo de nuevo en unos minutos.';
    case 502:
    case 503:
    case 504:
      return 'El servicio no está disponible temporalmente. Inténtalo de nuevo más tarde.';
    default:
      if (status >= 400 && status < 500) {
        return 'No se pudo completar la acción. Revisa los datos e inténtalo de nuevo.';
      }
      return 'Ha ocurrido un error inesperado. Inténtalo de nuevo más tarde.';
  }
}

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
    let message = getFriendlyStatusMessage(res.status);
    try {
      const body = await res.clone().json();
      if (body.errores && typeof body.errores === 'object' && !Array.isArray(body.errores)) {
        const fieldErrors = Object.entries(body.errores)
          .map(([field, detail]) => `${field}: ${String(detail)}`)
          .filter(Boolean);
        if (fieldErrors.length > 0) message = fieldErrors.join('. ');
      }
      if (body.errors && Array.isArray(body.errors)) {
        // Spring @Valid: lista de errores de campo (formato array)
        const fieldErrors = body.errors
          .map((e: { field?: string; defaultMessage?: string }) =>
            e.field ? `${e.field}: ${e.defaultMessage}` : e.defaultMessage
          )
          .filter(Boolean);
        if (fieldErrors.length > 0) message = fieldErrors.join('. ');
      } else if (body.errores && typeof body.errores === 'object') {
        // GlobalExceptionHandler 422: {errores: {campo: "mensaje"}, mensaje: "..."}
        // Traducir nombres de campo técnicos a mensajes legibles
        const friendlyNames: Record<string, string> = {
          pregunta: 'La pregunta',
          respuesta: 'La respuesta',
          titulo: 'El título',
          puntuacion: 'La puntuación',
          descripcion: 'La descripción',
          comentariosRespVisible: 'El comentario de corrección',
          imagen: 'La imagen',
          tema: 'El tema',
          correcta: 'La opción correcta',
        };
        const fieldErrors = Object.entries(body.errores)
          .map(([field, msg]) => `${friendlyNames[field] ?? field} ${msg}`)
          .filter(Boolean);
        if (fieldErrors.length > 0) message = fieldErrors.join('. ');
      } else if (body.detail) {
        message = body.detail;
      } else if (body.message || body.mensaje) {
        message = body.message || body.mensaje;
      }
    } catch {
      // body no era JSON, usar mensaje genérico
    }

    if (/^Error\s+\d{3}$/.test(message)) {
      message = getFriendlyStatusMessage(res.status);
    }

    throw new Error(message);
  }

  return res;
}
