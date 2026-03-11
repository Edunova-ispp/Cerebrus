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
import MediasCurso from "./pages/estadisticasCurso/MediasCurso";
import InfoPage from "./pages/infoPage/InfoPage";
import LandingPage from "./pages/landingPage/LandingPage";
import MapaCurso from "./pages/mapaCurso/MapaCurso";
import MisCursos from "./pages/misCursos/MisCursos";
import OrdenacionAlumno from "./pages/ordenacionAlumno/OrdenacionAlumno";
import ListaTemasCursoProfesor from "./pages/temasDelCurso/ListaTemasCursoProfesor";
import TeoriaAlumno from "./pages/TeoriaAlumno/TeoriaAlumno";
import TestAlumno from "./pages/testAlumno/TestAlumno";

function App() {
  return (
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
      <Route path="/clasificaciones/:clasificacionId/alumno" element={<ClasificacionAlumno />} />
      <Route path="/actividades/teoria/:actividadId" element={<TeoriaAlumno />} />

      <Route path="/generales/test/:testId/alumno" element={<TestAlumno />} />
      <Route path="/estadisticas/:id" element={<EstadisticasCurso />} />
      <Route path="/medias/:id" element={<MediasCurso />} />
    </Routes>
  );
}

export default App;