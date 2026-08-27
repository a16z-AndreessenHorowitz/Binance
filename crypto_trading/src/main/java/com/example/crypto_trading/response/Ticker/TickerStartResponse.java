package com.example.crypto_trading.response.Ticker;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TickerStartResponse {
  private String symbol;
  private String coinName;
  private String imageUrl;
  private String message;
}
