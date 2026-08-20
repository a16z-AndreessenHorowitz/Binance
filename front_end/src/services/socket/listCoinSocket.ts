import stompClient from "./stompClient";

export function startListCoinSocket(onTicker: (ticker: any) => void) {
  let listCoinSubscription: { unsubscribe: () => void } | undefined;

  function subscribeListCoinTicker() {
    if (listCoinSubscription) {
      return;
    }

    console.log("LIST COIN SOCKET: subscribing /topic/list-coins/ticker");

    listCoinSubscription = stompClient.subscribe("/topic/list-coins/ticker", (message) => {
      const ticker = JSON.parse(message.body);
      onTicker(ticker);
    });
  }

  if (stompClient.connected) {
    subscribeListCoinTicker();
  }

  const previousOnConnect = stompClient.onConnect;
  stompClient.onConnect = (frame) => {
    previousOnConnect?.(frame);
    console.log("LIST COIN SOCKET: STOMP connected");
    subscribeListCoinTicker();
  };

  if (!stompClient.active) {
    console.log("LIST COIN SOCKET: activating STOMP client");
    stompClient.activate();
  }

  return () => {
    console.log("LIST COIN SOCKET: unsubscribe");
    listCoinSubscription?.unsubscribe();
    listCoinSubscription = undefined;
    stompClient.onConnect = previousOnConnect;
  };
}
