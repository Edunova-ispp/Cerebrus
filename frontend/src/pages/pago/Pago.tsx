import { useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { apiFetch } from '../../utils/api';
import './SimuladorPago.css';

export default function SimuladorPago() {
  const [searchParams] = useSearchParams();
  const navigate       = useNavigate();

  // Los query params que genera tu MockPagoService
  // Ej: /simulador-pago?txn=txn_mock_123&susId=45
  const transaccionId  = searchParams.get('txn')   ?? '';
  const suscripcionId  = searchParams.get('susId') ?? '';

  const [procesando, setProcesando] = useState(false);
  const [error, setError]           = useState<string | null>(null);

  const apiBase = (import.meta.env.VITE_API_URL ?? '').trim().replace(/\/$/, '');

  // Parámetros inválidos → pantalla de error
  if (!transaccionId || !suscripcionId) {
    return (
      <div className="sim-page">
        <div className="sim-card">
          <p className="sim-error">
            ❌ Enlace de pago inválido. Faltan parámetros requeridos.
          </p>
          <button className="sim-btn sim-btn--secondary" onClick={() => navigate('/suscripcion')}>
            Volver a suscripción
          </button>
        </div>
      </div>
    );
  }

  // ── Confirmar pago exitoso ─────────────────────────────
  const handleSimularPago = async () => {
    try {
      setProcesando(true);
      setError(null);

      const res = await apiFetch(
        `${apiBase}/api/suscripciones/confirmar-pago/${transaccionId}`,
        { method: 'POST' }
      );

      if (!res.ok) throw new Error(`Error ${res.status}`);

      // Pago confirmado → volvemos a la app (suscripción ya activa)
      navigate('/suscripcion', { replace: true });
    } catch (err) {
      console.error(err);
      setError('El servidor no pudo confirmar el pago. Inténtalo de nuevo.');
    } finally {
      setProcesando(false);
    }
  };

  // ── Simular pago fallido ───────────────────────────────
  const handleSimularFallo = async () => {
    try {
      setProcesando(true);
      setError(null);

      await apiFetch(
        `${apiBase}/api/suscripciones/cancelar-pago/${transaccionId}`,
        { method: 'POST' }
      );
    } catch {
      // Ignoramos el error: si el endpoint no existe, igual volvemos
    } finally {
      setProcesando(false);
      navigate('/suscripcion', { replace: true });
    }
  };

  return (
    <div className="sim-page">
      <div className="sim-card">

        {/* Cabecera */}
        <div className="sim-header">
          <span className="sim-bank-icon">🏦</span>
          <h1 className="sim-title">Pasarela de Pago</h1>
          <p className="sim-subtitle">Simulador — entorno de pruebas</p>
        </div>

        {/* Info transacción */}
        <div className="sim-info">
          <div className="sim-info__row">
            <span className="sim-info__label">ID Suscripción</span>
            <span className="sim-info__value">#{suscripcionId}</span>
          </div>
          <div className="sim-info__row">
            <span className="sim-info__label">Referencia</span>
            <span className="sim-info__value sim-info__value--mono">{transaccionId}</span>
          </div>
        </div>

        {/* Formulario de tarjeta falso */}
        <div className="sim-fake-form">
          <p className="sim-fake-form__label">Datos de tarjeta</p>

          <div className="sim-fake-field">
            <label>Número de tarjeta</label>
            <input
              type="text"
              className="sim-fake-input"
              defaultValue="4242 4242 4242 4242"
              readOnly
            />
          </div>

          <div className="sim-fake-row">
            <div className="sim-fake-field">
              <label>Caducidad</label>
              <input type="text" className="sim-fake-input" defaultValue="12/99" readOnly />
            </div>
            <div className="sim-fake-field">
              <label>CVV</label>
              <input type="text" className="sim-fake-input" defaultValue="123" readOnly />
            </div>
          </div>

          <p className="sim-fake-note">
            Estos datos son ficticios. Ninguna transacción real se procesa.
          </p>
        </div>

        {error && <p className="sim-error">{error}</p>}

        {/* Botones */}
        <div className="sim-actions">
          <button
            className="sim-btn sim-btn--success"
            onClick={handleSimularPago}
            disabled={procesando}
          >
            {procesando ? 'Procesando…' : 'Pago Exitoso'}
          </button>
          <button
            className="sim-btn sim-btn--danger"
            onClick={handleSimularFallo}
            disabled={procesando}
          >
            Pago Fallido
          </button>
        </div>

      </div>
    </div>
  );
}