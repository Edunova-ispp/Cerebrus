import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import logo from "../../../assets/logo.png";
import "./LoginPage.css";

const Login = () => {
  const [identificador, setIdentificador] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  
  // Estados para la activación de cuenta
  const [searchParams] = useSearchParams();
  const [manualCode, setManualCode] = useState(''); // Para escribir el código a mano
  const [mensajeActivacion, setMensajeActivacion] = useState<{ texto: string, tipo: 'ok' | 'err' } | null>(null);
  const [loadingCode, setLoadingCode] = useState(false);
  
  const navigate = useNavigate();
  const apiBase = (import.meta.env.VITE_API_URL ?? "").trim().replace(/\/$/, "");

  // 1. LÓGICA AUTOMÁTICA: Detecta el código si viene en la URL (?confirmCode=12345678)
  useEffect(() => {
    const codeFromUrl = searchParams.get('confirmCode');
    if (codeFromUrl) {
      activarCuenta(codeFromUrl);
    }
  }, [searchParams]);

  // Función genérica para activar (sirve para el link y para el botón manual)
  const activarCuenta = async (codigo: string) => {
    setLoadingCode(true);
    try {
      const response = await fetch(`${apiBase}/auth/confirm-email/${codigo}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' }
      });

      const data = await response.json();

      if (response.ok) {
        setMensajeActivacion({ texto: "✅ ¡Cuenta activada! Ya puedes iniciar sesión.", tipo: 'ok' });
        setManualCode(''); // Limpiamos el input si lo usó
      } else {
        setMensajeActivacion({ texto: `❌ ${data.message || 'Código inválido'}`, tipo: 'err' });
      }
    } catch (err) {
      setMensajeActivacion({ texto: "❌ Error de conexión al activar.", tipo: 'err' });
    } finally {
      setLoadingCode(false);
    }
  };

  const handleLogin = async (e: React.SyntheticEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError('');

    try {
      const response = await fetch(`${apiBase}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ identificador, password }),
      });

      const data = await response.json();

      if (response.ok) {
        localStorage.setItem('token', data.token);
        localStorage.setItem('username', data.username);
        localStorage.setItem('role', data.roles);

        const rolUsuario = String(data.roles).toUpperCase();
        if (rolUsuario.includes("ALUMNO") || rolUsuario.includes("MAESTRO")) {
          navigate('/miscursos');
        } else if (rolUsuario.includes("ORGANIZACION") || rolUsuario.includes("DUENO")) {
          navigate('/infoDueños');
        } else {
          navigate('/');
        }
      } else {
        setError(data.message || 'Credenciales incorrectas o cuenta no activada.');
      }
    } catch {
      setError('No se pudo conectar con el servidor.');
    }
  };

  return (
    <div className="login-page-container">
      <div className="login-box">
        
        {/* SECCIÓN DE ACTIVACIÓN (BANNER) */}
        {mensajeActivacion && (
          <div className={`login-activation-banner ${mensajeActivacion.tipo}`}>
            {mensajeActivacion.texto}
          </div>
        )}

        <div className="login-header">
          <img src={logo} alt="Cerebrus Mascot" className="login-logo" />
          <h2 className="login-title">Iniciar Sesión</h2>
        </div>
        
        {/* FORMULARIO DE LOGIN NORMAL */}
        <form onSubmit={handleLogin} className="login-form">
          <div className="pixel-input-wrapper">
            <label htmlFor="identificador">Correo electrónico:</label>
            <input 
              id="identificador"
              type="text" 
              value={identificador} 
              onChange={(e) => setIdentificador(e.target.value)} 
              required 
            />
          </div>

          <div className="pixel-input-wrapper">
            <label htmlFor="password">Contraseña:</label>
            <div className="password-input-container">
              <input 
                id="password"
                type={showPassword ? 'text' : 'password'}
                value={password} 
                onChange={(e) => setPassword(e.target.value)} 
                required 
              />
              <button type="button" className="login-pw-toggle" onClick={() => setShowPassword(!showPassword)}>
                {showPassword ? '🙈' : '👁'}
              </button>
            </div>
          </div>

          {error && <div className="login-error-msg">{error}</div>}

          <button type="submit" className="pixel-btn-submit">Entrar</button>
        </form>

        <div className="pixel-divider">
          <span className="activation-subtitle">O ACTIVA TU CUENTA</span>
        </div>

        <div className="manual-verify-section">
          <div className="pixel-input-wrapper">
            <label>Código:</label>
            <input 
              type="text" 
              placeholder="Ej: 12345678" 
              value={manualCode}
              onChange={(e) => setManualCode(e.target.value)}
            />
          </div>

          <button 
            type="button"
            onClick={() => activarCuenta(manualCode)} 
            disabled={loadingCode || manualCode.length < 4}
            className="pixel-btn-submit"
          >
            {loadingCode ? "..." : "ACTIVAR"}
          </button>
        </div>

        <p className="login-register-text">
          ¿No tienes cuenta? <span onClick={() => navigate('/auth/register')}>Regístrate</span>
        </p>
      </div>
    </div>
  );
};

export default Login;