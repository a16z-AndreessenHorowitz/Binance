package com.example.crypto_trading.service.binance.ListCoin;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.example.crypto_trading.dto.binance.ListCoin.ListCoinBinanceTickerDTO;
import com.example.crypto_trading.dto.coingecko.ListCoinGeckoMarketDTO;
import com.example.crypto_trading.response.ListCoin.ListCoinMarketResponse;
import com.example.crypto_trading.websocket.ListCoin.ListCoinBinanceWebSocketClient;

@Service
public class ListCoinService {
  private static final int DEFAULT_LIMIT = 30;
  private static final int MAX_LIMIT = 100;

  private final ListCoinGeckoClient listCoinGeckoClient;
  private final ListCoinBinanceWebSocketClient binanceWebSocketClient;
  private final RestClient restClient;

  // Cache danh sách symbol có thể giao dịch từ Binance
  private volatile Set<String> tradableSymbols;

  public ListCoinService(
      ListCoinGeckoClient listCoinGeckoClient,
      ListCoinBinanceWebSocketClient binanceWebSocketClient,
      @Qualifier("binanceClient") RestClient restClient) {
    this.listCoinGeckoClient = listCoinGeckoClient;
    this.binanceWebSocketClient = binanceWebSocketClient;
    this.restClient = restClient;
  }

  public List<ListCoinMarketResponse> getCoins(int page, Integer limit) {
    int safePage = Math.max(page, 1);
    int safeLimit = normalizeLimit(limit);

    // Gọi API của listCoinGeckoClient để lấy danh sách coin
    List<ListCoinGeckoMarketDTO> coins = listCoinGeckoClient.getMarketCoins(safePage, safeLimit);

    // Chuyển đổi dữ liệu sang response của ứng dụng
    List<ListCoinMarketResponse> response = coins.stream()
        .map(this::toMarketResponse)
        .toList();

    // Lọc ra các symbol của Binance để kết nối WebSocket
    List<String> binanceSymbols = response.stream()
        .map(ListCoinMarketResponse::getBinanceSymbol)
        .filter(Objects::nonNull)
        .toList();

    // Kết nối WebSocket để nhận dữ liệu real-time
    binanceWebSocketClient.connect(binanceSymbols);

    return response;
  }

  // Xử lý limit
  private int normalizeLimit(Integer limit) {
    if (limit == null) {
      return DEFAULT_LIMIT; // 30
    }
    return Math.max(1, Math.min(limit, MAX_LIMIT));
  }

  // Chuyển đổi dữ liệu CoinGecko → response của ứng dụng
  private ListCoinMarketResponse toMarketResponse(ListCoinGeckoMarketDTO coin) {
    String rawSymbol = coin.getSymbol();
    String symbol = rawSymbol != null ? rawSymbol.toUpperCase() : "";
    String binanceSymbol = symbol.isEmpty() ? null : toBinanceSymbol(symbol);

    return ListCoinMarketResponse.builder()
        .id(coin.getId())
        .symbol(symbol)
        .binanceSymbol(binanceSymbol)
        .name(coin.getName())
        .image(coin.getImage())
        .marketCapRank(coin.getMarketCapRank())
        .marketCap(coin.getMarketCap())
        .build();
  }

  // Tạo Binance symbol (e.g. BTC → BTCUSDT), trả về null nếu không giao dịch được
  private String toBinanceSymbol(String symbol) {
    if ("USDT".equals(symbol)) {
      return null;
    }
    String binanceSymbol = symbol + "USDT";
    return isTradableSymbol(binanceSymbol) ? binanceSymbol : null;
  }

  // Kiểm tra symbol có đang được giao dịch trên Binance không
  private boolean isTradableSymbol(String symbol) {
    return getTradableSymbols().contains(symbol);
  }

  // Lấy danh sách symbol từ Binance API (lazy load + cache)
  private Set<String> getTradableSymbols() {
    if (tradableSymbols != null) {
      return tradableSymbols;
    }

    List<ListCoinBinanceTickerDTO> response = restClient.get()
        .uri("/api/v3/ticker/24hr")
        .retrieve()
        .body(new ParameterizedTypeReference<List<ListCoinBinanceTickerDTO>>() {
        });

    if (response == null) {
      return Collections.emptySet();
    }

    // ConcurrentHashMap.newKeySet() — thread-safe khi nhiều request đến cùng lúc
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
