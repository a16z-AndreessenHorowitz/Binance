package com.example.crypto_trading.response.OrderBook;

import lombok.Data;

@Data
public class OrderBookBinanceStreamResponse {
  private String stream;
  private OrderBookDepthResponse data;
}
