package com.example.crypto_trading.dto.binance.orderbook;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderBookDepthDTO {
  private String symbol;
  private Long lastUpdateId;
  private List<List<String>> bids; // Danh sách lệnh mua [[price, quantity], ...]
  private List<List<String>> asks; // Danh sách lệnh bán [[price, quantity], ...]
}
