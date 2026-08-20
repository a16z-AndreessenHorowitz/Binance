package com.example.crypto_trading.service.binance;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.example.crypto_trading.response.ListCoinGeckoMarketResponse;
import com.example.crypto_trading.response.ListCoinMarketResponse;
import com.example.crypto_trading.websocket.BinanceWebSocketClient;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListCoinService {
  private static final int DEFAULT_LIMIT = 30;
  private static final int MAX_LIMIT = 100;

  private final ListCoinGeckoClient listCoinGeckoClient;
  private final BinanceWebSocketClient binanceWebSocketClient;
  private final BinanceService binanceService;

  public List<ListCoinMarketResponse> getCoins(int page, Integer limit) {
    int safePage = Math.max(page, 1);
    int safeLimit = normalizeLimit(limit);
    //Gọi API của listCoinGeckoClient để lấy danh sách coin
    List<ListCoinGeckoMarketResponse> coins = listCoinGeckoClient.getMarketCoins(safePage, safeLimit);
    //chuyển đổi dữ liệu sang response của ứng dụng

    // Lấy danh sách Binance symbol từ các coin
    List<ListCoinMarketResponse> response = coins.stream()
        .map(this::toMarketResponse)
        .toList();
        
    //lọc ra các symbol của binance để kết nối websocket
    List<String> binanceSymbols = response.stream()
        .map(ListCoinMarketResponse::getBinanceSymbol)
        .filter(Objects::nonNull)
        .toList();

    // Kết nối WebSocket để nhận dữ liệu real-time
    binanceWebSocketClient.connect(binanceSymbols);
    
    return response;
  }

  //xử lý limit
  private int normalizeLimit(Integer limit) {
    if (limit == null) {
      return DEFAULT_LIMIT; //30
    }
    return Math.max(1, Math.min(limit, MAX_LIMIT));
  }

  //toMarketResponse() - Chuyển đổi dữ liệu
  private ListCoinMarketResponse toMarketResponse(ListCoinGeckoMarketResponse coin) {
    String symbol = coin.getSymbol().toUpperCase();
    String binanceSymbol = toBinanceSymbol(symbol);

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

  //toBinanceSymbol() - Tạo symbol Binance
  private String toBinanceSymbol(String symbol) {
    // Nếu là USDT thì không có symbol
    if ("USDT".equals(symbol)) {
      return null;
    }

    String binanceSymbol = symbol + "USDT";
    return binanceService.isTradableSymbol(binanceSymbol) ? binanceSymbol : null;
  }
}
