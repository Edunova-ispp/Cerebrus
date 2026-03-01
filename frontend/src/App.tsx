import { Route, Routes } from "react-router-dom";
import LoginPage from "./pages/auth/login/LoginPage";
import LogoutPage from "./pages/auth/logout/LogoutPage";
import RegisterPage from "./pages/auth/register/RegisterPage";
import CrearCurso from "./pages/crearCurso/CrearCurso";
import DeployTesting from "./pages/deployTesting/DeployTesting";
import DetalleCurso from "./pages/detalleCurso/DetalleCurso";
import InfoPage from "./pages/infoPage/InfoPage";
import LandingPage from "./pages/landingPage/LandingPage";
import MisCursos from "./pages/misCursos/MisCursos";
import Placeholder from "./pages/placeholder/placeholder";
import EditarCurso from "./pages/editarCurso/EditarCurso";
import ListaTemasCursoProfesor from "./pages/temasDelCurso/ListaTemasCursoProfesor";
import CrearTema from "./pages/crearTema/CrearTema";

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
      <Route path="/mapa/:id"      element={<Placeholder />} />
      <Route path="/cursos/:id"    element={<DetalleCurso />} />
      <Route path="/cursos/:id/temas"    element={<ListaTemasCursoProfesor />} />
      <Route path="/cursos/:id/temas/crear" element={<CrearTema />} />
      <Route path="/auth/login"    element={<LoginPage />} />
      <Route path="/auth/register"    element={<RegisterPage />} />
      <Route path="/auth/logout"    element={<LogoutPage />} />
    </Routes>
  );
}

export default App;