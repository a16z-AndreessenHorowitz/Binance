package com.example.crypto_trading.service.binance.OrderBook;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.example.crypto_trading.dto.binance.orderbook.OrderBookBinanceStreamDTO;
import com.example.crypto_trading.dto.binance.orderbook.OrderBookDepthDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderBookStreamService {

  private final ObjectMapper objectMapper;
  private final SimpMessagingTemplate messagingTemplate;

  /**
   * Parse JSON nhận từ Binance và bắn dữ liệu ra STOMP Topic cho Frontend
   * @param payload JSON raw từ Binance WebSocket
   * @param fallbackSymbol Symbol mặc định nếu stream không chứa tên symbol
   */
  public void publishOrderBook(String payload, String fallbackSymbol) {
    try {
      OrderBookDepthDTO depthData;
      String currentSymbol = fallbackSymbol;

      // Kiểm tra xem payload có dạng bọc stream {"stream": "...", "data": {...}} không
      if (payload.contains("\"stream\"") && payload.contains("\"data\"")) {
        OrderBookBinanceStreamDTO streamResponse = objectMapper.readValue(payload, OrderBookBinanceStreamDTO.class);
        depthData = streamResponse.getData();
        if (streamResponse.getStream() != null) {
          // Lấy symbol từ stream ví dụ: "btcusdt@depth20@100ms" -> "BTCUSDT"
          String raw = streamResponse.getStream().split("@")[0];
          currentSymbol = raw.toUpperCase();
        }
      } else {
        // Dạng payload trực tiếp: {"lastUpdateId": ..., "bids": [...], "asks": [...]}
        depthData = objectMapper.readValue(payload, OrderBookDepthDTO.class);
      }

      if (depthData == null) {
        return;
      }

      if (currentSymbol != null && !currentSymbol.isBlank()) {
        depthData.setSymbol(currentSymbol.toUpperCase());
        // Gửi ra topic ví dụ: "/topic/order-book/BTCUSDT"
        messagingTemplate.convertAndSend("/topic/order-book/" + currentSymbol.toUpperCase(), depthData);
      }

    } catch (Exception e) {
      log.error("Cannot parse Binance OrderBook message: {}", payload, e);
    }
  }
}
