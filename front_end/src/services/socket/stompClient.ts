import { Client } from "@stomp/stompjs";

const client = new Client({
  brokerURL: "ws://localhost:8080/ws",
  reconnectDelay: 5000,

  onConnect: () => {
    console.log("STOMP connected");
  },

  onStompError: (frame: unknown) => {
    console.error("STOMP error:", frame);
  },

  onWebSocketError: (error: unknown) => {
    console.error("WebSocket error:", error);
  },
});

export default client;
