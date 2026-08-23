import { Client } from "@stomp/stompjs";

const client = new Client({
  //Client sẽ kết nối WebSocket đến backend tại:
  brokerURL: "ws://localhost:8080/ws",
  //Nếu mất kết nối, client sẽ tự động thử kết nối lại sau 5 giây.
  reconnectDelay: 5000,

  // Khi kết nối STOMP thành công, nó sẽ chạy hàm này và in: STOMP connected
  onConnect: () => {
    console.log("STOMP connected");
  },

  // Khi có lỗi STOMP (ví dụ: lỗi frame hoặc frame không hợp lệ)
  onStompError: (frame: unknown) => {
    console.error("STOMP error:", frame);
  },

  // Khi có lỗi WebSocket thuần túy
  onWebSocketError: (error: unknown) => {
    console.error("WebSocket error:", error);
  },
});

export default client;
