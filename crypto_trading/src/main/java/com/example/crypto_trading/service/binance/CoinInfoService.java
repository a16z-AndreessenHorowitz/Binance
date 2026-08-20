package com.example.crypto_trading.service.binance;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.example.crypto_trading.dto.binance.CoinInfoDTO;
import com.example.crypto_trading.response.CoinGeckoSearchResponse;

@Service
public class CoinInfoService {
  private final RestClient restClient;
  private final Map<String, CoinInfoDTO> coinInfoCache = new ConcurrentHashMap<>();

  public CoinInfoService(@Qualifier("coinGeckoClient") RestClient restClient) {
    this.restClient = restClient;
  }

  public CoinInfoDTO getCoinInfo(String symbol){
    String normalizedSymbol = symbol.toUpperCase();
    return coinInfoCache.computeIfAbsent(normalizedSymbol, this::fetchCoinInfo);
  }

  private CoinInfoDTO fetchCoinInfo(String symbol) {
    // BTCUSDT → BTC
    String coinSymbol=symbol.replace("USDT", "");
    // Gọi CoinGecko search
    CoinGeckoSearchResponse response=restClient.get()
    .uri("/search?query=" + coinSymbol)
    .retrieve()
    .body(CoinGeckoSearchResponse.class);

    return response.getCoins()
          .stream()
          .filter(coin ->
            coin.getSymbol().equalsIgnoreCase(coinSymbol)
          )
          .findFirst()
          .orElseThrow(() ->
            new RuntimeException("Coin not found: " + coinSymbol)
          );
  }
}
