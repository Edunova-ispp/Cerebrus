import { Component } from "react";
import { Route, Routes } from "react-router-dom";
import LoginPage from "./pages/auth/login/LoginPage";
import LogoutPage from "./pages/auth/logout/LogoutPage";
import RegisterPage from "./pages/auth/register/RegisterPage";
import ClasificacionAlumno from "./pages/clasificacionAlumno/ClasificacionAlumno";
import CrearActividad from "./pages/crearActividad/crearActividad.tsx";
import CrearCurso from "./pages/crearCurso/CrearCurso";
import CrearTema from "./pages/crearTema/CrearTema";
import DeployTesting from "./pages/deployTesting/DeployTesting";
import DetalleCurso from "./pages/detalleCurso/DetalleCurso";
import EditarActividad from "./pages/editarActividad/EditarActividad";
import EditarCurso from "./pages/editarCurso/EditarCurso";
import EditarTema from "./pages/editarTema/EditarTema";
import EstadisticasCurso from "./pages/estadisticasCurso/EstadisticasCurso";
import EstadisticasActividades from "./pages/estadisticasCurso/EstadisticasActividades.tsx";
import EstadisticasTemas from "./pages/estadisticasCurso/EstadisticasTemas";
import GraficasActividades from "./pages/estadisticasCurso/GraficasActividades";
import GraficasTemas from "./pages/estadisticasCurso/GraficasTemas";
import InfoPage from "./pages/infoPage/InfoPage";
import LandingPage from "./pages/landingPage/LandingPage";
import MapaCurso from "./pages/mapaCurso/MapaCurso";
import MisCursos from "./pages/misCursos/MisCursos";
import OrdenacionAlumno from "./pages/ordenacionAlumno/OrdenacionAlumno";
import TeoriaAlumno from "./pages/TeoriaAlumno/TeoriaAlumno";
import MarcarImagenAlumno from "./pages/marcarImagenAlumno/MarcarImagenAlumno";
import Perfil from "./pages/perfil/Perfil";
import TableroAlumno from "./pages/tableroAlumno/TableroAlumno";
import CartaAlumno from "./pages/cartaAlumno/CartaAlumno";
import TestAlumno from "./pages/testAlumno/TestAlumno";
import CrucigramaAlumno from "./pages/crucigramaAlumno/CrucigramaAlumno";
import EstadisticasAlumno from "./pages/estadisticasCurso/EstadisticasAlumno.tsx";
import EstadisticasActividad from "./pages/estadisticasCurso/EstadisticasActividad.tsx";
import EstadisticasTema from "./pages/estadisticasCurso/EstadisticasTema.tsx";
import ProtectedRoute from "./components/ProtectedRoute.tsx";
import PreguntaAbiertaAlumno from "./pages/preguntaAbiertaAlumno/PreguntaAbiertaAlumno.tsx";
import TermsPage from "./pages/legal/TermsPage.tsx";

// ErrorBoundary que captura errores de componentes React y los pasa a Watchbug
class WatchbugErrorBoundary extends Component<
  { children: React.ReactNode },
  { hasError: boolean }
> {
  constructor(props: { children: React.ReactNode }) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError() {
    return { hasError: true };
  }

  componentDidCatch(error: Error, info: React.ErrorInfo) {
    const ws = (globalThis as any).WatchbugState;
    if (ws) {
      ws.errors.push({
        type: 'react',
        message: error.message,
        stack: (error.stack ?? '') + '\n\nComponent Stack:\n' + info.componentStack,
        timestamp: new Date().toISOString(),
      });
      // Actualizar badge si el widget ya está montado
      if (typeof (globalThis as any).updateErrorBadge === 'function') {
        (globalThis as any).updateErrorBadge();
      }
    }
  }

  render() {
    if (this.state.hasError) {
      return (
        <div style={{ padding: '40px', textAlign: 'center' }}>
          <h2>Algo salió mal</h2>
          <p>Usa el botón 🐛 para reportar el problema.</p>
          <button onClick={() => this.setState({ hasError: false })}>
            Reintentar
          </button>
        </div>
      );
    }
    return this.props.children;
  }
}

