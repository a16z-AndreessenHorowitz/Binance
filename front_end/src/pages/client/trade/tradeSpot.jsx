import HeaderTradeCoin from "../../../components/layout/headerTradeCoin";
import TradeHeader from "../../../components/layout/TradeHeader";
import OrderBook from "./orderBook";
import "./tradeSpot.css"
 import {useParams} from "react-router-dom"

function TradeSpot() {
  const {symbol}=useParams();
  console.log(symbol)
  return (
    <>
      <div className="tradeSpot">
        <HeaderTradeCoin/>
        <div id="trd-basic-layout-container">
          <TradeHeader/>
          <OrderBook symbol={symbol}/>
        </div>
      </div>
    </>
  );
}

export default TradeSpot;
