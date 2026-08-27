import "./tradingView.css";
import { startKlinesSocket } from "../../../services/socket/klinesSocket";
import { useEffect, useRef, useState } from "react";
import { startKlines } from "../../../services/api/api";
import { useTheme } from "../../../context/ThemeContext";
import formatTime from "../../../utils/formatTime";
import formatPrice from "../../../utils/formatPrice"
// chart
import { createChart, CandlestickSeries, createSeriesMarkers } from 'lightweight-charts';

const INTERVALS = [
  { label: "1s", value: "1s" },
  { label: "15ph", value: "15m" },
  { label: "1h", value: "1h" },
  { label: "4h", value: "4h" },
  { label: "1ng", value: "1d" },
  { label: "1 tuần", value: "1w" },
];
//hàm cập nhật high/lower marker
const updateHighLowerMarkets = (series, markersPrimitiveRef, data) => {
  if (!data || data.length === 0 || !series) return;

  let maxCandle = data[0];
  let minCandle = data[0];

  for (let i = 1; i < data.length; i++) {
    const item = data[i];
    if (item.high > maxCandle.high) maxCandle = item;
    if (item.low < minCandle.low) minCandle = item;
  }

  const markers = [
    {
      time: maxCandle.time,
      position: 'aboveBar',
      color: '#848e9c',
      shape: 'arrowDown',
      text: `${maxCandle.high.toLocaleString('en-US', { minimumFractionDigits: 2 })}`,
    },
    {
      time: minCandle.time,
      position: 'belowBar',
      color: '#848e9c',
      shape: 'arrowUp',
      text: `${minCandle.low.toLocaleString('en-US', { minimumFractionDigits: 2 })}`,
    },
  ];
  if (!markersPrimitiveRef.current) {
    markersPrimitiveRef.current = createSeriesMarkers(series, markers);
  } else {
    markersPrimitiveRef.current.setMarkers(markers);
  }
}

const formatPercent = (value) => {
  const sign = value < 0 ? -1 : 1;
  const absValue = Math.abs(value);
  return (sign * (Math.round((absValue + Number.EPSILON) * 100) / 100)).toFixed(2);
};



