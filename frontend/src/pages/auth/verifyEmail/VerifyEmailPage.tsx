import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import logo from '../../../assets/logo.png';
import './VerifyEmailPage.css';

const VerifyEmailPage = () => {
  const [codigo, setCodigo] = useState('');
  const [mensaje, setMensaje] = useState<{ texto: string; tipo: 'ok' | 'err' } | null>(null);
  const [loading, setLoading] = useState(false);

  const navigate = useNavigate();
  const apiBase = (import.meta.env.VITE_API_URL ?? '').trim().replace(/\/$/, '');

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setMensaje(null);

    const codigoLimpio = codigo.trim();
    if (!codigoLimpio) {
      setMensaje({ texto: 'Introduce un código de verificación.', tipo: 'err' });
      return;
    }

    setLoading(true);
    try {
      const response = await fetch(`${apiBase}/auth/confirm-email/${encodeURIComponent(codigoLimpio)}`, {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' }
      });

      const data = await response.json();
      if (response.ok) {
        setMensaje({ texto: 'Cuenta activada correctamente. Ya puedes iniciar sesión.', tipo: 'ok' });
        setCodigo('');
      } else {
        setMensaje({ texto: data.message || 'No se pudo verificar el código.', tipo: 'err' });
      }
    } catch {
      setMensaje({ texto: 'Error de conexión. Inténtalo de nuevo.', tipo: 'err' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="verify-page-container">
      <button
        type="button"
        className="verify-corner-logo-btn"
        onClick={() => navigate('/')}
        aria-label="Volver al inicio"
      >
        <img src={logo} alt="Cerebrus" className="verify-corner-logo" />
      </button>

      <div className="verify-box">
        <h2 className="verify-title">Verificar Cuenta</h2>
        <p className="verify-subtitle">Introduce el código que recibiste por correo electrónico.</p>

        <form onSubmit={handleSubmit} className="verify-form">
          <label htmlFor="codigoVerificacion" className="verify-label">Código de verificación</label>
          <input
            id="codigoVerificacion"
            type="text"
            inputMode="numeric"
            value={codigo}
            onChange={(e) => setCodigo(e.target.value)}
            placeholder="Ej: 12345678"
            className="verify-input"
            required
          />

          {mensaje && <div className={`verify-message ${mensaje.tipo}`}>{mensaje.texto}</div>}

          <button type="submit" className="verify-submit-btn" disabled={loading}>
            {loading ? 'Verificando...' : 'Verificar código'}
          </button>
        </form>

        <button type="button" className="verify-back-btn" onClick={() => navigate('/auth/login')}>
          Volver a login
        </button>
      </div>
    </div>
  );
};

export default VerifyEmailPage;
