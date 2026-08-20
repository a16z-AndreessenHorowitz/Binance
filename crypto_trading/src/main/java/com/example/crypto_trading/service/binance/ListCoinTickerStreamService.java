package com.example.crypto_trading.service.binance;

import org.springframework.stereotype.Service;

import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.example.crypto_trading.response.ListCoinBinanceStreamResponse;
import com.example.crypto_trading.response.ListCoinBinanceTickerResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Nhận dữ liệu JSON từ Binance, parse thành object, và gửi real-time đến giao diện người dùng (UI) qua WebSocket.
@Service
@RequiredArgsConstructor
@Slf4j
public class ListCoinTickerStreamService {
  //chuyển json thành java object
  private final ObjectMapper objectMapper;
  private final SimpMessagingTemplate messagingTemplate;
  // Gửi dữ liệu WebSocket đến client (UI)



  public void publishTicker(String payload) {
    try {
      //parse JSON thành java object
      ListCoinBinanceStreamResponse response = objectMapper.readValue(payload, ListCoinBinanceStreamResponse.class);
      //lấy dữ liệu từ java object
      ListCoinBinanceTickerResponse ticker = response.getData();

      //kiểm tra xem có dữ liệu không
      if (ticker == null || ticker.getSymbol() == null) {
        return;
      }
      //gửi dữ liệu đến client đang subscribe thông qua websocket c
      messagingTemplate.convertAndSend("/topic/list-coins/ticker", ticker);

      log.info(
          "Symbol: {}, Price: {}, Change: {}%, Volume: {}",
          ticker.getSymbol(),
          ticker.getPrice(),
          ticker.getPriceChangePercent(),
          ticker.getQuoteVolume());
    } catch (Exception e) {
      log.error(
          "Cannot parse Binance message: {}",
          payload,
          e);
    }
  }
}
