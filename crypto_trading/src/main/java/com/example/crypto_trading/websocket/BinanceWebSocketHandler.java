package com.example.crypto_trading.websocket;

import com.example.crypto_trading.service.binance.BinanceTickerStreamService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@Slf4j
@RequiredArgsConstructor
public class BinanceWebSocketHandler extends TextWebSocketHandler {
  // private static final Logger log = LoggerFactory.getLogger(BinanceWebSocketHandler.class); //cái này thay thế annotation có nhiệm vụ tạo ra log , thêm anno slj4 cho nhanh

  private final BinanceTickerStreamService tickerStreamService;
  private final ApplicationEventPublisher eventPublisher;


  @Override
  public void afterConnectionEstablished(WebSocketSession session) {
    log.info("Connected to Binance WebSocket");
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) {
    tickerStreamService.publishTicker(message.getPayload());
  }

  @Override
  public void handleTransportError(WebSocketSession session, Throwable exception) {
    log.error("Binance WebSocket transport error", exception);
    eventPublisher.publishEvent(new BinanceWebSocketDisconnectedEvent(exception.getMessage()));
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
    log.info("Disconnected from Binance WebSocket: {}", closeStatus);
    eventPublisher.publishEvent(new BinanceWebSocketDisconnectedEvent(closeStatus.toString()));
  }
}
