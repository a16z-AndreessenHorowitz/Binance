
import { useEffect, useState } from "react";
import "./overview.css"
import { getListCoin } from "../../../services/api/api";
import { startListCoinSocket } from "../../../services/socket/listCoinSocket";
import { useNavigate } from "react-router-dom";
function formatMoney(value) {
  if (value === null || value === undefined) {
    return "--";
  }

  return Number(value).toLocaleString("en-US", {
    style: "currency",
    currency: "USD",
    notation: "compact",
    maximumFractionDigits: 2,
  });
}

function formatPrice(price) {
  if (price === null || price === undefined || price === '') {
    return '-';
  }

  const num = Number(price);

  if (!Number.isFinite(num)) return '-';

  // Giá >= 1: luôn ít nhất 2 số lẻ
  if (num >= 1) {
    return num.toLocaleString('en-US', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 5
    });
  }

  // Giá từ 0.01 trở lên: tối đa 5 số lẻ
  if (num >= 0.01) {
    return num.toLocaleString('en-US', {
      maximumFractionDigits: 5
    });
  }

  // Giá nhỏ hơn 0.01:
  // tìm số 0 sau dấu phẩy rồi giữ thêm 5 chữ số có nghĩa
  const str = num.toString();
  const match = str.match(/^0\.(0*)(\d+)/);

  if (match) {
    const leadingZeros = match[1].length;
    const digits = leadingZeros + 5;

    return num.toLocaleString('en-US', {
      maximumFractionDigits: digits
    });
  }

  return num.toString();
}

function formatPriceChangePercent(priceChangePercent) {
  const number=Number(priceChangePercent)

  const formatted=number.toLocaleString("en-US",{
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });

  if(number>0){
    return `+${formatted}%`;
  }

  return `${formatted}%`;
}

function OverView() {
  const [coins, setCoins] = useState([]);
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");
  const navigate=useNavigate()

  //navigate
  function NavigateCoin(binanceSymbol){
    navigate(`/trade/${binanceSymbol}?type=spot`)
  }

  useEffect(() => {
    let ignore = false;

    async function fetchData() {
      try {
        setLoading(true);
        setErrorMessage("");

        const data = await getListCoin(1, 50);
        console.log("LIST COIN:", data);

        if (!ignore) { //() đúng mới chạy cho nên !ignore ở đây ra true
          setCoins(data);
        }
      } catch (error) {
        console.error(error);

        if (!ignore) {
          setErrorMessage(error.message || "Fetch list coin failed");
        }
      } finally {
        if (!ignore) {
          setLoading(false); 
        }
      }
    }

    fetchData();

    //Cái (ticker) => {} là callback function. Khi WebSocket nhận được dữ liệu mới, 
    //startListCoinSocket sẽ gọi callback này và truyền dữ liệu vào ticker.

    const stopListCoinSocket = startListCoinSocket((ticker) => {
      // ticker = { symbol: "BTCUSDT", price: 69187.55, priceChangePercent: 7.42, quoteVolume: 2125484288.60 }
      setCoins((prevCoins) =>
        prevCoins.map((coin) => {
          if (coin.binanceSymbol === ticker.symbol) {
            return {
              ...coin,
              price: ticker.price,
              priceChangePercent: ticker.priceChangePercent,
              quoteVolume: ticker.quoteVolume,
            };
          }
          return coin;
        })
      );
    });

    return () => {
      ignore = true; //làm vậy tránh nó setcoin khi unmount
      stopListCoinSocket();
    };
  }, []);





  return <>
    <div className="market-view">
      {loading && <div className="marketStatus">Loading...</div>}
      {errorMessage && <div className="marketStatus marketStatus--error">{errorMessage}</div>}

      {!loading && !errorMessage && coins.map((coin) => (
        <div className="listCoin" key={coin.id} onClick={()=>{
          NavigateCoin(coin.binanceSymbol)
        }}>
            {/* box info  */}
            <div className="infoCoin">
              <div className="box_info">
                <img src={coin.image} alt={coin.name} />
                <div className="tickerSymbol">{coin.symbol}</div>
                <div className="nameCoin">{coin.name}</div>
              </div>
            </div>
            {/* box giá */}
            <div className="priceCoin">
              <div className="showPrice">
                {coin.price != null ? `${formatPrice(coin.price)}` : "--"}
              </div>
              <div className="subPrice">
                {coin.price != null ? `$${formatPrice(coin.price)}` : "--"}
              </div>
            </div>

            <div className={
               Number(coin.priceChangePercent) > 0
              ? "percentChange statValue--green"
              : "percentChange statValue--red"
            }>
              {coin.priceChangePercent != null ? formatPriceChangePercent(coin.priceChangePercent) : "--"}
            </div>

            {/* box thay đổi % */}
            <div className="Volume24h">
              {coin.quoteVolume != null ? formatMoney(coin.quoteVolume) : "--"}
            </div>

          {/* box marketcap */}
          <div className="marketcap">
            {formatMoney(coin.marketCap)}
          </div>

          <div className="takking_action">
            <i className="fa-solid fa-magnifying-glass-chart"></i>
            <i className="fa-solid fa-chart-line"></i>
          </div>
        </div>
      ))}
    </div>
  </>;
}

export default OverView;
