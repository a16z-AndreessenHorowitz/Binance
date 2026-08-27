package com.example.crypto_trading.dto.binance.ListCoin;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ListCoinBinanceWsTickerDTO {
  // "s": "BTCUSDT"
  @JsonAlias("s")
  private String symbol;

  // "c": "67000.12"
  @JsonAlias("c")
  private BigDecimal price;

  // "P": "2.35"
  @JsonAlias("P")
  private BigDecimal priceChangePercent;

  // "q": "123456789.12"
  @JsonAlias("q")
  private BigDecimal quoteVolume;
}
