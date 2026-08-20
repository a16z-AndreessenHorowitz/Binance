import { BrowserRouter, Routes, Route } from "react-router-dom";
import TradePage from "../pages/client/trading/tradePage";

function AppRoutes() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/markets/overview" element={<TradePage />} />
      </Routes>
    </BrowserRouter>
  );
}

export default AppRoutes;
