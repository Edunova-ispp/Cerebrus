import { Route, Routes } from "react-router-dom";
import LoginPage from "./pages/auth/login/LoginPage";
import LogoutPage from "./pages/auth/logout/LogoutPage";
import RegisterPage from "./pages/auth/register/RegisterPage";
//import CourseDetailPage from "./pages/curso/detail/CourseDetailPage";
//import CourseFormPage from "./pages/curso/form/CourseFormPage";
import CourseListingPage from "./pages/curso/listing/CourseListingPage";
import DeployTesting from "./pages/deployTesting/DeployTesting";
import InfoPage from "./pages/infoPage/InfoPage";
import LandingPage from "./pages/landingPage/LandingPage";
import InfoPage from "./pages/infoPage/InfoPage";
import MisCursos from "./pages/misCursos/MisCursos";
import Placeholder from "./pages/placeholder/Placeholder";
import DetalleCurso from "./pages/detalleCurso/DetalleCurso";
import LoginPage from "./pages/auth/login/LoginPage";
import RegisterPage from "./pages/auth/register/RegisterPage";
import LogoutPage from "./pages/auth/logout/LogoutPage";

function App() {
  return (
    <Routes>
      <Route path="/deploy_testing" element={<DeployTesting />} />
      <Route path="/" element={<LandingPage />} />
      <Route path="/infoAlumnos"   element={<InfoPage userType="alumno" />} />
      <Route path="/infoProfesores" element={<InfoPage userType="profesor" />} />
      <Route path="/infoDueños"    element={<InfoPage userType="dueno" />} />
      <Route path="/misCursos"     element={<MisCursos />} />
      <Route path="/crearCurso"    element={<Placeholder />} />
      <Route path="/editarCurso/:id" element={<Placeholder />} />
      <Route path="/mapa/:id"      element={<Placeholder />} />
      <Route path="/cursos/:id"    element={<DetalleCurso />} />
      <Route path="/auth/login"    element={<LoginPage />} />
      <Route path="/auth/register"    element={<RegisterPage />} />
      <Route path="/auth/logout"    element={<LogoutPage />} />
      <Route path="/cursos" element={<CourseListingPage />} />
    </Routes>
  );
}

export default App;