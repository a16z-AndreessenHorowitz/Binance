package com.example.crypto_trading.websocket.ListCoin;

// ó chỉ là một “gói tin thông báo” nội bộ trong Spring.
// Hiểu đơn giản:
// BinanceWebSocketHandler:
//   "Ê, socket bị ngắt rồi nè" 
//   -> publish BinanceWebSocketDisconnectedEvent

// BinanceWebSocketClient:
//   nghe event đó
//   -> reconnect lại
// record trong Java tự tạo sẵn cho bạn:

public record ListCoinBinanceWebSocketDisconnectedEvent(String reason) {
}
