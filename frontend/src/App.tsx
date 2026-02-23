import { Routes, Route } from "react-router-dom";
import DeployTesting from "./pages/deployTesting/DeployTesting";
import LandingPage from "./pages/landingPage/LandingPage";
import InfoPage from "./pages/infoPage/InfoPage";

function App() {
  return (
    <Routes>
      <Route path="/deploy_testing" element={<DeployTesting />} />
      <Route path="/" element={<LandingPage />} />
      <Route path="/infoAlumnos"   element={<InfoPage userType="alumno" />} />
      <Route path="/infoProfesores" element={<InfoPage userType="profesor" />} />
      <Route path="/infoDueños"    element={<InfoPage userType="dueno" />} />
    </Routes>
  );
}

export default App;