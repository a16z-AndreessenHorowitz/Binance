package com.example.crypto_trading.dto.binance.orderbook;

import lombok.Data;

@Data
public class OrderBookBinanceStreamDTO {
  private String stream;
  private OrderBookDepthDTO data;
}
