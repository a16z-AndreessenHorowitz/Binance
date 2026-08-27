package com.example.crypto_trading.websocket.Ticker;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.example.crypto_trading.service.binance.Ticker.TickerStreamService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class TickerBinanceWebSocketHandler extends TextWebSocketHandler {

  private final TickerStreamService tickerStreamService;
  private final ApplicationEventPublisher eventPublisher;
  private volatile String currentSymbol = "";

  public void setCurrentSymbol(String symbol) {
    this.currentSymbol = symbol;
  }

  @Override
  public void afterConnectionEstablished(WebSocketSession session) {
    log.info("Connected to Binance Ticker WebSocket: {}", session.getId());
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) {
    tickerStreamService.publishTicker(message.getPayload(), currentSymbol);
  }

  @Override
  public void handleTransportError(WebSocketSession session, Throwable exception) {
    log.error("Binance Ticker WebSocket transport error", exception);
    eventPublisher.publishEvent(new TickerWebSocketDisconnectedEvent(exception.getMessage()));
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
    log.info("Disconnected from Binance Ticker WebSocket: {}", closeStatus);
    eventPublisher.publishEvent(new TickerWebSocketDisconnectedEvent(closeStatus.toString()));
  }
}
