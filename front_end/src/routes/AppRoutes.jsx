import { BrowserRouter, Routes, Route } from "react-router-dom";
import OverView from "../pages/client/market/overview";

function AppRoutes() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/markets/overview" element={<OverView />} />
      </Routes>
    </BrowserRouter>
  );
}

export default AppRoutes;
