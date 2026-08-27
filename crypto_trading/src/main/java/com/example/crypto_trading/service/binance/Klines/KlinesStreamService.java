package com.example.crypto_trading.service.binance.Klines;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.example.crypto_trading.dto.binance.klines.KlineBinanceDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class KlinesStreamService {

  private final ObjectMapper objectMapper;
  private final SimpMessagingTemplate messagingTemplate;

  /**
   * Parse JSON nhận từ Binance và bắn dữ liệu ra STOMP Topic cho Frontend
   * @param payload JSON raw từ Binance WebSocket
   * @param fallbackSymbol Symbol mặc định nếu stream không chứa tên symbol
   * @param fallbackInterval Interval mặc định nếu stream không chứa interval
   */
  public void publishKline(String payload, String fallbackSymbol, String fallbackInterval) {
    try {
      log.info("Raw Binance Klines payload: {}", payload);
      
      JsonNode rootNode = objectMapper.readTree(payload);
      if (rootNode == null || rootNode.isNull()) {
        return;
      }

      String currentSymbol = fallbackSymbol;
      String currentInterval = fallbackInterval;

      // Kiểm tra stream nếu là combined stream {"stream": "ethusdt@kline_15m", "data": {...}}
      if (rootNode.has("stream")) {
        String stream = rootNode.get("stream").asText();
        String[] parts = stream.split("@");
        if (parts.length >= 2) {
          currentSymbol = parts[0].toUpperCase();
          String[] klineParts = parts[1].split("_");
          if (klineParts.length >= 2) {
            currentInterval = klineParts[1];
          }
        }
      }

      // Lấy node kline 'k' nằm trong root hoặc trong data.k
      JsonNode klineNode;
      if (rootNode.has("data") && rootNode.get("data").has("k")) {
        klineNode = rootNode.get("data").get("k");
      } else if (rootNode.has("k")) {
        klineNode = rootNode.get("k");
      } else {
        klineNode = rootNode;
      }

      KlineBinanceDTO klineData = objectMapper.treeToValue(klineNode, KlineBinanceDTO.class);
      if (klineData == null) {
        return;
      }

      // Cập nhật symbol/interval nếu có trong kline data hoặc fallback
      if (klineData.getSymbol() != null && !klineData.getSymbol().isBlank()) {
        currentSymbol = klineData.getSymbol();
      }
      if (klineData.getInterval() != null && !klineData.getInterval().isBlank()) {
        currentInterval = klineData.getInterval();
      }

      if (currentSymbol != null && !currentSymbol.isBlank()) {
        klineData.setSymbol(currentSymbol.toUpperCase());
      }
      if (currentInterval != null && !currentInterval.isBlank()) {
        klineData.setInterval(currentInterval);
      }

      log.info("Parsed kline data: symbol={}, interval={}, openPrice={}, closePrice={}, volume={}", 
          klineData.getSymbol(), klineData.getInterval(), klineData.getOpenPrice(), 
          klineData.getClosePrice(), klineData.getVolume());

      if (currentSymbol != null && !currentSymbol.isBlank() && currentInterval != null && !currentInterval.isBlank()) {
        // Gửi ra topic ví dụ: "/topic/klines/BTCUSDT/15m"
        messagingTemplate.convertAndSend("/topic/klines/" + currentSymbol.toUpperCase() + "/" + currentInterval, klineData);
      }

    } catch (Exception e) {
      log.error("Cannot parse Binance Klines message: {}", payload, e);
    }
  }
}
