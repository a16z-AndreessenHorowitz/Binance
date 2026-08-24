import stompClient from "./stompClient";

export function startOrderBookSocket(
  symbol: string,
  onOrderBook: (orderBook: any) => void
) {
  // Biến này dùng để lưu orderBookSubscription hiện tại.
  let orderBookSubscription: { unsubscribe: () => void } | undefined;

  // Chuẩn hóa symbol: "BTC_USDT" hoặc "BTCUSDT" -> "BTCUSDT"
  const cleanSymbol = symbol ? symbol.replace(/[^a-zA-Z0-9]/g, "").toUpperCase() : "";

  function subscribeOrderBook() {
    // Kiểm tra xem subscribe chưa nếu đã có subscription rồi hoặc chưa có symbol thì return
    if (orderBookSubscription || !cleanSymbol) {
      return;
    }

    const topic = `/topic/order-book/${cleanSymbol}`;
    console.log(`ORDER BOOK SOCKET: subscribing ${topic}`);

    orderBookSubscription = stompClient.subscribe(topic, (message) => {
      const data = JSON.parse(message.body);
      onOrderBook(data);
    });
  }

  // Nếu STOMP đã connected thì subscribe ngay
  if (stompClient.connected) {
    subscribeOrderBook();
  }

  const previousOnConnect = stompClient.onConnect;
  stompClient.onConnect = (frame) => {
    // Nếu previousOnConnect có tồn tại thì gọi hàm onConnect cũ.
    previousOnConnect?.(frame);
    console.log("ORDER BOOK SOCKET: STOMP connected");
    subscribeOrderBook();
  };

  // Kiểm tra xem stomp đã activate chưa
  if (!stompClient.active) {
    console.log("ORDER BOOK SOCKET: activating STOMP client");
    stompClient.activate();
  }

  return () => {
    console.log(`ORDER BOOK SOCKET: unsubscribe /topic/order-book/${cleanSymbol}`);
    orderBookSubscription?.unsubscribe();
    orderBookSubscription = undefined;
    stompClient.onConnect = previousOnConnect;
  };
}
