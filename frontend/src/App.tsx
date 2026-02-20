import { Routes, Route } from "react-router-dom";
import DeployTesting from "./pages/deployTesting/DeployTesting";
import LandingPage from "./pages/landingPage/LandingPage"; 
function App() {

  return (
    <Routes>
      <Route path="/deploy_testing" element={<DeployTesting />} />
      <Route path="/" element={<LandingPage />} />
    </Routes>
  );
}

export default App;