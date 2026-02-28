import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import "./LoginPage.css"; 
import logo from "../../../assets/logo.png"; 

export type UserType = "alumno" | "profesor" | "dueno";

const Login = () => {
  const [identificador, setIdentificador] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    try {
      const response = await fetch('http://localhost:8080/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ identificador, password }),
      });

      if (response.ok) {
        const data = await response.json();
        
        localStorage.setItem('token', data.jwt);
        localStorage.setItem('username', data.username);
        localStorage.setItem('role', data.role);

        const rolUsuario = String(data.role).toUpperCase();

        if (rolUsuario.includes("ALUMNO")) {
          navigate('/miscursos');
        } 
        else if (rolUsuario.includes("PROFESOR") || rolUsuario.includes("MAESTRO")) {
          navigate('/crearcursos');
        } 
        else if (rolUsuario.includes("DUEÑO") || rolUsuario.includes("DUENO") || rolUsuario.includes("DIRECTOR")) {
          navigate('/infoDueños');
        } 
        else {
          navigate('/');
        }

      } else {
        const errorData = await response.json().catch(() => ({}));
        setError(errorData.message || 'Credenciales incorrectas. Inténtalo de nuevo.');
      }
    } catch {
      setError('No se pudo conectar con el servidor. Verifica que el backend esté activo.');
    }
  };

  return (
    <div className="login-page-container">
      <div className="login-box">
        
        {/* LOGO */}
        <div className="login-header">
          <img src={logo} alt="Cerebrus Mascot" className="login-logo" />
        </div>
        
        <form onSubmit={handleSubmit} className="login-form">
          
          {/* INPUT USUARIO */}
          <div className="pixel-input-wrapper">
            <label htmlFor="identificador">Usuario:</label>
            <input 
              id="identificador"
              type="text" 
              value={identificador} 
              onChange={(e) => setIdentificador(e.target.value)} 
              required 
            />
          </div>

          {/* INPUT CONTRASEÑA */}
          <div className="pixel-input-wrapper">
            <label htmlFor="password">Contraseña:</label>
            <input 
              id="password"
              type="password" 
              value={password} 
              onChange={(e) => setPassword(e.target.value)} 
              required 
            />
          </div>

          {/* MENSAJE DE ERROR */}
          {error && (
            <div className="login-error-msg">
              {error}
            </div>
          )}

          {/* BOTÓN Y REGISTRO */}
          <div className="login-actions">
            <button type="submit" className="pixel-btn-submit">
              Iniciar sesión
            </button>
            <p className="login-register-text">
              ¿No tienes cuenta? <span onClick={() => navigate('/auth/register')}>Regístrate</span>
            </p>
          </div>

        </form>
      </div>
    </div>
  );
};

export default Login;