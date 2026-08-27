package com.example.crypto_trading.dto.binance.ListCoin;

import lombok.Data;

@Data
public class ListCoinBinanceStreamDTO {
  private String stream;
  private ListCoinBinanceWsTickerDTO data;
}
