package com.example.crypto_trading.response;

import lombok.Data;

@Data
public class ListCoinBinanceStreamResponse {
  private String stream;

  private ListCoinBinanceTickerResponse data;
}
