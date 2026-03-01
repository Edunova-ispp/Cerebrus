import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import './RegisterPage.css'; 
import mascotImg from "../../../assets/logo.png"; 

const RegisterPage = () => {
  const [nombre, setNombre] = useState('');
  const [primerApellido, setPrimerApellido] = useState('');
  const [segundoApellido, setSegundoApellido] = useState('');
  const [email, setEmail] = useState('');
  const [username, setUsername] = useState('');
  const [organizacion, setOrganizacion] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [tipoUsuario, setTipoUsuario] = useState('ALUMNO');
  const [puntos, setPuntos] = useState('');

  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    if (password !== confirmPassword) {
      setError('Las contraseñas no coinciden.');
      return; 
    }

    const payload: any = { 
      nombre, 
      primerApellido, 
      segundoApellido,
      email, 
      username,
      password,
      organizacion,
      tipoUsuario 
    };

    if (tipoUsuario === "ALUMNO") {
      const valorPuntos = puntos === '' ? 0 : parseInt(puntos, 10);
      payload.puntos = valorPuntos;
    }

    try {
      const response = await fetch('http://localhost:8080/auth/register', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload),
      });

      let data;
      const contentType = response.headers.get("content-type");
      if (contentType && contentType.includes("application/json")) {
        data = await response.json();
      } else {
        data = { message: await response.text() };
      }

      if (response.ok) {
        setSuccess('¡Registro exitoso!');
        setTimeout(() => {
          if (tipoUsuario === 'ALUMNO') navigate('/miscursos');
          else if (tipoUsuario === 'MAESTRO') navigate('/miscursos');
          else if (tipoUsuario === 'DIRECTOR') navigate('/infoDueños');
          else navigate('/');
        }, 2000);
      } else {
        setError(data.message || 'Error en el servidor');
      }
    } catch {
      setError('Error de conexión con el servidor.');
    }
  };

  return (
    <div className="register-page-wrapper">
      <div className="register-card">
        <div className="register-header">
          <img src={mascotImg} alt="Cerebrus Mascot" className="register-mascot" />
          <h2 className="register-text-logo">Registro</h2>
        </div>

        <form onSubmit={handleSubmit} className="register-form-layout">

          <div className="register-fields-row">
            <div className="register-field-group">
              <label className="register-label">Tipo de cuenta:</label>
              <select
                value={tipoUsuario}
                onChange={(e) => setTipoUsuario(e.target.value)}
                className="register-input"
              >
                <option value="ALUMNO">Alumno</option>
                <option value="MAESTRO">Maestro</option>
                <option value="DIRECTOR">Director</option>
              </select>
            </div>
            {tipoUsuario === "ALUMNO" && (
              <div className="register-field-group">
                <label className="register-label">Puntos Iniciales:</label>
                <input
                  type="number"
                  placeholder="0"
                  value={puntos}
                  onChange={(e) => setPuntos(e.target.value)}
                  className="register-input"
                />
              </div>
            )}
          </div>

          <div className="register-fields-row">
            <div className="register-field-group">
              <label className="register-label">Nombre:</label>
              <input type="text" value={nombre} onChange={(e) => setNombre(e.target.value)} required className="register-input" />
            </div>
            <div className="register-field-group">
              <label className="register-label">Primer Apellido:</label>
              <input type="text" value={primerApellido} onChange={(e) => setPrimerApellido(e.target.value)} required className="register-input" />
            </div>
          </div>

          <div className="register-fields-row">
            <div className="register-field-group">
              <label className="register-label">Segundo Apellido:</label>
              <input type="text" value={segundoApellido} onChange={(e) => setSegundoApellido(e.target.value)} required className="register-input" />
            </div>
            <div className="register-field-group">
              <label className="register-label">Correo electrónico:</label>
              <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required className="register-input" />
            </div>
          </div>

          <div className="register-fields-row">
            <div className="register-field-group">
              <label className="register-label">Usuario:</label>
              <input type="text" value={username} onChange={(e) => setUsername(e.target.value)} required className="register-input" />
            </div>
            <div className="register-field-group">
              <label className="register-label">Organización:</label>
              <input type="text" value={organizacion} onChange={(e) => setOrganizacion(e.target.value)} required className="register-input" />
            </div>
          </div>

          <div className="register-fields-row">
            <div className="register-field-group">
              <label className="register-label">Contraseña:</label>
              <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required className="register-input" />
            </div>
            <div className="register-field-group">
              <label className="register-label">Confirmar contraseña:</label>
              <input type="password" value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)} required className="register-input" />
            </div>
          </div>

          {error && <p className="register-feedback register-feedback-error">{error}</p>}
          {success && <p className="register-feedback register-feedback-success">{success}</p>}

          <div className="register-submit-container">
            <button type="submit" className="register-submit-btn">Registrarse</button>
          </div>
        </form>
      </div>

      <p className="register-footer-text">
        ¿Ya tienes cuenta? <Link to="/auth/login" className="register-link">Iniciar sesión</Link>
      </p>
    </div>
  );
};

export default RegisterPage;