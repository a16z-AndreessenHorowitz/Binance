const BASE_URL = "http://localhost:8080/api/binance";




export async function getListCoin(page = 1, limit = 30) {
  const response = await fetch(`${BASE_URL}/list-coins?page=${page}&limit=${limit}`);
  if (!response.ok) {
    throw new Error("I can't get list coin info");
  }

  return response.json();
}
