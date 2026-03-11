import { useRef, useState } from 'react';
import { apiFetch } from '../../utils/api';
import './GenerarIAModal.css';

export interface GenerarIAModalProps {
  /** Tipo de actividad que se envía al backend: "TEORIA" | "TEST" | "ORDEN" */
  tipoActividad: string;
  open: boolean;
  onClose: () => void;
  /** Callback con el JSON parseado de la respuesta de la IA */
  onResult: (data: Record<string, unknown>) => void;
}

const TIMEOUT_MS = 120_000; // 2 min

function cleanJsonResponse(text: string): string {
  return text
    .replace(/^```(?:json)?\n?/, '')
    .replace(/\n?```$/, '')
    .trim();
}

export default function GenerarIAModal({
  tipoActividad,
  open,
  onClose,
  onResult,
}: GenerarIAModalProps) {
  const [prompt, setPrompt] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const abortRef = useRef<AbortController | null>(null);

  if (!open) return null;

  const handleGenerate = async () => {
    if (!prompt.trim()) {
      setError('Escribe una descripción para generar la actividad.');
      return;
    }

    setError('');
    setLoading(true);

    const controller = new AbortController();
    abortRef.current = controller;

    const timeoutId = setTimeout(() => controller.abort(), TIMEOUT_MS);

    try {
      const apiBase = (import.meta.env.VITE_API_URL ?? '').trim().replace(/\/$/, '');

      const res = await apiFetch(`${apiBase}/api/iaconnection/generar`, {
        method: 'POST',
        body: JSON.stringify({
          tipoActividad,
          prompt: prompt.trim(),
        }),
        signal: controller.signal,
      });

      const raw = await res.text();
      const cleaned = cleanJsonResponse(raw);
      const data = JSON.parse(cleaned);

      onResult(data);
      setPrompt('');
      onClose();
    } catch (err: unknown) {
      if (err instanceof DOMException && err.name === 'AbortError') {
        setError('La petición fue cancelada.');
      } else if (err instanceof TypeError && err.message.includes('fetch')) {
        setError('Error de conexión. Comprueba tu red e inténtalo de nuevo.');
      } else {
        const msg = err instanceof Error ? err.message : 'Error generando la actividad.';
        setError(msg);
      }
    } finally {
      clearTimeout(timeoutId);
      abortRef.current = null;
      setLoading(false);
    }
  };

  const handleCancel = () => {
    if (abortRef.current) {
      abortRef.current.abort();
    }
    setError('');
    setPrompt('');
    onClose();
  };

  return (
    <div className="iam-overlay" onMouseDown={(e) => { if (e.target === e.currentTarget) handleCancel(); }}>
      <div className="iam-modal" role="dialog" aria-modal="true">
        <h2 className="iam-title">Generar con IA</h2>

        <label style={{ fontWeight: 'bold', fontSize: '0.95rem' }}>
          Describe de qué trata la actividad
        </label>
        <textarea
          className="iam-textarea"
          placeholder="Ej: Una actividad sobre las capitales de Europa..."
          value={prompt}
          onChange={(e) => setPrompt(e.target.value)}
          disabled={loading}
          autoFocus
        />

        <div className="iam-warning">
          <span className="iam-warning-icon">!</span>
          <span>
            La IA puede cometer errores. Revisa siempre el contenido generado antes de guardar
            la actividad.
          </span>
        </div>

        {error && <p className="iam-error">{error}</p>}

        {loading && (
          <div className="iam-loading">
            <div className="iam-spinner" />
            <span className="iam-loading-text">Generando actividad… Puedes cancelar en cualquier momento.</span>
          </div>
        )}

        <div className="iam-buttons">
          <button type="button" className="iam-btn iam-btn-cancel" onClick={handleCancel}>
            Cancelar
          </button>
          <button
            type="button"
            className="iam-btn iam-btn-generate"
            onClick={handleGenerate}
            disabled={loading || !prompt.trim()}
          >
            {loading ? 'Generando...' : 'Generar'}
          </button>
        </div>
      </div>
    </div>
  );
}
