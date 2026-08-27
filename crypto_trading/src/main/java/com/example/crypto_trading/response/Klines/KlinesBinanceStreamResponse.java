package com.example.crypto_trading.response.Klines;

import lombok.Data;

@Data
public class KlinesBinanceStreamResponse {
  private String stream;
  private KlineResponse data;
}
