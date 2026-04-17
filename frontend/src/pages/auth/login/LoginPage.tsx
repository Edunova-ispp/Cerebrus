import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import logo from "../../../assets/logo.png";
import "./LoginPage.css";

const Login = () => {
  const [identificador, setIdentificador] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  
  const navigate = useNavigate();
  const apiBase = (import.meta.env.VITE_API_URL ?? "").trim().replace(/\/$/, "");

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
        else {
          setError(data.message || 'Credenciales incorrectas.');
        }
      }
    } catch {
      setError('No se pudo conectar con el servidor.');
    }
  };

  return (
    <div className="login-page-container">
      <button
        type="button"
        className="login-corner-logo-btn"
        onClick={() => navigate('/')}
        aria-label="Volver al inicio"
      >
        <img src={logo} alt="Cerebrus" className="login-corner-logo" />
      </button>

      <div className="login-box">
        <div className="login-header">
          <h2 className="login-title">Iniciar Sesión</h2>
        </div>
        
        <form onSubmit={handleLogin} className="login-form">
          <div className="login-field-group">
            <label className="login-field-label" htmlFor="identificador">Correo electrónico o Usuario:</label>
            <div className="pixel-input-wrapper">
              <input 
                id="identificador"
                type="text" 
                value={identificador} 
                onChange={(e) => setIdentificador(e.target.value)} 
                required 
                autoComplete="username"
              />
            </div>
          </div>

          <div className="login-field-group">
            <label className="login-field-label" htmlFor="password">Contraseña:</label>
            <div className="pixel-input-wrapper">
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
          </div>

          {error && <div className="login-error-msg">{error}</div>}

          <button type="submit" className="pixel-btn-submit">ENTRAR</button>
        </form>

        <button
          type="button"
          className="login-verify-link"
          onClick={() => navigate('/auth/verify-email')}
        >
          Tengo un código de verificación
        </button>

        <p className="login-register-text">
          ¿No tienes cuenta? <span onClick={() => navigate('/auth/register')}>Regístrate aquí</span>
        </p>
      </div>
    </div>
  );
};

export default Login;