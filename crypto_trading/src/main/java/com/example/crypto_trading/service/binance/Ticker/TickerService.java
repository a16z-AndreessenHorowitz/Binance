package com.example.crypto_trading.service.binance.Ticker;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.example.crypto_trading.response.Ticker.TickerStartResponse;
import com.example.crypto_trading.websocket.Ticker.TickerWebSocketClient;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TickerService {

  private final TickerWebSocketClient tickerWebSocketClient;
  private final RestClient restClient;

  public TickerService(
      TickerWebSocketClient tickerWebSocketClient,
      @Qualifier("coinGeckoClient") RestClient restClient) {
    this.tickerWebSocketClient = tickerWebSocketClient;
    this.restClient = restClient;
  }

  /**
   * Bắt đầu ticker stream cho symbol và trả về thông tin coin (bao gồm ảnh từ CoinGecko)
   */
  public TickerStartResponse startTicker(String symbol) {
    // 1. Kết nối WebSocket Binance cho ticker
    tickerWebSocketClient.connect(symbol);

    // 2. Lấy ảnh coin từ CoinGecko
    String cleanSymbol = symbol.replace("USDT", "").toLowerCase();
    String imageUrl = getCoinImage(cleanSymbol);

    return TickerStartResponse.builder()
        .symbol(symbol.toUpperCase())
        .coinName(cleanSymbol)
        .imageUrl(imageUrl)
        .message("Started ticker stream for " + symbol)
        .build();
  }

  /**
   * Gọi CoinGecko /search?query={symbol} để lấy ảnh coin
   * Chỉ 1 API call, trả về luôn ảnh
   * Cache kết quả để tránh rate limit
   */
  @Cacheable(value = "coinImage", key = "#coinSymbol")
  public String getCoinImage(String coinSymbol) {
    try {
      CoinSearchResponse searchResult = restClient.get()
          .uri(uriBuilder -> uriBuilder
              .path("/search")
              .queryParam("query", coinSymbol)
              .build())
          .retrieve()
          .body(CoinSearchResponse.class);

      if (searchResult != null && searchResult.getCoins() != null) {
        // Tìm coin khớp chính xác symbol
        for (CoinSearchItem coin : searchResult.getCoins()) {
          if (coin.getSymbol() != null && coin.getSymbol().equalsIgnoreCase(coinSymbol)) {
            return coin.getLarge(); // Ảnh lớn
          }
        }
      }
    } catch (Exception e) {
      log.warn("Cannot get coin image from CoinGecko for: {}", coinSymbol, e);
    }
    return null;
  }

  // --- DTO cho CoinGecko /search response ---

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  private static class CoinSearchResponse {
    private List<CoinSearchItem> coins;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  private static class CoinSearchItem {
    private String id;     // "bitcoin"
    private String symbol; // "BTC"
    private String name;   // "Bitcoin"
    private String thumb;  // Ảnh nhỏ
    private String large;  // Ảnh lớn
  }
}
