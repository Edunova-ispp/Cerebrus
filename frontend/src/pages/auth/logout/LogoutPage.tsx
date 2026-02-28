
import { useNavigate } from 'react-router-dom';
import "./LogoutPage.css"; // <-- Actualizado para cargar tu nuevo diseño pixel art

const Logout = () => {
  const navigate = useNavigate();

  const sendLogoutRequest = () => {
    // 1. Borrar el token de sesión del navegador
    localStorage.removeItem('token'); 
    
    // Nota: Si en tu Login guardaste el token con otro nombre (ej. 'jwt'), 
    // cambia 'token' por ese nombre en la línea de arriba.

    // 2. Redirigir al usuario a la pantalla de login
    navigate('/login'); // <-- Corregido para coincidir con tus rutas
  };

  return (
    <div className="logout-page-container">
      <div className="logout-form-container">
        <div className="hero-div-logout">
          <h2 className="logout-h2">¿Quieres cerrar sesión?</h2>
          
          <div className="button-container-logout">
            {/* Si dice que NO, lo mandamos de vuelta (ej. al home) */}
            <button 
              className="button-style-logout cancel-button-logout" 
              onClick={() => navigate('/')}
            >
              No
            </button>
            
            {/* Si dice que SÍ, ejecutamos la función de cierre de sesión */}
            <button 
              className="button-style-logout logout-button" 
              onClick={sendLogoutRequest}
            >
              Sí
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Logout;