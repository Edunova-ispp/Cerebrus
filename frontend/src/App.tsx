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
import ListaTemasCursoProfesor from "./pages/temasDelCurso/ListaTemasCursoProfesor";
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
      <Route path="/deploy_testing" element={<DeployTesting />} />
      <Route path="/" element={<LandingPage />} />
      <Route path="/infoAlumnos"   element={<InfoPage userType="alumno" />} />
      <Route path="/infoProfesores" element={<InfoPage userType="profesor" />} />
      <Route path="/infoDueños"    element={<InfoPage userType="dueno" />} />
      <Route path="/misCursos"     element={<MisCursos />} />
      <Route path="/crearCurso"    element={<CrearCurso />} />
      <Route path="/editarCurso/:id" element={<EditarCurso />} />
      <Route path="/mapa/:id"      element={<MapaCurso />} />
      <Route path="/cursos/:id"    element={<DetalleCurso />} />
      <Route path="/cursos/:id/temas"    element={<ListaTemasCursoProfesor />} />
      <Route path="/cursos/:id/temas/crear" element={<CrearTema />} />
      <Route path="/cursos/:id/temas/:temaId/editar" element={<EditarTema />} />
      <Route path="/cursos/:id/temas/:temaId/actividades/:actividadId/editar" element={<EditarActividad />} />
      <Route path="/auth/login"    element={<LoginPage />} />
      <Route path="/auth/register"    element={<RegisterPage />} />
      <Route path="/auth/logout"    element={<LogoutPage />} />
      <Route path="/cursos/:id/temas/:temaId/actividades/crear" element={<CrearActividad />} />
      <Route path="/ordenaciones/:ordenacionId/alumno" element={<OrdenacionAlumno />} />
      <Route path="/marcar-imagenes/:marcarImagenId/alumno" element={<MarcarImagenAlumno />} />
      <Route path="/clasificaciones/:clasificacionId/alumno" element={<ClasificacionAlumno />} />
      <Route path="/crucigrama/:crucigramaId/alumno" element={<CrucigramaAlumno />} />
      <Route path="/actividades/teoria/:actividadId" element={<TeoriaAlumno />} />

      <Route path="/generales/test/:testId/alumno" element={<TestAlumno />} />
      <Route path="/tableros/:tableroId/alumno" element={<TableroAlumno />} />
      <Route path="/generales/carta/:cartaId/alumno" element={<CartaAlumno />} />
      <Route path="/estadisticas/:id" element={<EstadisticasCurso />} />
      <Route path="/estadisticas/:id/actividades" element={<EstadisticasActividades />} />
      <Route path="/estadisticas/:cursoId/actividades/graficas" element={<GraficasActividades />} />
      <Route path="/estadisticas/:id/temas" element={<EstadisticasTemas />} />
      <Route path="/estadisticas/:cursoId/temas/graficas" element={<GraficasTemas />} />
      <Route path="/perfil" element={<Perfil />} />
      <Route path="/estadisticas/cursos/:cursoId/alumno/:alumnoNombre" element={<EstadisticasAlumno />} />
      <Route path="/estadisticas/temas/:id" element={<EstadisticasTema />} />
      <Route path="/estadisticas/actividades/:id" element={<EstadisticasActividad />} />
    </Routes>
    </WatchbugErrorBoundary>
  );
}

export default App;