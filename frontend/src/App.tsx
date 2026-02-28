import { Routes, Route } from "react-router-dom";
import DeployTesting from "./pages/deployTesting/DeployTesting";
import LandingPage from "./pages/landingPage/LandingPage";
import InfoPage from "./pages/infoPage/InfoPage";
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
      <Route path="/auth/login"    element={<LoginPage />} />
      <Route path="/auth/register"    element={<RegisterPage />} />
      <Route path="/auth/logout"    element={<LogoutPage />} />
    </Routes>
  );
}

export default App;