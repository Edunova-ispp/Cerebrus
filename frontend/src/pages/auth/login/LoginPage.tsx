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
  const [manualCode, setManualCode] = useState(''); 
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

  // Función para activar cuenta (usada por link o por botón manual)
  const activarCuenta = async (codigo: string) => {
    setLoadingCode(true);
    setMensajeActivacion(null);
    try {
      const response = await fetch(`${apiBase}/auth/confirm-email/${codigo}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' }
      });

      const data = await response.json();

      if (response.ok) {
        setMensajeActivacion({ texto: "✅ ¡Cuenta activada! Ya puedes iniciar sesión.", tipo: 'ok' });
        setManualCode(''); 
        setError(''); // Limpiamos errores de login previos
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
          navigate('/suscripcion');
        } else {
          navigate('/');
        }
      } else {
        if (response.status === 403 && data.message === "CUENTA_NO_VERIFICADA") {
          setError('⚠️ Error de verificación. Comprueba que tu cuenta esté activada o pertenezca a una organización con suscripción activa.');
          
          document.querySelector('.pixel-divider')?.scrollIntoView({ behavior: 'smooth' });
        } else if (response.status === 403 && data.message === "CUENTA_ORG_NO_SUSCRIPCION") {
          setError('⚠️ La cuenta pertenece a una organización sin suscripción activa.');
          
          document.querySelector('.pixel-divider')?.scrollIntoView({ behavior: 'smooth' });
        }
        else if (response.status === 403 && data.message === "ORG_NO_SUSCRIPCION") {
          setError('⚠️ La organización no tiene una suscripción pagada.');

          document.querySelector('.pixel-divider')?.scrollIntoView({ behavior: 'smooth' });
        } else if (response.status === 403 && data.message === "ORG_SUSCRIPCION_EXPIRADA") {
          setError('⚠️ La organización tiene una suscripción expirada.');

          document.querySelector('.pixel-divider')?.scrollIntoView({ behavior: 'smooth' });
        }
        else {
          setError(data.message || 'Credenciales incorrectas.');
        }
      }
    } catch (err) {
      setError('No se pudo conectar con el servidor.');
    }
  };

  return (
    <div className="login-page-container">
      <div className="login-box">
        
        {mensajeActivacion && (
          <div className={`login-activation-banner ${mensajeActivacion.tipo}`}>
            {mensajeActivacion.texto}
          </div>
        )}

        <div className="login-header">
          <img src={logo} alt="Cerebrus Logo" className="login-logo" />
          <h2 className="login-title">Iniciar Sesión</h2>
        </div>
        
        <form onSubmit={handleLogin} className="login-form">
          <div className="pixel-input-wrapper">
            <label htmlFor="identificador">Correo electrónico o Usuario:</label>
            <input 
              id="identificador"
              type="text" 
              value={identificador} 
              onChange={(e) => setIdentificador(e.target.value)} 
              required 
              autoComplete="username"
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
                autoComplete="current-password"
              />
              <button 
                type="button" 
                className="login-pw-toggle" 
                onClick={() => setShowPassword(!showPassword)}
                tabIndex={-1}
              >
                {showPassword ? '🙈' : '👁️'}
              </button>
              
            </div>
          </div>

          {error && <div className="login-error-msg">{error}</div>}

          <button type="submit" className="pixel-btn-submit">ENTRAR</button>
        </form>

        <div className="pixel-divider">
          <span className="activation-subtitle">O ACTIVA TU CUENTA</span>
        </div>

        {/* SECCIÓN MANUAL DE VERIFICACIÓN */}
        <div className="manual-verify-section">
          <p className="activation-help-text">¿Recibiste un código? Introdúcelo aquí:</p>
          <div className="pixel-input-wrapper">
            <input 
              type="text" 
              placeholder="Código de 8 dígitos" 
              value={manualCode}
              onChange={(e) => setManualCode(e.target.value.replace(/\D/g, ''))} // Solo números
              maxLength={8}
            />
          </div>

          <button type="submit" 
            onClick={() => activarCuenta(manualCode)} 
            disabled={loadingCode || manualCode.length < 4}
            className="pixel-btn-submit"
          >
            {loadingCode ? "Procesando..." : "ACTIVAR CUENTA"}
          </button>
        </div>

        <p className="login-register-text">
          ¿No tienes cuenta? <span onClick={() => navigate('/auth/register')}>Regístrate aquí</span>
        </p>
      </div>
    </div>
  );
};

export default Login;