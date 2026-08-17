# Frontend Structure

Du an dang dung React + Vite. Cau truc nay chia theo feature de sau nay app Binance/crypto lon len van de quan ly.

## Thu muc chinh

- `app/`: setup cap ung dung nhu router, providers, store.
- `assets/`: anh, icon, font va tai nguyen tinh.
- `components/`: component dung chung nhieu noi.
- `config/`: cau hinh app, bien moi truong, endpoint.
- `constants/`: hang so dung chung.
- `features/`: nghiep vu rieng theo tung domain, gom rieng `admin/` neu la tinh nang quan tri.
- `hooks/`: custom hook dung chung.
- `layouts/`: layout khung trang cho client va admin.
- `lib/`: wrapper thu vien ben ngoai hoac helper ky thuat.
- `pages/`: cac man hinh route-level, chia `client/`, `admin/`, va `auth/`.
- `services/`: ket noi API, Binance API, websocket.
- `styles/`: style global, variables, theme.
- `types/`: kieu du lieu dung chung neu sau nay chuyen sang TypeScript.
- `utils/`: ham tien ich thuan.
- `mocks/`: du lieu gia lap khi chua co API.
- `tests/`: setup test va helper test.

## Goi y dat file

- Man hinh nguoi dung: `pages/client/`
- Man hinh quan tri: `pages/admin/`
- Login/register/user session: `pages/auth/` va `features/auth/`
- Market list, ticker, order book: `features/market/`
- Buy/sell, order form, chart trading: `features/trading/`
- Vi, balance, PnL: `features/portfolio/`
- Danh sach coin theo doi: `features/watchlist/`
- Tin tuc crypto: `features/news/`
- Quan ly user, coin, order, transaction, report, setting: `features/admin/`
- Component nut, input, modal dung chung: `components/ui/`
- Header, sidebar nho dung lai: `components/layout/`
- Khung trang lon cho tung vung: `layouts/ClientLayout/` va `layouts/AdminLayout/`
- Binance REST/WebSocket client: `services/binance/`
