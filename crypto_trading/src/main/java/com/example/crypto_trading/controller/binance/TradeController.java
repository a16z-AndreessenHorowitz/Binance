package com.example.crypto_trading.controller.binance;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.crypto_trading.response.Klines.KlinesStartResponse;
import com.example.crypto_trading.response.Ticker.TickerStartResponse;
import com.example.crypto_trading.service.binance.Klines.KlinesService;
import com.example.crypto_trading.service.binance.Ticker.TickerService;
import com.example.crypto_trading.websocket.OrderBook.OrderBookWebSocketClient;

import lombok.RequiredArgsConstructor;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/binance")
public class TradeController {
  
  private final OrderBookWebSocketClient orderBookWebSocketClient;
  private final TickerService tickerService;
  private final KlinesService klinesService;

  @GetMapping("/order-book/start")
  public String startOrderBook(@RequestParam String symbol){
    orderBookWebSocketClient.connect(symbol);
    return "Started stream for " + symbol;
  }

  @GetMapping("/ticker/start")
  public TickerStartResponse startTicker(@RequestParam String symbol){
    return tickerService.startTicker(symbol);
  }

  @GetMapping("/klines/start")
  public KlinesStartResponse startKlines(
      @RequestParam String symbol,
      @RequestParam String interval,
      @RequestParam(defaultValue = "500") Integer limit){
    
    // 1. Lấy lịch sử klines
    List<List<Object>> historicalKlines = klinesService.getKlines(symbol, interval, limit);
    
    // 2. Kích hoạt stream
    klinesService.startKlines(symbol, interval);
    
    // 3. Trả về cả lịch sử
    return KlinesStartResponse.builder()
        .symbol(symbol.toUpperCase())
        .interval(interval)
        .historicalKlines(historicalKlines)
        .message("Started klines stream for " + symbol + " with interval " + interval)
        .build();
  }

}

