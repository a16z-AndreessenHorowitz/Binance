package com.example.crypto_trading.websocket.ListCoin;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
//file có nhiệm vụ kết nối tới websocket binance để lấy dữ liệu
@Component
@RequiredArgsConstructor
@Slf4j
public class ListCoinBinanceWebSocketClient {
  private final WebSocketClient webSocketClient; //nó nằm bên config kết nối ra ngoàu websocket của binance
  private final ListCoinBinanceWebSocketHandler listCoinBinanceWebSocketHandler;
  private final ScheduledExecutorService reconnectExecutor = Executors.newSingleThreadScheduledExecutor();
  private final AtomicBoolean connecting = new AtomicBoolean(false);
  private final AtomicBoolean reconnectScheduled = new AtomicBoolean(false);
  private volatile WebSocketSession listCoinSession;
  private volatile List<String> lastListCoinSymbols = Collections.emptyList();
  private volatile List<String> currentListCoinSymbols = Collections.emptyList();

  public synchronized void connect(List<String> symbols) {
    //chuẩn hoá danh sách thành symbol ["BTCUSDT", "ETHUSDT", "btcusdt", "", null, "ETHUSDT"] => ["btcusdt@ticker", "ethusdt@ticker"]
    List<String> normalizedSymbols = symbols.stream()
        .filter(symbol -> symbol != null && !symbol.isBlank())
        .map(symbol -> symbol.toLowerCase() + "@ticker")
        .distinct()
        .toList();

    // nếu như không có danh sách coin thì đóng socket và return
    if (normalizedSymbols.isEmpty()) {
      lastListCoinSymbols = Collections.emptyList();
      closeListCoinSession();
      currentListCoinSymbols = Collections.emptyList();
      return;
    }

    if (normalizedSymbols.equals(currentListCoinSymbols) && listCoinSession != null && listCoinSession.isOpen()) {
      return;
    }
      //tạo ra danh sách bản sao normalizedSymbols không ảnh hưởng nó
    List<String> requestSymbols = new ArrayList<>(normalizedSymbols);
    // gán danh sách copy vào bién lastListCoinSymbols
    lastListCoinSymbols = requestSymbols;
    //đóng kết nối session cũ danh sách coin hiện tại để chuẩn bị cập nhật new data
    closeListCoinSession();

    // ngăn chặn nhiều luồng cùng kết nối
    if (!connecting.compareAndSet(false, true)) {
      return;
    }

    String streams = String.join("/", requestSymbols);
    String url = "wss://stream.binance.com/stream?streams=" + streams;

    webSocketClient.execute(listCoinBinanceWebSocketHandler,
        new WebSocketHttpHeaders(),
        URI.create(url)
    ).whenComplete((session, exception) -> {

      connecting.set(false);

      if (exception != null) {
        log.error("Cannot connect to Binance list coin WebSocket", exception);
        scheduleReconnect();
        return;
      }
      //lưu đối tượng session vào biến toàn cục listCoinSession
      listCoinSession = session;
      //Lưu danh sách symbols vào biến đã được kết nối thành công vào biến currentListCoinSymbols
      currentListCoinSymbols = requestSymbols;

      //nếu như danh sách symbols đã thay đổi thì gọi connect để kết nối lại
      if (!requestSymbols.equals(lastListCoinSymbols)) {
        connect(lastListCoinSymbols.stream()
            .map(symbol -> symbol.replace("@ticker", "").toUpperCase())
            .toList());
      }
    });
  }

  @EventListener
  public void reconnect(ListCoinBinanceWebSocketDisconnectedEvent event) {
    log.info("Scheduling Binance WebSocket reconnect: {}", event.reason());
    scheduleReconnect();
  }

  private void scheduleReconnect() {
    if (!reconnectScheduled.compareAndSet(false, true)) {
      return;
    }

    reconnectExecutor.schedule(() -> {
      reconnectScheduled.set(false);
      connect(lastListCoinSymbols.stream()
          .map(symbol -> symbol.replace("@ticker", "").toUpperCase())
          .toList());
    }, 5, TimeUnit.SECONDS);
  }

  private void closeListCoinSession() {
    if (listCoinSession == null || !listCoinSession.isOpen()) {
      currentListCoinSymbols = Collections.emptyList();
      return;
    }

    try {
      listCoinSession.close();
      currentListCoinSymbols = Collections.emptyList();
    } catch (Exception exception) {
      log.warn("Cannot close old Binance list coin WebSocket session", exception);
    }
  }

  @PreDestroy
  public void shutdown() {
    closeListCoinSession();
    reconnectExecutor.shutdownNow();
  }
}
