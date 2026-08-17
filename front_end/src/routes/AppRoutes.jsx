import { BrowserRouter, Routes, Route } from "react-router-dom";
import TradePage from "../pages/client/trading/TradePage";

function AppRoutes() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<TradePage />} />
      </Routes>
    </BrowserRouter>
  );
}

export default AppRoutes;
