import { useState,useEffect, useRef } from "react";
import "./orderBook.css"
import {startOrderBookSocket} from "../../../services/socket/orderBookSocket"
import { getOrderBook } from "../../../services/api/api";
import formatPrice from "../../../utils/formatPrice";


function formatAmount(qty) {
  if (!qty) return "--";
  const num = Number(qty);

  if (num >= 1_000_000) {
    return (num / 1_000_000).toFixed(2).replace(".", ",") + "M"; // 1,140,000 → 1,14M
  } else if (num >= 1000) {
    return (num / 1000).toFixed(2).replace(".", ",") + "K";      // 21385 → 21,38K
  } else if (num >= 1) {
    return num.toFixed(4).replace(".", ",");                                        // 1.2345 → 1.2345
  } else {
    return num.toFixed(5).replace(".", ",");                                        // 0.00007 → 0.00007
  }
}


function formatTotal(price, qty) {
  const total = Number(price) * Number(qty);
  if (total >= 1_000_000_000) {
    return (total / 1_000_000_000).toFixed(2).replace(".", ",") + "B"; // 1.14B
  } else if (total >= 1_000_000) {
    return (total / 1_000_000).toFixed(2).replace(".", ",") + "M";     // 1,14M
  } else if (total >= 1000) {
    return (total / 1000).toFixed(2).replace(".", ",") + "K";          // 21,38K
  }
  return total.toFixed(6).replace(".", ",");
}


function OrderBook({ symbol, currentPrice }){
  
  const [orderBook,setOrderBook]=useState({bids: [], asks: []});
  
  //làm màu xanh đỏ cho price
  const [priceDirection, setPriceDirection] = useState("up");
  const prevPriceRef = useRef(null);

  useEffect(() => {
    if (currentPrice) {
      const nextPrice = Number(currentPrice);
      if (prevPriceRef.current !== null) {
        if (nextPrice > prevPriceRef.current) {
          setPriceDirection("up");
        } else if (nextPrice < prevPriceRef.current) {
          setPriceDirection("down");
        }
      } else {
        //giá đầu tiên là màu xanh
        setPriceDirection("up");
      }
      prevPriceRef.current = nextPrice;
    }
  }, [currentPrice]);

  //làm màu xanh đỏ cho price
    
  useEffect(() => {
    if(!symbol) return;
    setPriceDirection("up");
    prevPriceRef.current = null;

    console.log("Đang lắng nghe sổ lệnh cho:", symbol);
    // 1. Báo cho Backend biết để Backend connect ra Binance WebSocket
    getOrderBook(symbol);

    // 2. Lắng nghe dữ liệu realtime từ Backend đẩy về
    // data={symbol:`BTCUSDT`,lastUpdateId:1234567, asks: [ ["79740.92", "0.00007"], ["79739.88", "0.04029"], ... ], // Lệnh Bán (đỏ)
    // bids: [ ["79736.05", "0.28202"], ["79735.10", "0.15000"], ... ]  // Lệnh Mua (xanh)

    const stopOrderBookSocket=startOrderBookSocket(symbol,(data)=>{
      if(data){
        setOrderBook({
          bids: data.bids || [],
          asks: data.asks || [],
        }); 
      }


    });

    //clean up khi đổi coin
    return ()=>{
      stopOrderBookSocket();
    };
  }, [symbol]);

   // Lấy top 17 lệnh bán (Asks) và đảo ngược để giá thấp nhất nằm gần đường ticker ở giữa
    const asksList= (orderBook.asks || []).slice(0,17).reverse();
    // Lấy top 17 lệnh mua (Bids)
    const bidsList= (orderBook.bids || []).slice(0,17)
    

      // Tìm khối lượng lớn nhất trong danh sách để làm mốc 100%
  const maxAskQty = Math.max(...asksList.map(([_, qty]) => Number(qty) || 0), 1);
  const maxBidQty = Math.max(...bidsList.map(([_, qty]) => Number(qty) || 0), 1);

  
  return <>
    <div className="orderBook">
      <div className="nameBook">
        Sổ lệnh 
        <i className="fa-solid fa-ellipsis"></i>
      </div>
      <div className="orderbook-header">
        <div className="orderbook-header-tips">
          <i className="fa-solid fa-table-columns"></i>
          <i className="fa-solid fa-table-columns"></i>
          <i className="fa-solid fa-table-columns"></i>
        </div> 
        <div className="orderbook-tickSize">
          0.01 <i className="fa-solid fa-caret-down"></i>
        </div> 
      </div>  
      <div className="content">
          <div className="item">Giá(USDT)</div>
          <div className="item">Số lượng (BTC)</div>
          <div className="item">Tổng</div>
      </div>
      {/* order book bán*/}
      <div className="orderbook-list-container">
        {/* đây là div ban đầu khi chưa cho thêm linear-gradient  */}
        {/* {asksList.map(([price, qty], index) => (
          <div key={`ask-${index}`} className="orderbook-row">
            <div className="emit-price ask-price">{formatPrice(price)}</div>
            <div className="emit-price">{formatAmount(qty)}</div>
            <div className="emit-price">{formatTotal(price, qty)}</div>
          </div>
        ))} */}


        {asksList.map(([price, qty], index) => {
          const percent = Math.min(((Number(qty) || 0) / maxAskQty) * 100, 100);
            return (
              <div
                key={`ask-${index}`}
                className="orderbook-row"
                style={{
                  background: `linear-gradient(to left, rgba(246, 70, 93, 0.15) ${percent}%, transparent ${percent}%)`,
                }}
              >
                <div className="emit-price ask-price">{formatPrice(price)}</div>
                <div className="emit-price">{formatAmount(qty)}</div>
                <div className="emit-price">{formatTotal(price, qty)}</div>
              </div>
              );
            })}
      </div>

      <div className="orderbook-ticker">
        <div className={`price ${priceDirection}`}>
          {formatPrice(currentPrice)}
        </div>
        <div className="subPrice">
          ${formatPrice(currentPrice)}
        </div>
        <i className="fa-solid fa-chevron-right"></i>
      </div>
      {/* order book mua*/}
      <div className="orderbook-list-container">
        {/* tương tự đây nhé gradiend  */}
        {/* {bidsList.map(([price, qty], index) => (
          <div key={`bid-${index}`} className="orderbook-row">
            <div className="emit-price bid-price">{formatPrice(price)}</div>
            <div className="emit-price">{formatAmount(qty)}</div>
            <div className="emit-price">{formatTotal(price, qty)}</div>
          </div>
        ))} */}

        {bidsList.map(([price, qty], index) => {
          const percent = Math.min(((Number(qty) || 0) / maxBidQty) * 100, 100);
          return (
            <div
              key={`bid-${index}`}
              className="orderbook-row"
              style={{
                background: `linear-gradient(to left, rgba(14, 203, 129, 0.15) ${percent}%, transparent ${percent}%)`,
              }}
            >
              <div className="emit-price bid-price">{formatPrice(price)}</div>
              <div className="emit-price">{formatAmount(qty)}</div>
              <div className="emit-price">{formatTotal(price, qty)}</div>
            </div>
          );
        })}
       </div>


    </div>
  </>
}
export default OrderBook;