package com.example.crypto_trading.websocket.ListCoin;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.example.crypto_trading.service.binance.ListCoin.ListCoinTickerStreamService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

//nhận dữ liệu từ file BinanceWebSocketClient và chuyển tiếp dữ liệu thông qua ListCoinTickerStreamService để xử lý 
@Component
@Slf4j
@RequiredArgsConstructor
public class ListCoinBinanceWebSocketHandler extends TextWebSocketHandler {
  private final ListCoinTickerStreamService tickerStreamService;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  public void afterConnectionEstablished(
      WebSocketSession session) {
    log.info(
        "Connected to Binance list coin WebSocket: {}",
        session.getId());
  }

  // nhận dữ liệu từ binance quan trọng nó chạy mỗi khi binance gửi dữ liệu mới
  @Override
  protected void handleTextMessage(
      WebSocketSession session,
      TextMessage message) {
    tickerStreamService.publishTicker(message.getPayload());
  }

  // lỗi kết nối
  @Override
  public void handleTransportError(
      WebSocketSession session,
      Throwable exception) {
    log.error(
        "Binance list coin WebSocket transport error",
        exception);

    eventPublisher.publishEvent(
        new ListCoinBinanceWebSocketDisconnectedEvent(
            exception.getMessage()));
  }

  // ngắt kết nối
  @Override
  public void afterConnectionClosed(
      WebSocketSession session,
      CloseStatus closeStatus) {

    log.info(
        "Disconnected from Binance list coin WebSocket: {}",
        closeStatus);

    eventPublisher.publishEvent(
        new ListCoinBinanceWebSocketDisconnectedEvent(
            closeStatus.toString()));
  }

}
