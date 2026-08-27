import { Client } from "@stomp/stompjs";

const connectListeners = new Set<(frame: any) => void>();

const client = new Client({
  // Client sẽ kết nối WebSocket đến backend tại:
  brokerURL: "ws://localhost:8080/ws",
  // Nếu mất kết nối, client sẽ tự động thử kết nối lại sau 5 giây.
  reconnectDelay: 5000,

  // Khi kết nối STOMP thành công, gọi tất cả các listener đã đăng ký
  onConnect: (frame) => {
    console.log("STOMP connected");
    connectListeners.forEach((listener) => {
      try {
        listener(frame);
      } catch (err) {
        console.error("Error in STOMP connect listener:", err);
      }
    });
  },

  // Khi có lỗi STOMP
  onStompError: (frame: unknown) => {
    console.error("STOMP error:", frame);
  },

  // Khi có lỗi WebSocket thuần túy
  onWebSocketError: (error: unknown) => {
    console.error("WebSocket error:", error);
  },
});

/**
 * Đăng ký callback khi STOMP kết nối (hoặc gọi ngay nếu đã kết nối).
 * Trả về hàm unregister để hủy đăng ký khi component unmount.
 */
export function onStompConnect(callback: (frame: any) => void) {
  connectListeners.add(callback);
  if (client.connected) {
    try {
      callback(null);
    } catch (err) {
      console.error("Error running STOMP callback:", err);
    }
  }
  return () => {
    connectListeners.delete(callback);
  };
}

export default client;
