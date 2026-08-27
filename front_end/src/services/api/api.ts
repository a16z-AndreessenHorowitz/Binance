const BASE_URL = "http://localhost:8080/api/binance";



export async function getListCoin(page = 1, limit = 30) {
  const response = await fetch(`${BASE_URL}/list-coins?page=${page}&limit=${limit}`);
  if (!response.ok) {
    throw new Error("I can't get list coin info");
  }

  return response.json();
}

export async function getOrderBook(symbol = "BTCUSDT") {
  const response = await fetch(`${BASE_URL}/order-book/start?symbol=${symbol}`)
  if(!response.ok){
    throw new Error("Cant not get order book")
  }
  return response.text()
}

export async function getCoinTicker (symbol = "BTCUSDT") {
  const response = await fetch(`${BASE_URL}/ticker/start?symbol=${symbol}`)
  if(!response.ok){
    throw new Error("Cant not get coin info")
  }
  return response.json()
}
  
export async function startKlines(symbol = "BTCUSDT", interval = "15m", limit = 500) {
  const response = await fetch(`${BASE_URL}/klines/start?symbol=${symbol}&interval=${interval}&limit=${limit}`)
  if(!response.ok){
    throw new Error("Cant not start klines stream")
  }
  return response.json();
}