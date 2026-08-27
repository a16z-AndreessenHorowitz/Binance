import { useState } from "react";
import HeaderTradeCoin from "../../../components/layout/headerTradeCoin";
import TradeHeader from "../../../components/layout/tradeHeader";
import OrderBook from "./orderBook";
import TradingView from "./tradingView";
import "./tradeSpot.css"
 import {useParams} from "react-router-dom"

function TradeSpot() {
  const {symbol}=useParams();
  const [currentPrice,setCurrentPrice]=useState(null);
  return (
    <>
      <div className="tradeSpot">
        <HeaderTradeCoin />
        <div id="trd-basic-layout-container">
          <TradeHeader symbol={symbol} onPriceUpdate={setCurrentPrice}/>
         <div className="trade-main-content">
           <OrderBook symbol={symbol} currentPrice={currentPrice}/>
          <TradingView symbol={symbol} />
         </div>
        </div>
      </div>
    </>
  );
}

export default TradeSpot;
