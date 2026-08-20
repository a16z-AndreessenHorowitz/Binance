package com.example.crypto_trading.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

  @Bean
  public RestClient binanceClient(){
    return RestClient.builder()
    .baseUrl("https://api.binance.com").build();
  }
}
