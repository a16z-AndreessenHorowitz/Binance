package com.example.crypto_trading.websocket.Klines;

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
public class KlinesWebSocketClient {

  private final WebSocketClient webSocketClient;
  private final KlinesBinanceWebSocketHandler klinesHandler;
  private final ScheduledExecutorService reconnectExecutor = Executors.newSingleThreadScheduledExecutor();
  private final AtomicBoolean connecting = new AtomicBoolean(false);
  private final AtomicBoolean reconnectScheduled = new AtomicBoolean(false);
  
  private volatile WebSocketSession currentSession;
  private volatile String currentSymbol = "";
  private volatile String currentInterval = "";
  private volatile String lastConnectedSymbol = "";
  private volatile String lastConnectedInterval = "";

  public synchronized void connect(String symbol, String interval) {
    log.info("KlinesWebSocketClient.connect called with symbol={}, interval={}", symbol, interval);
    
    if (symbol == null || symbol.isBlank() || interval == null || interval.isBlank()) {
      lastConnectedSymbol = "";
      lastConnectedInterval = "";
      closeSession();
      return;
    }

    String normalizedSymbol = symbol.trim().toLowerCase();
    String normalizedInterval = interval.trim().toLowerCase();

    // Nếu đã kết nối với symbol và interval này rồi thì không cần kết nối lại
    if (normalizedSymbol.equals(currentSymbol) && normalizedInterval.equals(currentInterval) 
        && currentSession != null && currentSession.isOpen()) {
      log.info("Already connected to {}@{}, skipping reconnection", normalizedSymbol, normalizedInterval);
      return;
    }

    lastConnectedSymbol = normalizedSymbol;
    lastConnectedInterval = normalizedInterval;
    closeSession();

    if (!connecting.compareAndSet(false, true)) {
      log.info("Already connecting, skipping");
      return;
    }

    currentSymbol = normalizedSymbol;
    currentInterval = normalizedInterval;
    klinesHandler.setCurrentSymbol(normalizedSymbol.toUpperCase());
    klinesHandler.setCurrentInterval(normalizedInterval);

    // Stream klines của Binance: {symbol}@kline_{interval}
    String url = "wss://stream.binance.com/ws/" + normalizedSymbol + "@kline_" + normalizedInterval;
    log.info("Connecting to Binance Klines: {}", url);

    webSocketClient.execute(klinesHandler, new WebSocketHttpHeaders(), URI.create(url))
        .whenComplete((session, exception) -> {
          connecting.set(false);

          if (exception != null) {
            log.error("Cannot connect to Binance Klines WebSocket for {}@{}", normalizedSymbol, normalizedInterval, exception);
            scheduleReconnect();
            return;
          }

          currentSession = session;
          log.info("Binance Klines WebSocket connected for {}@{}", normalizedSymbol, normalizedInterval);

          // Nếu symbol hoặc interval đã đổi trong lúc đang kết nối
          if (!normalizedSymbol.equals(lastConnectedSymbol) || !normalizedInterval.equals(lastConnectedInterval)) {
            connect(lastConnectedSymbol, lastConnectedInterval);
          }
        });
  }

  @EventListener
  public void reconnect(KlinesWebSocketDisconnectedEvent event) {
    log.info("Scheduling Binance Klines WebSocket reconnect: {}", event.reason());
    scheduleReconnect();
  }

  private void scheduleReconnect() {
    if (lastConnectedSymbol.isBlank() || lastConnectedInterval.isBlank() || !reconnectScheduled.compareAndSet(false, true)) {
      return;
    }

    reconnectExecutor.schedule(() -> {
      reconnectScheduled.set(false);
      if (!lastConnectedSymbol.isBlank() && !lastConnectedInterval.isBlank()) {
        connect(lastConnectedSymbol, lastConnectedInterval);
      }
    }, 5, TimeUnit.SECONDS);
  }

  public synchronized void closeSession() {
    if (currentSession != null && currentSession.isOpen()) {
      try {
        currentSession.close();
      } catch (Exception e) {
        log.warn("Error closing Klines session", e);
      }
    }
    currentSession = null;
    currentSymbol = "";
    currentInterval = "";
  }

  @PreDestroy
  public void shutdown() {
    closeSession();
    reconnectExecutor.shutdownNow();
  }
}
