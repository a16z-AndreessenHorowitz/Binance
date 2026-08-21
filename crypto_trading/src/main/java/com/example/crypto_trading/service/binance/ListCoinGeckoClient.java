package com.example.crypto_trading.service.binance;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.example.crypto_trading.response.ListCoinGeckoMarketResponse;

// Gọi API CoinGecko để lấy danh sách coin theo thị trường (market cap)
@Service
public class ListCoinGeckoClient {
  private final RestClient restClient;

  public ListCoinGeckoClient(@Qualifier("coinGeckoClient") RestClient restClient) {
    this.restClient = restClient;
  }

  // Cache kết quả 5 phút — tránh gọi lại CoinGecko liên tục và bị rate limit (429)
  @Cacheable(value = "coinGeckoMarkets", key = "#page + '-' + #limit")
  public List<ListCoinGeckoMarketResponse> getMarketCoins(int page, int limit) {
    List<ListCoinGeckoMarketResponse> coins = restClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/coins/markets")
            .queryParam("vs_currency", "usd")
            .queryParam("order", "market_cap_desc")
            .queryParam("per_page", limit)
            .queryParam("page", page)
            .queryParam("sparkline", false)
            .build())
        .retrieve()
        .body(new ParameterizedTypeReference<List<ListCoinGeckoMarketResponse>>() {
        });
        // Type Erasure: xoá kiểu
        // Lúc viết code:
        // List<ListCoinGeckoMarketResponse> coins;
        // Lúc runtime (sau Type Erasure):
        // List coins;  // Java quên mất kiểu bên trong là gì!
        // ParameterizedTypeReference giữ lại thông tin generic

    return coins == null ? List.of() : coins;
  }
}
