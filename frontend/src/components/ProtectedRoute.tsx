import { Navigate } from 'react-router-dom';

interface ProtectedRouteProps {
  children: React.ReactElement;
  allowedRoles: string[]; // Ej: ['ALUMNO', 'PROFESOR']
}

const ProtectedRoute = ({ children, allowedRoles }: ProtectedRouteProps) => {
  const token = localStorage.getItem('token');
  const userRole = localStorage.getItem('role')?.toUpperCase() || "";

  // 1. Si no hay token, al Login
  if (!token) {
    return <Navigate to="/auth/login" replace />;
  }

  // 2. Si el rol no está en la lista permitida, al Home (o una página 403)
  const isAllowed = allowedRoles.some(role => userRole.includes(role));
  
  if (!isAllowed) {
    return <Navigate to="/" replace />;
  }

  // 3. Si todo ok, renderiza la página
  return children;
};

export default ProtectedRoute;