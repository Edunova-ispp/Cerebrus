import { useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { apiFetch } from '../../utils/api';
import './SimuladorPago.css';

// ── Utilidades de validación ──────────────────────────────────────────────────

function luhn(numero: string): boolean {
  const digits = numero.replace(/\s/g, '').split('').reverse().map(Number);
  const sum = digits.reduce((acc, d, i) => {
    if (i % 2 === 1) { d *= 2; if (d > 9) d -= 9; }
    return acc + d;
  }, 0);
  return sum % 10 === 0;
}

function validarCaducidad(caducidad: string): string | null {
  const match = caducidad.match(/^(\d{2})\/(\d{2})$/);
  if (!match) return 'Formato inválido (MM/AA)';
  const mes = parseInt(match[1], 10);
  const anio = parseInt(match[2], 10) + 2000;
  if (mes < 1 || mes > 12) return 'Mes inválido';
  const hoy = new Date();
  const expiry = new Date(anio, mes - 1, 1);
  const inicioMesActual = new Date(hoy.getFullYear(), hoy.getMonth(), 1);
  if (expiry < inicioMesActual) return 'Tarjeta caducada';
  return null;
}

function validarTarjeta(numero: string): string | null {
  const raw = numero.replace(/\s/g, '');
  if (raw.length < 13) return 'Número demasiado corto';
  if (raw.length > 19) return 'Número demasiado largo';
  if (!luhn(raw)) return 'Número de tarjeta inválido';
  return null;
}

function validarCVV(cvv: string): string | null {
  if (cvv.length < 3) return 'CVV demasiado corto';
  return null;
}

function validarTitular(titular: string): string | null {
  if (titular.trim().length < 3) return 'Nombre demasiado corto';
  if (!/^[A-Z\s'-]+$/.test(titular)) return 'Solo letras y espacios';
  return null;
}

// ── Componente ────────────────────────────────────────────────────────────────

export default function SimuladorPago() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  const transaccionId = searchParams.get('txn') ?? '';
  const suscripcionId = searchParams.get('susId') ?? '';

  const [procesando, setProcesando] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [flipped, setFlipped] = useState(false);

  const [numeroTarjeta, setNumeroTarjeta] = useState('4242 4242 4242 4242');
  const [titular, setTitular] = useState('TEST USER');
  const [caducidad, setCaducidad] = useState('12/29');
  const [cvv, setCvv] = useState('123');

  const [errores, setErrores] = useState<Record<string, string | null>>({});

  const apiBase = (import.meta.env.VITE_API_URL ?? '').trim().replace(/\/$/, '');

  const handleNumeroTarjeta = (e: React.ChangeEvent<HTMLInputElement>) => {
    const raw = e.target.value.replace(/\D/g, '').slice(0, 16);
    const formatted = raw.match(/.{1,4}/g)?.join(' ') ?? raw;
    setNumeroTarjeta(formatted);
    setErrores(prev => ({ ...prev, numeroTarjeta: validarTarjeta(formatted) }));
  };

  const handleCaducidad = (e: React.ChangeEvent<HTMLInputElement>) => {
    const raw = e.target.value.replace(/\D/g, '').slice(0, 4);
    const formatted = raw.length > 2 ? `${raw.slice(0, 2)}/${raw.slice(2)}` : raw;
    setCaducidad(formatted);
    if (formatted.length === 5) {
      setErrores(prev => ({ ...prev, caducidad: validarCaducidad(formatted) }));
    }
  };

  const handleCVV = (e: React.ChangeEvent<HTMLInputElement>) => {
    const val = e.target.value.replace(/\D/g, '').slice(0, 4);
    setCvv(val);
    setErrores(prev => ({ ...prev, cvv: validarCVV(val) }));
  };

  const handleTitular = (e: React.ChangeEvent<HTMLInputElement>) => {
    const val = e.target.value.toUpperCase();
    setTitular(val);
    setErrores(prev => ({ ...prev, titular: validarTitular(val) }));
  };

  const validarTodo = (): boolean => {
    const nuevosErrores = {
      numeroTarjeta: validarTarjeta(numeroTarjeta),
      caducidad: validarCaducidad(caducidad),
      cvv: validarCVV(cvv),
      titular: validarTitular(titular),
    };
    setErrores(nuevosErrores);
    return Object.values(nuevosErrores).every(e => e === null);
  };

  if (!transaccionId || !suscripcionId) {
    return (
      <div className="sim-page">
        <div className="sim-card">
          <p className="sim-error">Enlace de pago inválido. Faltan parámetros requeridos.</p>
          <button className="sim-btn sim-btn--secondary" onClick={() => navigate('/suscripcion')}>
            Volver a suscripción
          </button>
        </div>
      </div>
    );
  }

  const handleSimularPago = async () => {
    if (!validarTodo()) {
      setError('Corrige los errores del formulario antes de continuar.');
      return;
    }
    try {
      setProcesando(true);
      setError(null);
      const res = await apiFetch(
        `${apiBase}/api/suscripciones/confirmar-pago/${transaccionId}`,
        { method: 'POST' }
      );
      if (!res.ok) throw new Error(`Error ${res.status}`);
      navigate('/suscripcion?resultado=exito', { replace: true });
    } catch (err) {
      console.error(err);
      setError('El servidor no pudo confirmar el pago. Inténtalo de nuevo.');
    } finally {
      setProcesando(false);
    }
  };

  const handleSimularFallo = async () => {
    try {
      setProcesando(true);
      setError(null);
      await apiFetch(
        `${apiBase}/api/suscripciones/cancelar-pago/${transaccionId}`,
        { method: 'POST' }
      );
    } catch {
      // ignorado
    } finally {
      setProcesando(false);
      navigate('/suscripcion?resultado=cancelado', { replace: true });
    }
  };

  const hayErrores = Object.values(errores).some(e => e !== null);

  return (
    <div className="sim-page">
      <div className="sim-card">

        {/* Cabecera */}
        <div className="sim-header">
          <div className="sim-bank-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <rect x="2" y="7" width="20" height="14" rx="2" />
              <path d="M16 3H8L2 7h20l-6-4z" />
              <line x1="2" y1="11" x2="22" y2="11" />
            </svg>
          </div>
          <h1 className="sim-title">Pasarela de pago</h1>
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

        {/* Tarjeta visual con flip */}
        <div className={`sim-card-3d${flipped ? ' sim-card-3d--flipped' : ''}`}>
          <div className="sim-card-3d__inner">

            {/* Cara delantera */}
            <div className="sim-card-visual sim-card-3d__front">
              <div className="sim-card-visual__top">
                <div className="sim-card-visual__chip" />
                <div className="sim-card-visual__network">
                  <div className="sim-card-visual__circle sim-card-visual__circle--front" />
                  <div className="sim-card-visual__circle sim-card-visual__circle--back" />
                </div>
              </div>
              <div className="sim-card-visual__number">
                {numeroTarjeta || '•••• •••• •••• ••••'}
              </div>
              <div className="sim-card-visual__bottom">
                <div>
                  <p className="sim-card-visual__field-label">Titular</p>
                  <p className="sim-card-visual__field-value">{titular || 'NOMBRE APELLIDO'}</p>
                </div>
                <div>
                  <p className="sim-card-visual__field-label">Caducidad</p>
                  <p className="sim-card-visual__field-value">{caducidad || 'MM/AA'}</p>
                </div>
              </div>
            </div>

            {/* Cara trasera */}
            <div className="sim-card-visual sim-card-3d__back">
              <div className="sim-card-visual__stripe" />
              <div className="sim-card-visual__cvv-area">
                <span className="sim-card-visual__field-label">CVV</span>
                <div className="sim-card-visual__cvv-box">
                  {'•'.repeat(cvv.length) || '•••'}
                </div>
              </div>
            </div>

          </div>
        </div>

        {/* Formulario */}
        <div className="sim-form">

          <div className="sim-form__field">
            <label className="sim-form__label">Número de tarjeta</label>
            <input
              type="text"
              className={`sim-form__input${errores.numeroTarjeta ? ' sim-form__input--error' : ''}`}
              value={numeroTarjeta}
              onChange={handleNumeroTarjeta}
              onFocus={() => setFlipped(false)}
              placeholder="4242 4242 4242 4242"
              maxLength={19}
            />
            {errores.numeroTarjeta && <span className="sim-form__error">{errores.numeroTarjeta}</span>}
          </div>

          <div className="sim-form__field">
            <label className="sim-form__label">Titular</label>
            <input
              type="text"
              className={`sim-form__input${errores.titular ? ' sim-form__input--error' : ''}`}
              value={titular}
              onChange={handleTitular}
              onFocus={() => setFlipped(false)}
              placeholder="NOMBRE APELLIDO"
              maxLength={26}
            />
            {errores.titular && <span className="sim-form__error">{errores.titular}</span>}
          </div>

          <div className="sim-form__row">
            <div className="sim-form__field">
              <label className="sim-form__label">Caducidad</label>
              <input
                type="text"
                className={`sim-form__input${errores.caducidad ? ' sim-form__input--error' : ''}`}
                value={caducidad}
                onChange={handleCaducidad}
                onFocus={() => setFlipped(false)}
                placeholder="MM/AA"
                maxLength={5}
              />
              {errores.caducidad && <span className="sim-form__error">{errores.caducidad}</span>}
            </div>
            <div className="sim-form__field">
              <label className="sim-form__label">CVV</label>
              <input
                type="password"
                className={`sim-form__input${errores.cvv ? ' sim-form__input--error' : ''}`}
                value={cvv}
                onChange={handleCVV}
                onFocus={() => setFlipped(true)}
                onBlur={() => setFlipped(false)}
                placeholder="123"
                maxLength={4}
              />
              {errores.cvv && <span className="sim-form__error">{errores.cvv}</span>}
            </div>
          </div>
        </div>

        <p className="sim-fake-note">
          ⚠️ Datos ficticios. Ninguna transacción real se procesa.
        </p>

        {error && <p className="sim-error">{error}</p>}

        <hr className="sim-divider" />

        <div className="sim-actions">
          <button
            className="sim-btn sim-btn--success"
            onClick={handleSimularPago}
            disabled={procesando || hayErrores}
          >
            {procesando ? 'Procesando…' : '✓ Pago exitoso'}
          </button>
          <button
            className="sim-btn sim-btn--danger"
            onClick={handleSimularFallo}
            disabled={procesando}
          >
            ✕ Pago fallido
          </button>
        </div>

      </div>
    </div>
  );
}