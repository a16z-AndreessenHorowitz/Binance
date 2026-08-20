package com.example.crypto_trading.service.binance;

import com.example.crypto_trading.dto.binance.BinanceTickerDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BinanceTickerStreamService {
  private static final Logger log = LoggerFactory.getLogger(BinanceTickerStreamService.class);

  private final ObjectMapper objectMapper;
  private final SimpMessagingTemplate messagingTemplate;


  public void publishTicker(String payload) {
    try {
      BinanceTickerDTO ticker = objectMapper.readValue(payload, BinanceTickerDTO.class);

      messagingTemplate.convertAndSend("/topic/ticker/" + ticker.getSymbol(), ticker);
    } catch (JsonProcessingException exception) {
      log.error("Cannot parse Binance ticker message", exception);
    }
  }
}
