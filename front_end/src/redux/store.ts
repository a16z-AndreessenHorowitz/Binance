import { configureStore } from "@reduxjs/toolkit";
import tickerReducer from "./tickerSlice";

// configureStore dùng để tạo kho Redux.

export const store= configureStore({
  reducer:{
    ticker: tickerReducer,
  }
})

// RootState chính là kiểu dữ liệu của toàn bộ Redux Store State.
export type RootState = ReturnType<typeof store.getState>;
// lấy kiểu của dispatch.
export type AppDispatch = typeof store.dispatch;
