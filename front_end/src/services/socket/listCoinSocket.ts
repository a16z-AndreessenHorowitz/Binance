import stompClient, { onStompConnect } from "./stompClient";

export function startListCoinSocket(onTicker: (ticker: any) => void) {
  let listCoinSubscription: { unsubscribe: () => void } | undefined;

  function subscribeListCoinTicker() {
    if (listCoinSubscription || !stompClient.connected) {
      return;
    }

    console.log("LIST COIN SOCKET: subscribing /topic/list-coins/ticker");

    listCoinSubscription = stompClient.subscribe("/topic/list-coins/ticker", (message) => {
      const ticker = JSON.parse(message.body);
      onTicker(ticker);
    });
  }

  // Đăng ký callback khi kết nối STOMP
  const unregisterConnect = onStompConnect(() => {
    subscribeListCoinTicker();
  });

  if (!stompClient.active) {
    console.log("LIST COIN SOCKET: activating STOMP client");
    stompClient.activate();
  }

  return () => {
    console.log("LIST COIN SOCKET: unsubscribe");
    unregisterConnect();
    listCoinSubscription?.unsubscribe();
    listCoinSubscription = undefined;
  };
}
// trong đoạn return 
// Nhớ đoạn trước:

// const previousOnConnect = stompClient.onConnect;

// Sau đó bạn đã thay onConnect:

// stompClient.onConnect = (frame) => {
//   previousOnConnect?.(frame);

//   subscribeListCoinTicker();
// };

// Khi cleanup:

// stompClient.onConnect = previousOnConnect;

// để trả lại onConnect ban đầu.