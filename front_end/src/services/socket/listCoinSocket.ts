import stompClient from "./stompClient";

export function startListCoinSocket(onTicker: (ticker: any) => void) {

  // Biến này dùng để lưu listCoinSubscription hiện tại.
  let listCoinSubscription: { unsubscribe: () => void } | undefined;

  function subscribeListCoinTicker() {

    // Kiểm tra xem subscribe chưa nếu đã có subscription rồi thì return không subscrbile lần nữa
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
    // Nếu previousOnConnect có tồn tại thì gọi hàm onConnect cũ.
    previousOnConnect?.(frame);
    console.log("LIST COIN SOCKET: STOMP connected");
    subscribeListCoinTicker();
  };
  //kiểm tra xem stomp đã kết nối chưa
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