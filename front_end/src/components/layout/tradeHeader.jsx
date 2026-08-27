import "./tradeHeader.css";
import { useEffect, useState, useRef } from "react";
import { getCoinTicker } from "../../services/api/api";
import { startTickerSocket } from "../../services/socket/tickerSocket";
import formatPrice from "../../utils/formatPrice";

function formatPercent(price) {
  const number=Number(price)

  const formatted=number.toLocaleString("en-US",{
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });

  if(number>0){
    return `+${formatted}%`;
  }

  return `${formatted}%`;
}
function formatPriceChange(price) {
  return Number(price).toLocaleString("en-US", {
    minimumFractionDigits: 2,// tối đa hai số sau dấu thập phân
  });
}


function TradeHeader({symbol , onPriceUpdate}) {
  const [ticker, setTicker] = useState(null);
  //dùng để làm màu xanh đỏ cho price
  const [priceDirection, setPriceDirection] = useState("up");
  const prevPriceRef = useRef(null);
  //

  const [imageCoin, setImageCoin] = useState("");
  const showPrice = ticker?.lastPrice ? formatPrice(ticker.lastPrice) : "00,000.00";
  const highPrice=ticker?.highPrice ? formatPrice(ticker.highPrice) : "00,000.00";
  const lowPrice=ticker?.lowPrice ? formatPrice(ticker.lowPrice) : "00,000.00";
  const priceChange=ticker?.priceChange ? formatPriceChange(ticker.priceChange) : "00,000.00";
  const priceChangePercent=ticker?.priceChangePercent ? formatPercent(ticker.priceChangePercent) : "00.00%";
  const rawpriceChangePercent=ticker?.priceChangePercent ? ticker.priceChangePercent : "00.00%";
  //cái raw dùng làm css biến đọng 24h
  const volume=ticker?.volume ? formatPrice(ticker.volume) : "00,000.00";
  const quoteVolume=ticker?.quoteVolume ? formatPrice(ticker.quoteVolume) : "00,000.00";

  useEffect(()=>{
    if(!symbol) return;
    //dùng để màu xanh đỏ giá price
    setPriceDirection("up");
    prevPriceRef.current = null;
    //

    console.log("Đang lắng nghe sổ lệnh cho ticker:", symbol);
    
    // 1. Báo cho Backend biết để Backend connect ra Binance WebSocket + lấy ảnh coin
    getCoinTicker(symbol).then((data) => {
      if (data?.imageUrl) {
        setImageCoin(data.imageUrl);
      }
    });

    const stopStickerSocket=startTickerSocket(symbol,(data)=>{
      setTicker(data);
      
      //dùng để làm màu xanh đỏ cho price
      if (data?.lastPrice) {
        const nextPrice = Number(data.lastPrice);
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
      //cập nhật giá ra ngoài
      onPriceUpdate(data.lastPrice);
      //
    })


    //clean up
    return ()=>{
      stopStickerSocket();
    };
  },[symbol])

return <>
  <div className="tradingPair">
    <div className="childrenContainer">
      <div className="FavoritePairCoin">
        <i className="fa-regular fa-star"></i>
      </div>
      <div className="imagePairCoin">
        <img src={imageCoin || null } alt="Coin Image" />
      </div>
        <div className="namePairCoin">
          <div className="pairCoin">
            {symbol}
            <i className="fa-solid fa-caret-down dropdownCoin"></i>
          </div>
          <div className="linkPairCoin">
            Giá {symbol.substring(0,3)}
          </div>
      </div>
      <div className="nowPrice">
        <div className={`showPrice ${priceDirection === "up" ? "statValue--green" : priceDirection === "down" ? "statValue--red" : ""}`}>
          {showPrice}
        </div>
        <div className="subPrice">
          ${showPrice}
        </div>
      </div>
      
    </div>

    <div className="tradingStats2">
      <div className="box_1">
        <div className="nowPrice2">
          {/* <div className={`showPrice2 ${priceDirection === "up" ? "statValue--green" : priceDirection === "down" ? "statValue--red" : ""}`}> */}
          <div className={`showPrice2 ${priceDirection === "up" ? "price-up" : priceDirection === "down" ? "price-down" : ""}`}>

            {showPrice}
          </div>
          <div className="subPrice2">
            ${showPrice} 
            <div className={
               Number(rawpriceChangePercent) > 0
              ? " statValue--green"
              : " statValue--red"
            }>
              {priceChangePercent}
            </div>
          </div>
        </div>
        <div className="statValue statValue--yellow tag-text">
          <a>Thanh toán</a>
          <a>Khối lượng</a>
          <a>Phổ biến</a>
          <a>Price Protection</a>
        </div>
          <div className="Chain">
          <div className="statLabel">Mạng lưới</div>
          <div className="ChainCoin">BTC (5)</div>
        </div>
      </div>
      <div className="box_2">
        <div className="statItem">
        <div className="statLabel">Giá cao nhất 24h</div>
        <div className="statValue">{highPrice}</div>
      </div>

      <div className="statItem">
        <div className="statLabel">Giá thấp nhất 24h</div>
        <div className="statValue">{lowPrice}</div>
      </div>

      <div className="statItem">
        <div className="statLabel">Khối lượng 24h({symbol ? symbol.toUpperCase().replace("USDT", "") : ""})</div>
        <div className="statValue">{volume}</div>
      </div>

      <div className="statItem">
        <div className="statLabel">Khối lượng 24h(USDT)</div>
        <div className="statValue">{quoteVolume}</div>
      </div>
      </div>
    </div>

    <div className="tradingStats">
      <div className="statItem">
        <div className="statLabel">Biến động trong 24 giờ</div>
        <div className={
          Number(rawpriceChangePercent) > 0
            ? "statValue statValue--green"
            : "statValue statValue--red"} >
          {priceChange} {priceChangePercent}</div>
        </div>

      <div className="statItem">
        <div className="statLabel">Giá cao nhất 24h</div>
        <div className="statValue">{highPrice}</div>
      </div>

      <div className="statItem">
        <div className="statLabel">Giá thấp nhất 24h</div>
        <div className="statValue">{lowPrice}</div>
      </div>

      <div className="statItem">
        <div className="statLabel">Khối lượng 24h({symbol ? symbol.toUpperCase().replace("USDT", "") : ""})</div>
        <div className="statValue">{volume}</div>
      </div>

      <div className="statItem">
        <div className="statLabel">Khối lượng 24h(USDT)</div>
        <div className="statValue">{quoteVolume}</div>
      </div>

      <div className="statItem">
        <div className="statLabel">Mạng lưới</div>
        <div className="statValue">{symbol ? symbol.toUpperCase().replace("USDT", "") : ""} (5)</div>
      </div>

      <div className="statItem tokenItem">
        <div className="statLabel">Thẻ token</div>

        <div className="statValue statValue--yellow">
          <a>Thanh toán</a>
          <a>Khối lượng</a>
          <a>Phổ biến</a>
          <a>Price Protection</a>
        </div>
      </div>

      {/* <button className="statsNextButton">
        <i class="fa-solid fa-chevron-right"></i>      
      </button> */}
    </div>

  </div>
</>
}

export default TradeHeader;
