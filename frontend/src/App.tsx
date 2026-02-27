import { Routes, Route } from "react-router-dom";
import DeployTesting from "./pages/deployTesting/DeployTesting";
import LandingPage from "./pages/landingPage/LandingPage";
import InfoPage from "./pages/infoPage/InfoPage";
import MisCursos from "./pages/misCursos/MisCursos";
import CrearCurso from "./pages/crearCurso/CrearCurso";

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
    </Routes>
  );
}

export default App;