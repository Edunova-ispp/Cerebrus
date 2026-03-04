
import { useNavigate } from 'react-router-dom';
import "./LogoutPage.css"; 

const Logout = () => {
  const navigate = useNavigate();

  const sendLogoutRequest = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('role');
    navigate('/auth/login');
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