package com.example.crypto_trading.dto.binance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ListCoinBinanceTickerDTO {
  private String symbol;
  private String priceChange;
  private String priceChangePercent;
  private String lastPrice;
  private String highPrice;
  private String lowPrice;
  private String volume;
  private String quoteVolume;
}
