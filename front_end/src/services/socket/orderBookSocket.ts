import stompClient, { onStompConnect } from "./stompClient";

export function startOrderBookSocket(
  symbol: string,
  onOrderBook: (orderBook: any) => void
) {
  let orderBookSubscription: { unsubscribe: () => void } | undefined;

  const cleanSymbol = symbol ? symbol.replace(/[^a-zA-Z0-9]/g, "").toUpperCase() : "";

  if (!cleanSymbol) return () => {};

  const topic = `/topic/order-book/${cleanSymbol}`;

  function subscribeOrderBook() {
    if (orderBookSubscription || !cleanSymbol || !stompClient.connected) {
      return;
    }

    console.log(`ORDER BOOK SOCKET: subscribing ${topic}`);

    orderBookSubscription = stompClient.subscribe(topic, (message) => {
      const data = JSON.parse(message.body);
      onOrderBook(data);
    });
  }

  // Đăng ký callback khi kết nối STOMP
  const unregisterConnect = onStompConnect(() => {
    subscribeOrderBook();
  });

  if (!stompClient.active) {
    console.log("ORDER BOOK SOCKET: activating STOMP client");
    stompClient.activate();
  }

  return () => {
    console.log(`ORDER BOOK SOCKET: unsubscribe /topic/order-book/${cleanSymbol}`);
    unregisterConnect();
    orderBookSubscription?.unsubscribe();
    orderBookSubscription = undefined;
  };
}


// giải thích vì sao stomp.onConnect cần gắn thêm hành động mới 
// Vì stompClient là 1 object duy nhất dùng chung, nên stompClient.onConnect cũng chỉ có 1 chỗ để gán. Nếu màn hình A gán:

// stompClient.onConnect = subscribeGiaCoin;

// Sau đó màn hình B gán tiếp:

// stompClient.onConnect = subscribeOrderBook;

// → Cái gán sau sẽ ĐÈ MẤT cái gán trước! Bây giờ stompClient.onConnect chỉ còn chạy subscribeOrderBook, còn subscribeGiaCoin bị mất tiêu, không bao giờ được gọi nữa dù màn hình A vẫn đang hiển thị và cần nó.

// Đây chính là vấn đề code đang cố tránh.




// trong hàm return 
// Trạng thái đầu:  onConnect = undefined
//                         ↓
// Gắn thêm:        onConnect = hàmA   (previousOnConnect đã lưu lại giá trị "undefined" từ trước)
//                         ↓
// Dọn dẹp (cleanup): onConnect = previousOnConnect  →  onConnect = undefined

// Tức là: trạng thái đầu là gì thì lúc dọn dẹp trả về đúng y trạng thái đó — dù nó là undefined (chưa có ai gán) hay là 1 hàm thật (do màn hình khác gán trước đó).
//  Code không quan tâm giá trị cụ thể là gì, nó chỉ đơn giản là "chụp ảnh lại trước khi sửa" rồi "khôi phục ảnh đó khi xong việc" — nguyên tắc chung là vậy, áp dụng cho mọi trường hợp.