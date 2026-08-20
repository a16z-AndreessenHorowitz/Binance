package com.example.crypto_trading.controller.binance;

import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



import com.example.crypto_trading.response.ListCoinMarketResponse;
import com.example.crypto_trading.service.binance.BinanceService;

import com.example.crypto_trading.service.binance.ListCoinService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/binance")
public class BinanceController {

  private final BinanceService binanceService;

  private final ListCoinService listCoinService;



  @GetMapping("/list-coins")
  public List<ListCoinMarketResponse> getListCoins(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "30") int limit
  ) {
    return listCoinService.getCoins(page, limit);
  }
}
