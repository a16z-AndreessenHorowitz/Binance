import { Routes, Route } from "react-router-dom";
import OverView from "../pages/client/market/overview";
import TradeSpot from "../pages/client/trade/tradeSpot";

function AppRoutes() {
  return (
      <Routes>
        <Route path="/markets/overview" element={<OverView />} />
        <Route path="/trade/:symbol" element={<TradeSpot/>}/>
      </Routes>
  );
}

export default AppRoutes;
