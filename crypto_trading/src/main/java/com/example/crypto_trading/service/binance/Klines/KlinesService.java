package com.example.crypto_trading.service.binance.Klines;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.example.crypto_trading.websocket.Klines.KlinesWebSocketClient;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class KlinesService {

  private final KlinesWebSocketClient klinesWebSocketClient;
  private final RestClient restClient;

  public KlinesService(
      KlinesWebSocketClient klinesWebSocketClient,
      @Qualifier("binanceClient") RestClient restClient) {
    this.klinesWebSocketClient = klinesWebSocketClient;
    this.restClient = restClient;
  }

  /**
   * Lấy lịch sử klines từ Binance REST API
   */
  public List<List<Object>> getKlines(String symbol, String interval, Integer limit) {
    int safeLimit = limit != null ? limit : 500;
    
    List<List<Object>> response = restClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/api/v3/klines")
            .queryParam("symbol", symbol.toUpperCase())
            .queryParam("interval", interval)
            .queryParam("limit", safeLimit)
            .build())
        .retrieve()
        .body(new ParameterizedTypeReference<List<List<Object>>>() {
        });

    log.info("Retrieved {} klines for {}@{}", response != null ? response.size() : 0, symbol, interval);
    return response;
  }

  /**
   * Bắt đầu klines stream cho symbol và interval
   */
  public String startKlines(String symbol, String interval) {
    log.info("KlinesService.startKlines called with symbol={}, interval={}", symbol, interval);
    klinesWebSocketClient.connect(symbol, interval);
    return "Started klines stream for " + symbol + " with interval " + interval;
  }
}
