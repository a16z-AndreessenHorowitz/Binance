package com.example.crypto_trading.dto.binance.klines;

import lombok.Data;

@Data
public class KlinesBinanceStreamDTO {
  private String stream;
  private KlineBinanceDTO data;
}
