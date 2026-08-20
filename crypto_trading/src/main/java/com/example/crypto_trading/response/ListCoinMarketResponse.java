package com.example.crypto_trading.response;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ListCoinMarketResponse {
  private String id;
  private String symbol;
  private String binanceSymbol;
  private String name;
  private String image;
  private Integer marketCapRank;
  private BigDecimal marketCap;
}
