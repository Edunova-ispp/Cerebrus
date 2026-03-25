import React from 'react';
import { Link } from 'react-router-dom';
import './TermsPage.css';
// Importamos el logo usando la misma ruta relativa (ajusta los '../' si tu carpeta legal está en otro nivel)
import logo from '../../assets/logo.png';

const TermsPage: React.FC = () => {
  return (
    <div className="terms-page-container">
      <div className="terms-header-actions" style={{ marginBottom: '1rem' }}>
        <Link to="/" style={{ textDecoration: 'none', color: '#3498db', fontWeight: 'bold' }}>
          &larr; Volver al Inicio
        </Link>
      </div>
      
      {/* Contenedor del logo centrado */}
      <div style={{ textAlign: 'center', marginBottom: '1.5rem' }}>
        <img 
          src={logo} 
          alt="Cerebrus Logo" 
          style={{ width: '250px', height: 'auto' }} 
        />
      </div>

      <h1>Política de Privacidad y Términos de Servicio</h1>
      
      <div className="terms-section">
        <h2>1. Información que recopilamos y finalidad</h2>
        <p>
          Para poder ofrecerle los servicios educativos de Cerebrus, Edunova recopilamos y tratamos los siguientes datos durante el proceso de registro:
        </p>
        <ul>
          <li><strong>Nombre y Apellidos:</strong> Son necesarios para identificarle de forma unívoca dentro de la plataforma, personalizar su experiencia de aprendizaje y facilitar la comunicación y evaluación entre maestros y alumnos.</li>
          <li><strong>Correo Electrónico:</strong> Se utiliza como credencial de acceso único, para la recuperación de contraseñas y para enviarle notificaciones esenciales relacionadas con el funcionamiento del servicio.</li>
          <li><strong>Centro Educativo u Organización:</strong> Es requerido para vincular a los usuarios con su entorno académico real. Esto permite a los maestros organizar a sus alumnos en clases, asignar actividades específicas y realizar un seguimiento del progreso dentro del marco de su institución.</li>
          <li><strong>Contraseña:</strong> Almacenada de forma estrictamente cifrada para garantizar la seguridad y privacidad de su cuenta.</li>
        </ul>
      </div>

      <div className="terms-section">
        <h2>2. Base Legal y Conservación de los Datos</h2>
        <p>
          El tratamiento de sus datos se basa en el <strong>consentimiento explícito</strong> que usted otorga al marcar la casilla de aceptación durante el registro. 
          Conservaremos sus datos personales únicamente mientras su cuenta permanezca activa para prestarle el servicio, o hasta que usted solicite expresamente su eliminación.
        </p>
      </div>

      <div className="terms-section">
        <h2>3. Privacidad y Terceros</h2>
        <p>
          En Cerebrus nos tomamos muy en serio su privacidad. Sus datos personales son de uso exclusivo para el funcionamiento de la plataforma educativa. <strong>No vendemos, alquilamos ni cedemos</strong> su información personal a terceros con fines comerciales o publicitarios bajo ninguna circunstancia.
        </p>
      </div>

      <div className="terms-section">
        <h2>4. Condiciones de Uso de la Aplicación</h2>
        <p>
          El acceso y uso de esta plataforma implica la aceptación de estas condiciones. El usuario se compromete a hacer un uso adecuado y respetuoso de la plataforma con fines estrictamente educativos. Cerebrus se reserva el derecho de modificar, suspender o cancelar las cuentas de aquellos usuarios que incumplan estas normas o realicen un uso indebido del servicio.
        </p>
      </div>

      <div className="terms-section">
        <h2>5. Derechos del Usuario (Derechos Acceso, Rectificación, Cancelación y Oposición)</h2>
        <p>
          De acuerdo con el RGPD, usted tiene derecho a acceder a sus datos personales, solicitar la rectificación de los datos inexactos, solicitar su supresión (derecho al olvido), limitar u oponerse a su tratamiento y solicitar la portabilidad de los mismos. Puede retirar su consentimiento en cualquier momento sin que ello afecte a la licitud del tratamiento previo.
        </p>
      </div>

      <div className="terms-contact">
        <h2>6. Contacto y Cancelación de Cuenta</h2>
        <p>
          Si desea ejercer cualquiera de sus derechos, tiene alguna duda sobre esta política, o desea solicitar la <strong>eliminación total de sus datos y la cancelación definitiva de su cuenta</strong>, por favor póngase en contacto con nosotros escribiendo al siguiente correo electrónico:
        </p>
        <p><a href="mailto:cerebrus.edunova@gmail.com"><strong>cerebrus.edunova@gmail.com</strong></a></p>
      </div>

      <div className="terms-header-actions" style={{ marginBottom: '1rem', marginTop: '2rem', textAlign: 'center' }}>
        <Link to="/" style={{ textDecoration: 'none', color: '#3498db', fontWeight: 'bold' }}>
          &larr; Volver al Inicio
        </Link>
      </div>
    </div>
  );
};

export default TermsPage;