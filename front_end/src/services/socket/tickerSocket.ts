import stompClient, { onStompConnect } from "./stompClient";

export function startTickerSocket(symbol: string, onTicker: (ticker: any) => void) {
  let tickerSubscription: { unsubscribe: () => void } | undefined;

  const cleanSymbol = symbol ? symbol.replace(/[^a-zA-Z0-9]/g, "").toUpperCase() : "";

  if (!cleanSymbol) return () => {};

  const topic = `/topic/ticker/${cleanSymbol}`;

  function subscribeTicker() {
    if (tickerSubscription || !cleanSymbol || !stompClient.connected) return;

    console.log(`TICKER SOCKET: subscribing ${topic}`);
    tickerSubscription = stompClient.subscribe(topic, (message) => {
      const data = JSON.parse(message.body);
      onTicker(data);
    });
  }

  // Đăng ký callback khi kết nối STOMP
  const unregisterConnect = onStompConnect(() => {
    subscribeTicker();
  });

  if (!stompClient.active) {
    console.log("TICKER SOCKET: activating STOMP client");
    stompClient.activate();
  }

  return () => {
    console.log(`TICKER SOCKET: unsubscribe /topic/ticker/${cleanSymbol}`);
    unregisterConnect();
    tickerSubscription?.unsubscribe();
    tickerSubscription = undefined;
  };
}
