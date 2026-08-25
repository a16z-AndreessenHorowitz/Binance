package com.example.crypto_trading.controller.binance;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.crypto_trading.websocket.OrderBook.OrderBookWebSocketClient;

import lombok.RequiredArgsConstructor;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/binance")
public class TradeController {
  private final OrderBookWebSocketClient orderBookWebSocketClient;

  @GetMapping("/order-book/start")
  public String startOrderBook(@RequestParam String symbol){
    orderBookWebSocketClient.connect(symbol);
    return "Started stream for " + symbol;
  }

}
