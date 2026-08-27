package com.example.crypto_trading.websocket.Klines;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.example.crypto_trading.service.binance.Klines.KlinesStreamService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class KlinesBinanceWebSocketHandler extends TextWebSocketHandler {

  private final KlinesStreamService klinesStreamService;
  private final ApplicationEventPublisher eventPublisher;
  private volatile String currentSymbol = "";
  private volatile String currentInterval = "";

  public void setCurrentSymbol(String symbol) {
    this.currentSymbol = symbol;
  }

  public void setCurrentInterval(String interval) {
    this.currentInterval = interval;
  }

  @Override
  public void afterConnectionEstablished(WebSocketSession session) {
    log.info("Connected to Binance Klines WebSocket: {}", session.getId());
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) {
    klinesStreamService.publishKline(message.getPayload(), currentSymbol, currentInterval);
  }

  @Override
  public void handleTransportError(WebSocketSession session, Throwable exception) {
    log.error("Binance Klines WebSocket transport error", exception);
    eventPublisher.publishEvent(new KlinesWebSocketDisconnectedEvent(exception.getMessage()));
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
    log.info("Disconnected from Binance Klines WebSocket: {}", closeStatus);
    eventPublisher.publishEvent(new KlinesWebSocketDisconnectedEvent(closeStatus.toString()));
  }
}
