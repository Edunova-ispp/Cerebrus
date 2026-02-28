import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import './RegisterPage.css'; 
// Asumimos que el logo es la imagen de la mascota pixel art de la derecha
import mascotImg from "../../../assets/logo.png"; 

const RegisterPage = () => {
  // --- ESTADOS (Lógica intacta) ---
  const [nombre, setNombre] = useState('');
  const [primerApellido, setPrimerApellido] = useState('');
  const [segundoApellido, setSegundoApellido] = useState('');
  const [email, setEmail] = useState('');
  const [organizacion, setOrganizacion] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  
  const navigate = useNavigate();

  // --- MANEJADOR (Lógica intacta con validación de coincidencia) ---
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    // Validación de contraseñas
    if (password !== confirmPassword) {
      setError('Las contraseñas no coinciden. Por favor, inténtalo de nuevo.');
      return; 
    }

    try {
      // Petición al backend
      const response = await fetch('http://localhost:8080/auth/register', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ 
          nombre: nombre, 
          primerApellido: primerApellido, 
          segundoApellido: segundoApellido,
          email: email, 
          username: email, // Usamos email como username por defecto
          password: password,
          organizacion: organizacion,
          tipoUsuario: "DIRECTOR" // Rol fijo según historia de usuario
        }),
      });

      let data;
      try {
        data = await response.json();
      } catch  {
        data = { message: "Error procesando la respuesta del servidor" };
      }

      if (response.ok) {
        setSuccess('¡Cuenta de organización creada con éxito! Entrando...');
        
        // Redirección tras éxito
        setTimeout(() => {
          navigate('/infoDueños'); 
        }, 2000);
      } else {
        // Error controlado desde el backend
        setError(data.message || 'Error al registrar la cuenta.');
      }
    } catch  {
      // Error de conexión
      setError('Error de conexión con el servidor. ¿Está encendido el backend?');
    }
  };

  // --- RENDERIZADO (Estructura visual actualizada para coincidir con la imagen) ---
  return (
    <div className="register-page-wrapper">
      {/* Texto pequeño arriba a la izquierda */}
      <div className="register-top-label">Registro</div>
      
      {/* Cabecera: Texto Cerebrus + Mascota */}
      <div className="register-header">
          <h1 className="register-text-logo">Cerebrus</h1>
          <img src={mascotImg} alt="Cerebrus Mascot" className="register-mascot" />
      </div>

      {/* Tarjeta blanca con bordes negros */}
      <div className="register-card">
        <form onSubmit={handleSubmit} className="register-form-layout">
          
          {/* Campo: Nombre */}
          <div className="register-field-group">
            <label htmlFor="nombre" className="register-label">Nombre:</label>
            <input id="nombre" type="text" value={nombre} onChange={(e) => setNombre(e.target.value)} required className="register-input" />
          </div>

          {/* Campo: Primer Apellido */}
          <div className="register-field-group">
            <label htmlFor="primerApellido" className="register-label">Primer Apellido:</label>
            <input id="primerApellido" type="text" value={primerApellido} onChange={(e) => setPrimerApellido(e.target.value)} required className="register-input" />
          </div>

          {/* Campo: Segundo Apellido */}
          <div className="register-field-group">
            <label htmlFor="segundoApellido" className="register-label">Segundo Apellido:</label>
            <input id="segundoApellido" type="text" value={segundoApellido} onChange={(e) => setSegundoApellido(e.target.value)} required className="register-input" />
          </div>

          {/* Campo: Correo electrónico */}
          <div className="register-field-group">
            <label htmlFor="email" className="register-label">Correo electrónico:</label>
            <input id="email" type="email" value={email} onChange={(e) => setEmail(e.target.value)} required className="register-input" />
          </div>

          {/* Campo: Organización */}
          <div className="register-field-group">
            <label htmlFor="organizacion" className="register-label">Organización:</label>
            <input id="organizacion" type="text" value={organizacion} onChange={(e) => setOrganizacion(e.target.value)} required className="register-input" />
          </div>

          {/* Campo: Contraseña */}
          <div className="register-field-group">
            <label htmlFor="password" className="register-label">Contraseña:</label>
            <input id="password" type="password" value={password} onChange={(e) => setPassword(e.target.value)} required className="register-input" />
          </div>

          {/* Campo: Confirmar contraseña */}
          <div className="register-field-group">
            <label htmlFor="confirmPassword" className="register-label">Confirmar contraseña:</label>
            <input id="confirmPassword" type="password" value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)} required className="register-input" />
          </div>

          {/* Mensajes de feedback (Error/Éxito) */}
          {error && <p className="register-feedback register-feedback-error">{error}</p>}
          {success && <p className="register-feedback register-feedback-success">{success}</p>}
          
          {/* Contenedor para centrar el botón */}
          <div className="register-submit-container">
            <button type="submit" className="register-submit-btn">
              Registrate
            </button>
          </div>
        </form>
      </div>

      {/* Footer */}
      <p className="register-footer-text">
        ¿Ya tienes cuenta? <Link to="/auth/login" className="register-link">Iniciar sesión</Link>
      </p>
      
    </div>
  );
};

export default RegisterPage;