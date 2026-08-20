package com.example.crypto_trading.dto.binance;

import lombok.Data;

@Data
public class CoinInfoDTO {
  private String id;
    private String name;
    private String api_symbol;
    private String symbol;
    private Integer market_cap_rank;
    private String thumb;
    private String large;
}
