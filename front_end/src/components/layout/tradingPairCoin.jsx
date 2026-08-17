import "./tradingPairCoin.css";
import btcImg from "../../assets/images/btc.png";

function TradingPairCoin() {
return <>
  <div className="tradingPair">
    <div className="childrenContainer">
      <div className="FavoritePairCoin">
        <i className="fa-regular fa-star"></i>
      </div>
      <div className="imagePairCoin">
        <img src={btcImg} alt="Coin Image" />
      </div>
        <div className="namePairCoin">
          <div className="pairCoin">
            BTC/USDT
          </div>
          <div className="linkPairCoin">
            Giá Bitcoin
          </div>
      </div>
      <div className="nowPrice">
        <div className="showPrice">
          26,000.00
        </div>
        <div className="subPrice">
          $26,000.00
        </div>
      </div>
      
    </div>

    <div className="tradingStats">
      <div className="statItem">
        <div className="statLabel">Biến động trong 24 giờ</div>
        <div className="statValue statValue--green">511,43 +0,81%</div>
      </div>

      <div className="statItem">
        <div className="statLabel">Giá cao nhất 24h</div>
        <div className="statValue">63.616,00</div>
      </div>

      <div className="statItem">
        <div className="statLabel">Giá thấp nhất 24h</div>
        <div className="statValue">62.716,00</div>
      </div>

      <div className="statItem">
        <div className="statLabel">Khối lượng 24h(BTC)</div>
        <div className="statValue">7.350,86</div>
      </div>

      <div className="statItem">
        <div className="statLabel">Khối lượng 24h(USDT)</div>
        <div className="statValue">464.450.035,46</div>
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

export default TradingPairCoin;
