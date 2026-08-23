import "./tradeHeader.css";

import { useState } from "react";


function formatPrice(price) {
  return Number(price).toLocaleString("en-US", {
    minimumFractionDigits: 2,// tối đa hai số sau dấu thập phân
    maximumFractionDigits: 2,//tối đa hiển thị 2 số sau thập phần
  });
}
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

function TradeHeader() {
  const symbol = "BTCUSDT";
  const [ticker, setTicker] = useState(null);

  const [imageCoin, setImageCoin] = useState("");
  const showPrice = ticker?.lastPrice ? formatPrice(ticker.lastPrice) : "00,000.00";
  const highPrice=ticker?.highPrice ? formatPrice(ticker.highPrice) : "00,000.00";
  const lowPrice=ticker?.lowPrice ? formatPrice(ticker.lowPrice) : "00,000.00";
  const priceChange=ticker?.priceChange ? formatPriceChange(ticker.priceChange) : "00,000.00";
  const priceChangePercent=ticker?.priceChangePercent ? formatPercent(ticker.priceChangePercent) : "00.00%";
  const rawpriceChangePercent=ticker?.priceChangePercent ? ticker.priceChangePercent : "00.00%";
  //cái raw dùng làm css biến đọng 24h
  const volume=ticker?.volume ? formatPrice(ticker.volume) : "00,000.00";
  const quoteVolume=ticker?.volume ? formatPrice(ticker.quoteVolume) : "00,000.00";


return <>
  <div className="tradingPair">
    <div className="childrenContainer">
      <div className="FavoritePairCoin">
        <i className="fa-regular fa-star"></i>
      </div>
      <div className="imagePairCoin">
        <img src={imageCoin || ""} alt="Coin Image" />
      </div>
        <div className="namePairCoin">
          <div className="pairCoin">
            BTC/USDT
            <i className="fa-solid fa-caret-down dropdownCoin"></i>
          </div>
          <div className="linkPairCoin">
            Giá Bitcoin
          </div>
      </div>
      <div className="nowPrice">
        <div className="showPrice">
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
          <div className="showPrice2">
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
        <div className="statLabel">Khối lượng 24h(BTC)</div>
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
        <div className="statLabel">Khối lượng 24h(BTC)</div>
        <div className="statValue">{volume}</div>
      </div>

      <div className="statItem">
        <div className="statLabel">Khối lượng 24h(USDT)</div>
        <div className="statValue">{quoteVolume}</div>
      </div>

      <div className="statItem">
        <div className="statLabel">Mạng lưới</div>
        <div className="statValue">BTC (5)</div>
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
