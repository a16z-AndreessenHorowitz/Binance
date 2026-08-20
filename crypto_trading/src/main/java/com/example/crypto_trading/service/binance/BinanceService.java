package com.example.crypto_trading.service.binance;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.example.crypto_trading.dto.binance.BinanceTickerDTO;

@Service
public class BinanceService {
  private final RestClient restClient;
  private volatile Set<String> tradableSymbols;

  public BinanceService(@Qualifier("binanceClient") RestClient restClient) {
    this.restClient = restClient;
  }

  public BinanceTickerDTO getTicker(String symbol){
    return restClient.get()
      .uri("/api/v3/ticker/24hr?symbol=" + symbol)
      .retrieve()
      .body(BinanceTickerDTO.class);
  }

  public boolean isTradableSymbol(String symbol) {
    return getTradableSymbols().contains(symbol);
  }

  private Set<String> getTradableSymbols() {
    if (tradableSymbols != null) {
      return tradableSymbols;
    }

    List<BinanceTickerDTO> response = restClient.get()
        .uri("/api/v3/ticker/24hr")
        .retrieve()
        .body(new ParameterizedTypeReference<List<BinanceTickerDTO>>() {
        });

    if (response == null) {
      return Collections.emptySet();
    }

    Set<String> symbols = ConcurrentHashMap.newKeySet();
    response.forEach(ticker -> {
      if (ticker.getSymbol() != null) {
        symbols.add(ticker.getSymbol());
      }
    });

    tradableSymbols = symbols;
    return tradableSymbols;
  }
}
