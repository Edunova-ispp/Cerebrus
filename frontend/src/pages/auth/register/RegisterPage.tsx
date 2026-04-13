import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import './RegisterPage.css'; 
import mascotImg from "../../../assets/logo.png"; 

const RegisterPage = () => {
  // 1. ESTADOS
  const [nombre, setNombre] = useState('');
  const [primerApellido, setPrimerApellido] = useState('');
  const [segundoApellido, setSegundoApellido] = useState('');
  const [email, setEmail] = useState('');
  const [nombreCentro, setNombreCentro] = useState(''); 
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  
  const [showPassword, setShowPassword] = useState(false);
  const [acceptedTerms, setAcceptedTerms] = useState(false);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [isSuccess, setIsSuccess] = useState(false); 
  
  const navigate = useNavigate();

  // 2. LÓGICA DE ENVÍO
  const handleSubmit = async (e: React.SyntheticEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError('');

    // VALIDACIÓN: Todos los campos son obligatorios (incluido segundo apellido)
    if (!nombre || !primerApellido || !segundoApellido || !email || !nombreCentro || !password) {
      setError('Por favor, rellena todos los campos obligatorios.');
      return; 
    }

    // Validación: Contraseñas coincidentes
    if (password !== confirmPassword) {
      setError('Las contraseñas no coinciden.');
      return; 
    }

    setLoading(true);

    // REQUISITOS: username copiado del email y rol de ORGANIZACIÓN
    const payload = { 
      nombre, 
      primerApellido, 
      segundoApellido,
      email, 
      username: email, // El email actúa como nombre de usuario
      password,
      nombreCentro, 
      tipoUsuario: "ORGANIZACION" 
    };

    try {
      const apiBase = (import.meta.env.VITE_API_URL ?? "").trim().replace(/\/$/, "");
      const response = await fetch(`${apiBase}/auth/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });

      const data = await response.json();

      if (response.ok) {
        // Registro exitoso -> Mostrar pantalla de confirmación de Email
        setIsSuccess(true); 
      } else {
        // TRADUCCIÓN DE ERRORES: Si el backend se queja del 'username', el usuario debe ver 'email'
        
        let mensajeError = data.message || 'Error en el servidor';

        if (mensajeError.toLowerCase().includes("nombre de usuario") || 
            mensajeError.toLowerCase().includes("username")) {
          mensajeError = "Este correo electrónico ya está registrado.";
        }

        setError(mensajeError);
      }
    } catch (err) {
      setError('Error de conexión con el servidor. Inténtalo más tarde.');
    } finally {
      setLoading(false);
    }
  };

  // 3. VISTA DE ÉXITO (TRAS REGISTRO CORRECTO)
  if (isSuccess) {
    return (
      <div className="register-page-wrapper">
        <div className="register-card success-modal">
          <div className="register-header">
            <img src={mascotImg} alt="Cerebrus" className="register-mascot" />
            <h2 className="pixel-title">¡SOLICITUD ENVIADA! ✉️</h2>
          </div>
          <div className="success-content">
            <p className="success-message">
              Gracias por registrar tu organización, <strong>{nombre}</strong>.
            </p>
            <p className="success-instruction">
              Hemos enviado un enlace de activación a <strong>{email}</strong>. 
              <br /><br />
              <strong>Debes confirmar tu cuenta</strong> desde el correo electrónico para poder iniciar sesión. Si no lo ves, revisa tu carpeta de spam.
            </p>
          </div>
          <button className="register-submit-btn" onClick={() => navigate('/auth/login')}>
            Volver al Inicio de Sesión
          </button>
        </div>
      </div>
    );
  }

  // 4. VISTA DEL FORMULARIO
  return (
    <div className="register-page-wrapper">
      <div className="register-card">
        <div className="register-header">
          <img src={mascotImg} alt="Cerebrus Mascot" className="register-mascot" />
          <h2 className="register-text-logo">Registro de Organización</h2>
        </div>

        <form onSubmit={handleSubmit} className="register-form-layout">
          
          {/* Fila: Nombre y Primer Apellido */}
          <div className="register-fields-row">
            <div className="register-field-group">
              <label className="register-label">Nombre del responsable: *</label>
              <input 
                type="text" 
                value={nombre} 
                onChange={(e) => setNombre(e.target.value)} 
                required 
                className="register-input" 
                placeholder="Ej: Ana"
              />
            </div>
            <div className="register-field-group">
              <label className="register-label">Primer apellido: *</label>
              <input 
                type="text" 
                value={primerApellido} 
                onChange={(e) => setPrimerApellido(e.target.value)} 
                required 
                className="register-input" 
                placeholder="Ej: Pérez"
              />
            </div>
          </div>

          {/* Fila: Segundo Apellido y Email */}
          <div className="register-fields-row">
            <div className="register-field-group">
              <label className="register-label">Segundo apellido: *</label>
              <input 
                type="text" 
                value={segundoApellido} 
                onChange={(e) => setSegundoApellido(e.target.value)} 
                required 
                className="register-input" 
                placeholder="Ej: Gómez"
              />
            </div>
            <div className="register-field-group">
              <label className="register-label">Correo electrónico: *</label>
              <input 
                type="email" 
                value={email} 
                onChange={(e) => setEmail(e.target.value)} 
                required 
                className="register-input" 
                placeholder="contacto@organizacion.com" 
              />
            </div>
          </div>

          {/* Nombre de la Organización */}
          <div className="register-field-group full-width">
            <label className="register-label">Nombre de la Organización / Centro: *</label>
            <input 
              type="text" 
              value={nombreCentro} 
              onChange={(e) => setNombreCentro(e.target.value)} 
              required 
              className="register-input" 
              placeholder="Ej: Centro Educativo Cerebrus" 
            />
          </div>

          {/* Fila: Password y Confirmación */}
          <div className="register-fields-row">
            <div className="register-field-group">
              <label className="register-label">Contraseña: *</label>
              <div className="pw-input-wrap">
                <input 
                  type={showPassword ? 'text' : 'password'} 
                  value={password} 
                  onChange={(e) => setPassword(e.target.value)} 
                  required 
                  className="register-input" 
                />
                <button 
                  type="button" 
                  className="pw-toggle-btn" 
                  onClick={() => setShowPassword(!showPassword)}
                >
                  {showPassword ? '🙈' : '👁'}
                </button>
              </div>
            </div>
            <div className="register-field-group">
              <label className="register-label">Confirmar contraseña: *</label>
              <input 
                type="password" 
                value={confirmPassword} 
                onChange={(e) => setConfirmPassword(e.target.value)} 
                required 
                className="register-input" 
              />
            </div>
          </div>

          {/* Feedback de Error */}
          {error && <p className="register-feedback register-feedback-error">{error}</p>}

          {/* Términos y Condiciones */}
          <div className="register-terms-container">
            <input
              type="checkbox"
              id="terms"
              checked={acceptedTerms}
              onChange={(e) => setAcceptedTerms(e.target.checked)}
              required
            />
            <label htmlFor="terms">
              Acepto los <Link to="/terminos">Términos y Condiciones</Link> y la política de privacidad.
            </label>
          </div>

          {/* Botón de envío */}
          <div className="register-submit-container">
            <button
              type="submit"
              className="register-submit-btn"
              disabled={!acceptedTerms || loading}
            >
              {loading ? "Procesando..." : "Registrar Organización"}
            </button>
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