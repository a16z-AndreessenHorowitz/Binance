package com.example.crypto_trading.service.binance.ListCoin;

import org.springframework.stereotype.Service;

import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.example.crypto_trading.dto.binance.ListCoin.ListCoinBinanceStreamDTO;
import com.example.crypto_trading.dto.binance.ListCoin.ListCoinBinanceWsTickerDTO;
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
      ListCoinBinanceStreamDTO response = objectMapper.readValue(payload, ListCoinBinanceStreamDTO.class);
      //lấy dữ liệu từ java object
      ListCoinBinanceWsTickerDTO ticker = response.getData();

      //kiểm tra xem có dữ liệu không
      if (ticker == null || ticker.getSymbol() == null) {
        return;
      }
      //gửi dữ liệu đến client đang subscribe thông qua websocket c
      messagingTemplate.convertAndSend("/topic/list-coins/ticker", ticker);

    } catch (Exception e) {
      log.error(
          "Cannot parse Binance message: {}",
          payload,
          e);
    }
  }
}