function TradingView({ symbol }) {
  //ẩn hiện .chart-title-row  khung trên trandingview
  const [isChartTitleVisible, setIsChartTitleVisible] = useState(true);
  //lấy trạng thái isDark
  const { isDark } = useTheme();
  //

  const chartContainerRef = useRef(null);
  const chartRef = useRef(null);
  const seriesRef = useRef(null);
  const [selectedInterval, setSelectedInterval] = useState("15m");
  const [activeViewMode, setActiveViewMode] = useState("goc");

  //high low market
  const markersRef = useRef(null);// Ref lưu trữ marker
  const klinesDataRef = useRef([]);// Lưu mảng nến hiện tại

  //hoveredCandle sẽ chứa các cây nến hiện tại ví dụ
  // {
  //   time: 1756281600,
  //   open: 79023.75,
  //   high: 79028.49,
  //   low: 78546.13,
  //   close: 78936.77
  // }

  //làm cập nhật giá trên thanh biểu đồ open high low close
  const [hoveredCandle, setHoveredCandle] = useState(null);
  const isCrosshairActiveRef = useRef(false); //lưu trạng thái hover hay không hover

  // === DÁN HÀM NÀY VÀO ĐÂY ===
  const getPreviousCandle = (currentCandle) => {
    if (!currentCandle || !klinesDataRef.current || klinesDataRef.current.length === 0) return null;
    const index = klinesDataRef.current.findIndex(k => k.time === currentCandle.time);
    if (index > 0) {
      return klinesDataRef.current[index - 1];
    }
    return null;
  };

  useEffect(() => {
    if (!symbol || !chartContainerRef.current) return;

    // 1. Khởi tạo biểu đồ Lightweight Charts
    const chart = createChart(chartContainerRef.current, {
      layout: {
        background: { color: isDark ? "#181a20" : "#ffffff" },
        textColor: isDark ? "#848e9c" : "#707a8a",
      },
      grid: {
        vertLines: { color: isDark ? "#1e2329" : "#f0f3fa" },
        horzLines: { color: isDark ? "#1e2329" : "#f0f3fa" },
      },
      crosshair: {
        mode: 0,
        vertLine: {
          color: isDark ? "#474d57" : "#929aa5",
          style: 3,
          labelBackgroundColor: isDark ? "#2b313a" : "#707a8a",
        },
        horzLine: {
          color: isDark ? "#474d57" : "#929aa5",
          style: 3,
          labelBackgroundColor: isDark ? "#2b313a" : "#707a8a",
        },
      },
      timeScale: {
        borderColor: isDark ? "#2b313a" : "#eaecef",
        timeVisible: true,
        secondsVisible: false,
         // Cho phép zoom out
  minBarSpacing: 2,
      },
      rightPriceScale: {
        borderColor: isDark ? "#2b313a" : "#eaecef",
      },
      autoSize: true,
    });

    // 2. Thêm Candlestick Series với màu chuẩn Binance (Xanh tăng / Đỏ giảm)
    const candlestickSeries = chart.addSeries(CandlestickSeries, {
      upColor: "#0ecb81",
      downColor: "#f6465d",
      borderVisible: false,
      wickUpColor: "#0ecb81",
      wickDownColor: "#f6465d",
    });

    chartRef.current = chart;
    seriesRef.current = candlestickSeries;
    //high low
    markersRef.current = null;
    // 3. Gọi API startKlines với interval được chọn
    startKlines(symbol, selectedInterval, 1000)
      .then((response) => {
        if (response && response.historicalKlines) {
          const formattedHistory = response.historicalKlines.map((k) => ({
            time: Math.floor(k[0] / 1000),
            open: parseFloat(k[1]),
            high: parseFloat(k[2]),
            low: parseFloat(k[3]),
            close: parseFloat(k[4]),
          }));
          candlestickSeries.setData(formattedHistory);
          //high low
          klinesDataRef.current = formattedHistory;
          //cập nhật giá cao nhất thấp nhất cho khung thời gian vừa chọn
          updateHighLowerMarkets(
            candlestickSeries, 
            markersRef, 
            formattedHistory
          );

          // Mặc định hiển thị cây nến mới nhất để hiển thị lên khung
          if (formattedHistory.length > 0) {
              setHoveredCandle(
                formattedHistory[formattedHistory.length - 1]
              );
            }
        }
      })
      .catch((error) => {
        console.error("Error fetching historical klines:", error);
      });

    // 4. Subscribe to klines socket để nhận real-time updates theo interval
    const unsubscribe = startKlinesSocket(symbol, selectedInterval, (kline) => {
      if (!kline || !seriesRef.current) return;

      const newCandle = {
        time: Math.floor(kline.startTime / 1000),
        open: parseFloat(kline.openPrice),
        high: parseFloat(kline.highPrice),
        low: parseFloat(kline.lowPrice),
        close: parseFloat(kline.closePrice),
      };

      seriesRef.current.update(newCandle);

      // Cập nhật lại mảng nến & check lại High/Low nếu cây nến mới/hiện tại phá đỉnh hoặc đáy
      const data = klinesDataRef.current;
      if (data && data.length > 0) {
        if (data[data.length - 1].time === newCandle.time) {
          data[data.length - 1] = newCandle;
        } else {
          data.push(newCandle);
        }
        updateHighLowerMarkets(
          seriesRef.current, 
          markersRef, 
          data
        );

        // Nếu chuột không rê trên biểu đồ, luôn cập nhật nến real-time mới nhất
        if (!isCrosshairActiveRef.current) {
          setHoveredCandle(newCandle);
        }

      }
    });

    // 5. Tự động tính High/Low theo đúng vùng nến đang ZOOM/KÉO trên màn hình
    //chat là đối tượng biểu đồ. timeScale = quản lý cách dữ liệu/nến được hiển thị theo chiều ngang. subscribeVisibleLogicalRangeChange Theo dõi sự thay đổi của phạm vi index đang hiển thị trên màn hình.
    chart.timeScale().subscribeVisibleLogicalRangeChange((logicalRange) => {
      if (!logicalRange || !klinesDataRef.current || !klinesDataRef.current.length || !seriesRef.current) return;
      //logicalRanfe sẽ trả về {from : 102, to:205} là cây nến bắt đầu và kết thúc
      //xác định cây nến đầu không cho nó ăm
      const from = Math.max(0, Math.floor(logicalRange.from));
      // xác định cây nến cuối cùng
      const to = Math.min(klinesDataRef.current.length - 1, Math.ceil(logicalRange.to));

      if (from <= to) {
        //lấy các cây nến trong vùng nhìn thấy
        const visibleBars = klinesDataRef.current.slice(from, to + 1);
        if (visibleBars.length > 0) {
          //tính high và low của vùng đang xem
          updateHighLowerMarkets(seriesRef.current, markersRef, visibleBars);
        }
      }
    });

    //
    chart.subscribeCrosshairMove((param) => {
      // Chuột ra ngoài chart
      if (
        param.point === undefined ||
        !param.time ||
        param.point.x < 0 ||
        param.point.x > chartContainerRef.current.clientWidth ||
        param.point.y < 0 ||
        param.point.y > chartContainerRef.current.clientHeight
      ) {
        isCrosshairActiveRef.current=false
        // Quay lại cây nến mới nhất
        if (
          klinesDataRef.current &&
          klinesDataRef.current.length > 0
        ) {
          setHoveredCandle(
            klinesDataRef.current[
              klinesDataRef.current.length - 1
            ]
          );
        }
      } else {
        isCrosshairActiveRef.current=true;  
        // Lấy cây nến tại vị trí chuột
        const candleData =
          param.seriesData.get(candlestickSeries);

        if (candleData) {
          isCrosshairActiveRef.current=true;
          setHoveredCandle(candleData);
        }
      }
    });

    // 6. Clean up khi component unmount hoặc khi đổi symbol/interval
    return () => {
      unsubscribe();
      chart.remove();
      chartRef.current = null;
      seriesRef.current = null;
      markersRef.current = null;
    };
  }, [symbol, selectedInterval]);

  // Cập nhật màu sắc biểu đồ khi chuyển đổi theme Dark / Light
  useEffect(() => {
    if (!chartRef.current) return;

    chartRef.current.applyOptions({
      layout: {
        background: { color: isDark ? "#181a20" : "#ffffff" },
        textColor: isDark ? "#848e9c" : "#707a8a",
      },
      grid: {
        vertLines: { color: isDark ? "#1e2329" : "#f0f3fa" },
        horzLines: { color: isDark ? "#1e2329" : "#f0f3fa" },
      },
      crosshair: {
        vertLine: {
          color: isDark ? "#474d57" : "#929aa5",
          labelBackgroundColor: isDark ? "#2b313a" : "#707a8a",
        },
        horzLine: {
          color: isDark ? "#474d57" : "#929aa5",
          labelBackgroundColor: isDark ? "#2b313a" : "#707a8a",
        },
      },
      timeScale: {
        borderColor: isDark ? "#2b313a" : "#eaecef",
      },
      rightPriceScale: {
        borderColor: isDark ? "#2b313a" : "#eaecef",
      },
    });
  }, [isDark]);

  return (
    <div className="trading-view">
      {/* Header tabs top */}
      <div className="coinInfoTable">
        <div className="tablist">
          <div className="active">Đồ thị</div>
          <div>Thông tin</div>
          <div>Dữ liệu</div>
          <div>Square</div>
        </div>

        <div className="item-centers">
          <div>ai</div>
          <div><i className="fa-regular fa-bell"></i></div>
          <div><i className="fa-solid fa-expand"></i></div>
        </div>
      </div>

      {/* Thanh công cụ Toolbar Binance style */}
      <div className="chart-toolbar">
        <div className="toolbar-left">
          <span className="toolbar-label">Thời gian</span>

          <div className="interval-list">
            {INTERVALS.map((item) => (
              <button
                key={item.value}
                className={`interval-btn ${selectedInterval === item.value ? "active" : ""}`}
                onClick={() => setSelectedInterval(item.value)}
              >
                {item.label}
              </button>
            ))}
            <button className="toolbar-icon-btn dropdown-caret">
              <i className="fa-solid fa-caret-down"></i>
            </button>
          </div>

          <div className="toolbar-divider" />

          {/* Các icon công cụ Binance */}
          <div className="tools-list">
            <button className="toolbar-icon-btn" title="Thời gian">
              <i className="fa-regular fa-calendar-days"></i>
            </button>
            <button className="toolbar-icon-btn" title="So sánh">
              <i className="fa-regular fa-circle-plus"></i>
            </button>
            <button className="toolbar-icon-btn" title="Chỉ báo kỹ thuật">
              <i className="fa-solid fa-chart-line"></i>
            </button>
            <button className="toolbar-icon-btn" title="Loại nến">
              <i className="fa-solid fa-chart-simple"></i>
            </button>
            <button className="toolbar-icon-btn" title="Cài đặt">
              <i className="fa-solid fa-sliders"></i>
            </button>
            <button className="toolbar-icon-btn" title="Mở rộng">
              <i className="fa-solid fa-circle-dot"></i>
            </button>
          </div>
        </div>

        {/* Các chế độ hiển thị bên phải: Gốc / Trading View / Chi tiết */}
        <div className="toolbar-right">
          <button
            className={`view-mode-btn ${activeViewMode === "goc" ? "active" : ""}`}
            onClick={() => setActiveViewMode("goc")}
          >
            Gốc
          </button>
          <button
            className={`view-mode-btn ${activeViewMode === "tradingview" ? "active" : ""}`}
            onClick={() => setActiveViewMode("tradingview")}
          >
            Trading View
          </button>
          <button
            className={`view-mode-btn ${activeViewMode === "detail" ? "active" : ""}`}
            onClick={() => setActiveViewMode("detail")}
          >
            Chi tiết
          </button>
        </div>
      </div>

      {/* Khung chứa biểu đồ và thanh thông tin đè lên */}
      <div className="chart-wrapper">
        {isChartTitleVisible ? (
          <div className="chart-title-row">
            <div className="icon-box" style={{ cursor: "pointer" }} onClick={() => setIsChartTitleVisible(false)}>
              <i className="fa-solid fa-caret-down"></i>
            </div>
            {hoveredCandle ? (
              <>
                <span>{formatTime(hoveredCandle.time)}</span>

                {/* Tính toán màu sắc chủ đạo của cây nến hiện tại */}
                {(() => {
                  const isCandleUp = hoveredCandle.close >= hoveredCandle.open;
                  const candleColor = isCandleUp ? "#0ecb81" : "#f6465d"; // Xanh Binance / Đỏ Binance

                  const prevCandle = getPreviousCandle(hoveredCandle);
                  const basePrice = prevCandle ? prevCandle.close : hoveredCandle.open;
                  const changePercent = ((hoveredCandle.close - basePrice) / basePrice) * 100;
                  const truncatedPercent = Math.trunc(changePercent * 100) / 100;
                  const isChangeUp = truncatedPercent >= 0;

                  const amplitudePercent = ((hoveredCandle.high - hoveredCandle.low) / hoveredCandle.open) * 100;
                  const truncatedAmplitude = Math.trunc(amplitudePercent * 100) / 100;

                  return (
                    <>
                      {/* Các chỉ số giá trị áp dụng màu động theo xu hướng nến (candleColor) */}
                      <span>Mở <b style={{ color: candleColor }}>{formatPrice(hoveredCandle.open)}</b></span>
                      <span>Trần <b style={{ color: candleColor }}>{formatPrice(hoveredCandle.high)}</b></span>
                      <span>Sàn <b style={{ color: candleColor }}>{formatPrice(hoveredCandle.low)}</b></span>
                      <span>Đóng <b style={{ color: candleColor }}>{formatPrice(hoveredCandle.close)}</b></span>
                      
                      {/* Chỉ số Biến động áp dụng màu theo chiều tăng/giảm của phần trăm biến động */}
                      <span>
                        Biến động{" "}
                        <b style={{ color: isChangeUp ? "#0ecb81" : "#f6465d" }}>
                          {isChangeUp && truncatedPercent > 0 ? "+" : ""}
                          {truncatedPercent.toFixed(2)}%
                        </b>
                      </span>
                      
                      {/* Chỉ số Khung */}
                      <span>
                        Khung{" "}
                        <b style={{ color: candleColor }}>
                          {truncatedAmplitude.toFixed(2)}%
                        </b>
                      </span>
                    </>
                  );
                })()}
              </>
            ) : ""}
          </div>
        ) : (
          <div className="show-chart-title-row" onClick={() => setIsChartTitleVisible(true)}>
            <i className="fa-solid fa-chevron-right"></i>
          </div>
        )}

        {/* Canvas biểu đồ */}
        <div ref={chartContainerRef} className="kline-container" />
      </div>

    </div>
  );
}

export default TradingView;