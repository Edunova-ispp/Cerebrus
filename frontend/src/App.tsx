import { Route, Routes } from "react-router-dom";
import LoginPage from "./pages/auth/login/LoginPage";
import LogoutPage from "./pages/auth/logout/LogoutPage";
import RegisterPage from "./pages/auth/register/RegisterPage";
import CrearCurso from "./pages/crearCurso/CrearCurso";
import DeployTesting from "./pages/deployTesting/DeployTesting";
import DetalleCurso from "./pages/detalleCurso/DetalleCurso";
import EditarCurso from "./pages/editarCurso/EditarCurso";
import EstadisticasCurso from "./pages/estadisticasCurso/EstadisticasCurso";
import InfoPage from "./pages/infoPage/InfoPage";
import LandingPage from "./pages/landingPage/LandingPage";
import MisCursos from "./pages/misCursos/MisCursos";
import Placeholder from "./pages/placeholder/placeholder";

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
      <Route path="/auth/login"    element={<LoginPage />} />
      <Route path="/auth/register"    element={<RegisterPage />} />
      <Route path="/auth/logout"    element={<LogoutPage />} />
      <Route path="/estadisticas/:id" element={<EstadisticasCurso />} />
    </Routes>
  );
}

export default App;