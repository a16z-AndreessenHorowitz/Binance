package com.example.crypto_trading.websocket.Ticker;

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
public class TickerWebSocketClient {
  private final WebSocketClient webSocketClient;
  private final TickerBinanceWebSocketHandler tickerHandler;
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
    tickerHandler.setCurrentSymbol(normalizedSymbol.toUpperCase());

    // Stream 24hr ticker của Binance
    String url = "wss://stream.binance.com/ws/" + normalizedSymbol + "@ticker";
    log.info("Connecting to Binance Ticker: {}", url);

    webSocketClient.execute(tickerHandler, new WebSocketHttpHeaders(), URI.create(url))
        .whenComplete((session, exception) -> {
          connecting.set(false);

          if (exception != null) {
            log.error("Cannot connect to Binance Ticker WebSocket for {}", normalizedSymbol, exception);
            scheduleReconnect();
            return;
          }

          currentSession = session;
          log.info("Binance Ticker WebSocket connected for {}", normalizedSymbol);

          // Nếu symbol đã đổi trong lúc đang kết nối
          if (!normalizedSymbol.equals(lastConnectedSymbol)) {
            connect(lastConnectedSymbol);
          }
        });
  }

  @EventListener
  public void reconnect(TickerWebSocketDisconnectedEvent event) {
    log.info("Scheduling Binance Ticker WebSocket reconnect: {}", event.reason());
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
        log.warn("Error closing Ticker session", e);
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
