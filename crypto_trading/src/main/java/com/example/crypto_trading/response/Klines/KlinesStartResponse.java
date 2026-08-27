package com.example.crypto_trading.response.Klines;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KlinesStartResponse {
  private String symbol;
  private String interval;
  private List<List<Object>> historicalKlines;
  private String message;
}
