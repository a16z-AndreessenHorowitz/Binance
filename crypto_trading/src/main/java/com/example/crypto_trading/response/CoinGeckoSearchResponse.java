package com.example.crypto_trading.response;

import java.util.List;

import com.example.crypto_trading.dto.binance.CoinInfoDTO;

import lombok.Data;

// json có dạng 
// {
//   "coins": [
//     {...},
//     {...}
//   ]
// } cho nên  private List<CoinGeckoCoinDTO> coins;
@Data
public class CoinGeckoSearchResponse {
  private List<CoinInfoDTO> coins;
}
