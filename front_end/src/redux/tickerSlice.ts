import { createSlice, type PayloadAction } from "@reduxjs/toolkit";
//createSlice  hàm của Redux Toolkit dùng để tạo một Redux slice. PayloadAction là kiểu TypeScript dùng để mô tả action của Redux.
export type TickerData = {
  symbol: string;
  lastPrice?: string;
  priceChange?: string;
  priceChangePercent?: string;
  highPrice?: string;
  lowPrice?: string;
  volume?: string;
};
//tạo ra 1 Một object mà key là string, còn value là TickerData.
type TickerState = {
  tickersBySymbol: Record<string, TickerData>;
};

// Đây là trạng thái ban đầu.
const initialState: TickerState = {
  tickersBySymbol: {},
};

// Tạo một khu vực Redux tên là ticker
const tickerSlice = createSlice({
  name: "ticker",
  initialState,
  reducers: { //các hàm thay đổi
    setTicker: (
      state,
      action: PayloadAction<{ symbol: string; ticker: TickerData }>
    ) => {
      const { symbol, ticker } = action.payload;
      state.tickersBySymbol[symbol] = ticker;
    },
    // clearTicker: (state, action: PayloadAction<string>) => {
    //   delete state.tickersBySymbol[action.payload];
    // },
  },
});

export const { setTicker } = tickerSlice.actions;


// selectTickerBySymbol Nó đơn giản là một hàm lấy dữ liệu ra.
export const selectTickerBySymbol = (
  state: { ticker: TickerState },
  symbol: string
) => state.ticker.tickersBySymbol[symbol];

export default tickerSlice.reducer;
