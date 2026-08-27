package com.example.crypto_trading.service.binance.Ticker;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.example.crypto_trading.dto.binance.ticker.TickerBinanceDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TickerStreamService {

  private final ObjectMapper objectMapper;
  private final SimpMessagingTemplate messagingTemplate;

  /**
   * Parse JSON nhận từ Binance và bắn dữ liệu ra STOMP Topic cho Frontend
   * @param payload JSON raw từ Binance WebSocket
   * @param fallbackSymbol Symbol mặc định nếu stream không chứa tên symbol
   */
  public void publishTicker(String payload, String fallbackSymbol) {
    try {
      TickerBinanceDTO tickerData = objectMapper.readValue(payload, TickerBinanceDTO.class);

      if (tickerData == null) {
        return;
      }

      // Lấy symbol từ data hoặc dùng fallback
      String currentSymbol = fallbackSymbol;
      if (tickerData.getTickerSymbol() != null && !tickerData.getTickerSymbol().isBlank()) {
        currentSymbol = tickerData.getTickerSymbol().toUpperCase();
      }

      if (currentSymbol != null && !currentSymbol.isBlank()) {
        tickerData.setSymbol(currentSymbol.toUpperCase());
        // Gửi ra topic ví dụ: "/topic/ticker/BTCUSDT"
        messagingTemplate.convertAndSend("/topic/ticker/" + currentSymbol.toUpperCase(), tickerData);
      }

    } catch (Exception e) {
      log.error("Cannot parse Binance Ticker message: {}", payload, e);
    }
  }
}
