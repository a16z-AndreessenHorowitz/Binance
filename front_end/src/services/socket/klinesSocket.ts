import stompClient, { onStompConnect } from "./stompClient";

export function startKlinesSocket(
  symbol: string,
  interval: string,
  onKline: (kline: any) => void
) {
  let klinesSubscription: { unsubscribe: () => void } | undefined;

  const cleanSymbol = symbol ? symbol.replace(/[^a-zA-Z0-9]/g, "").toUpperCase() : "";

  if (!cleanSymbol) return () => {};

  const topic = `/topic/klines/${cleanSymbol}/${interval}`;

  function subscribeKlines() {
    if (klinesSubscription || !cleanSymbol || !stompClient.connected) {
      return;
    }

    console.log(`KLINES SOCKET: subscribing ${topic}`);

    klinesSubscription = stompClient.subscribe(topic, (message) => {
      const data = JSON.parse(message.body);
      onKline(data);
    });
  }

  // Đăng ký callback khi kết nối STOMP
  const unregisterConnect = onStompConnect(() => {
    subscribeKlines();
  });

  if (!stompClient.active) {
    console.log("KLINES SOCKET: activating STOMP client");
    stompClient.activate();
  }

  return () => {
    console.log(`KLINES SOCKET: unsubscribe /topic/klines/${cleanSymbol}/${interval}`);
    unregisterConnect();
    klinesSubscription?.unsubscribe();
    klinesSubscription = undefined;
  };
}