function App() {
  return (
    <WatchbugErrorBoundary>
      <Routes>
      {/* Rutas públicas */}
      <Route path="/terminos" element={<TermsPage />} />
      <Route path="/deploy_testing" element={<DeployTesting />} />
      <Route path="/" element={<LandingPage />} />
      <Route path="/auth/login"    element={<LoginPage />} />
      <Route path="/auth/register"    element={<RegisterPage />} />
      <Route path="/auth/logout"    element={<LogoutPage />} />
      <Route path="/infoAlumnos"   element={<InfoPage userType="alumno" />} />
      <Route path="/infoProfesores" element={<InfoPage userType="profesor" />} />
      <Route path="/infoDueños"    element={<InfoPage userType="dueno" />} />

      {/* Rutas roles */}
      <Route path="/misCursos" element={
          <ProtectedRoute allowedRoles={['ALUMNO', 'MAESTRO', 'DUENO']}><MisCursos /></ProtectedRoute>
        } />
        <Route path="/cursos/:id" element={
          <ProtectedRoute allowedRoles={['ALUMNO', 'MAESTRO', 'DUENO']}><DetalleCurso /></ProtectedRoute>
        } />
        <Route path="/perfil" element={
          <ProtectedRoute allowedRoles={['ALUMNO', 'MAESTRO', 'DUENO']}><Perfil /></ProtectedRoute>
        } />

      {/* Rutas alumnos */}
      <Route path="/mapa/:id" element={
          <ProtectedRoute allowedRoles={['ALUMNO']}><MapaCurso /></ProtectedRoute>
        } />
        <Route path="/ordenaciones/:ordenacionId/alumno" element={
          <ProtectedRoute allowedRoles={['ALUMNO']}><OrdenacionAlumno /></ProtectedRoute>
        } />
        <Route path="/marcar/:marcarImagenId/alumno" element={
          <ProtectedRoute allowedRoles={['ALUMNO']}><MarcarImagenAlumno /></ProtectedRoute>
        } />
        <Route path="/marcar-imagenes/:marcarImagenId/alumno" element={
          <ProtectedRoute allowedRoles={['ALUMNO']}><MarcarImagenAlumno /></ProtectedRoute>
        } />
        <Route path="/clasificaciones/:clasificacionId/alumno" element={
          <ProtectedRoute allowedRoles={['ALUMNO']}><ClasificacionAlumno /></ProtectedRoute>
        } />
        <Route path="/crucigrama/:crucigramaId/alumno" element={
          <ProtectedRoute allowedRoles={['ALUMNO']}><CrucigramaAlumno /></ProtectedRoute>
        } />
        <Route path="/actividades/teoria/:actividadId" element={
          <ProtectedRoute allowedRoles={['ALUMNO']}><TeoriaAlumno /></ProtectedRoute>
        } />
        <Route path="/generales/test/:testId/alumno" element={
          <ProtectedRoute allowedRoles={['ALUMNO']}><TestAlumno /></ProtectedRoute>
        } />
        <Route path="/tableros/:tableroId/alumno" element={
          <ProtectedRoute allowedRoles={['ALUMNO']}><TableroAlumno /></ProtectedRoute>
        } />
        <Route path="/generales/carta/:cartaId/alumno" element={
          <ProtectedRoute allowedRoles={['ALUMNO']}><CartaAlumno /></ProtectedRoute>
        } />
        <Route path="/abierta/:actividadId/alumno" element={
          <ProtectedRoute allowedRoles={['ALUMNO']}><PreguntaAbiertaAlumno /></ProtectedRoute>
        } />
        <Route path="/estadisticas/:id" element={
          <ProtectedRoute allowedRoles={['ALUMNO']}><EstadisticasAlumno /></ProtectedRoute>
        } />

      {/* Rutas profesores y dueños */}
        <Route path="/crearCurso" element={
          <ProtectedRoute allowedRoles={['MAESTRO', 'DUENO']}><CrearCurso /></ProtectedRoute>
        } />  
        <Route path="/editarCurso/:id" element={
          <ProtectedRoute allowedRoles={['MAESTRO', 'DUENO']}><EditarCurso /></ProtectedRoute>
        } /> 
        <Route path="/cursos/:id/temas/crear" element={
          <ProtectedRoute allowedRoles={['MAESTRO', 'DUENO']}><CrearTema /></ProtectedRoute>
        } /> 
        <Route path="/cursos/:id/temas/:temaId/editar" element={
          <ProtectedRoute allowedRoles={['MAESTRO', 'DUENO']}><EditarTema /></ProtectedRoute>
        } /> 
        <Route path="/cursos/:id/temas/:temaId/actividades/:actividadId/editar" element={
          <ProtectedRoute allowedRoles={['MAESTRO', 'DUENO']}><EditarActividad /></ProtectedRoute>
        } /> 
        <Route path="/cursos/:id/temas/:temaId/actividades/crear" element={
          <ProtectedRoute allowedRoles={['MAESTRO', 'DUENO']}><CrearActividad /></ProtectedRoute>
        } /> 
        <Route path="/estadisticas/:id/actividades" element={
          <ProtectedRoute allowedRoles={['MAESTRO', 'DUENO']}><EstadisticasActividades /></ProtectedRoute>
        } /> 
        <Route path="/estadisticas/:cursoId/actividades/graficas" element={
          <ProtectedRoute allowedRoles={['MAESTRO', 'DUENO']}><GraficasActividades /></ProtectedRoute>
        } /> 
        <Route path="/estadisticas/:id/temas" element={
          <ProtectedRoute allowedRoles={['MAESTRO', 'DUENO']}><EstadisticasTemas /></ProtectedRoute>
        } />
        <Route path="/estadisticas/:cursoId/temas/graficas" element={
          <ProtectedRoute allowedRoles={['MAESTRO', 'DUENO']}><GraficasTemas /></ProtectedRoute>
        } />
        <Route path="/estadisticas/cursos/:cursoId/alumno/:alumnoNombre" element={
          <ProtectedRoute allowedRoles={['MAESTRO', 'DUENO']}><EstadisticasAlumno /></ProtectedRoute>
        } />
        <Route path="/estadisticas/temas/:id" element={
          <ProtectedRoute allowedRoles={['MAESTRO', 'DUENO']}><EstadisticasTema /></ProtectedRoute>
        } />
        <Route path="/estadisticas/actividades/:id" element={
          <ProtectedRoute allowedRoles={['MAESTRO', 'DUENO']}><EstadisticasActividad /></ProtectedRoute>
        } />
    </Routes>
    </WatchbugErrorBoundary>
  );
}

export default App;