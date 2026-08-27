package com.example.crypto_trading.dto.binance.ticker;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TickerBinanceDTO {
  private String symbol;

  @JsonAlias("e")
  private String eventType;        // "24hrTicker"

  @JsonAlias("s")
  private String tickerSymbol;     // "BTCUSDT"

  @JsonAlias("p")
  private String priceChange;      // Price change

  @JsonAlias("P")
  private String priceChangePercent; // Price change percent

  @JsonAlias("w")
  private String weightedAvgPrice; // Weighted average price

  @JsonAlias("c")
  private String lastPrice;        // Last price

  @JsonAlias("Q")
  private String lastQty;          // Last quantity

  @JsonAlias("o")
  private String openPrice;        // Open price

  @JsonAlias("h")
  private String highPrice;        // High price

  @JsonAlias("l")
  private String lowPrice;         // Low price

  @JsonAlias("v")
  private String volume;           // Total traded base asset volume

  @JsonAlias("q")
  private String quoteVolume;      // Total traded quote asset volume

  @JsonAlias("O")
  private Long openTime;           // Statistics open time

  @JsonAlias("C")
  private Long closeTime;          // Statistics close time
}
