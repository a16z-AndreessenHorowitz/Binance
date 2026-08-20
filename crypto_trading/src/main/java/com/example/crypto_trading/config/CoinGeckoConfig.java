package com.example.crypto_trading.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class CoinGeckoConfig {

  @Bean
  public RestClient coinGeckoClient() {
    return RestClient.builder()
        .baseUrl("https://api.coingecko.com/api/v3")
        .build();
  }
}
