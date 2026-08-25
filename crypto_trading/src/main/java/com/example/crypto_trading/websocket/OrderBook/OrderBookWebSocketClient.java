package com.example.crypto_trading.websocket.OrderBook;

import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderBookWebSocketClient {

  private final WebSocketClient webSocketClient;
  private final OrderBookBinanceWebSocketHandler orderBookHandler;
  private final ScheduledExecutorService reconnectExecutor = Executors.newSingleThreadScheduledExecutor();
  private final AtomicBoolean connecting = new AtomicBoolean(false);
  private final AtomicBoolean reconnectScheduled = new AtomicBoolean(false);
  
  private volatile WebSocketSession currentSession;
  private volatile String currentSymbol = "";
  private volatile String lastConnectedSymbol = "";

  public synchronized void connect(String symbol) {
    if (symbol == null || symbol.isBlank()) {
      lastConnectedSymbol = "";
      closeSession();
      return;
    }

    String normalizedSymbol = symbol.trim().toLowerCase();

    // Nếu đã kết nối với symbol này rồi thì không cần kết nối lại
    if (normalizedSymbol.equals(currentSymbol) && currentSession != null && currentSession.isOpen()) {
      return;
    }

    lastConnectedSymbol = normalizedSymbol;
    closeSession();

    if (!connecting.compareAndSet(false, true)) {
      return;
    }

    currentSymbol = normalizedSymbol;
    orderBookHandler.setCurrentSymbol(normalizedSymbol.toUpperCase());

    // Stream depth20 @ 100ms của Binance
    String url = "wss://stream.binance.com:9443/ws/" + normalizedSymbol + "@depth20@1000ms";
    log.info("Connecting to Binance OrderBook: {}", url);

    webSocketClient.execute(orderBookHandler, new WebSocketHttpHeaders(), URI.create(url))
        .whenComplete((session, exception) -> {
          connecting.set(false);

          if (exception != null) {
            log.error("Cannot connect to Binance OrderBook WebSocket for {}", normalizedSymbol, exception);
            scheduleReconnect();
            return;
          }

          currentSession = session;
          log.info("Binance OrderBook WebSocket connected for {}", normalizedSymbol);

          // Nếu symbol đã đổi trong lúc đang kết nối
          if (!normalizedSymbol.equals(lastConnectedSymbol)) {
            connect(lastConnectedSymbol);
          }
        });
  }

  @EventListener
  public void reconnect(OrderBookWebSocketDisconnectedEvent event) {
    log.info("Scheduling Binance OrderBook WebSocket reconnect: {}", event.reason());
    scheduleReconnect();
  }

  private void scheduleReconnect() {
    if (lastConnectedSymbol.isBlank() || !reconnectScheduled.compareAndSet(false, true)) {
      return;
    }

    reconnectExecutor.schedule(() -> {
      reconnectScheduled.set(false);
      if (!lastConnectedSymbol.isBlank()) {
        connect(lastConnectedSymbol);
      }
    }, 5, TimeUnit.SECONDS);
  }

  public synchronized void closeSession() {
    if (currentSession != null && currentSession.isOpen()) {
      try {
        currentSession.close();
      } catch (Exception e) {
        log.warn("Error closing OrderBook session", e);
      }
    }
    currentSession = null;
    currentSymbol = "";
  }

  @PreDestroy
  public void shutdown() {
    closeSession();
    reconnectExecutor.shutdownNow();
  }
}
