package com.example.crypto_trading.response;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ListCoinGeckoMarketResponse {
  private String id;
  private String symbol;

  private String name;

  private String image;

  @JsonProperty("market_cap_rank")
  private Integer marketCapRank;

  @JsonProperty("market_cap")
  private BigDecimal marketCap;
}
