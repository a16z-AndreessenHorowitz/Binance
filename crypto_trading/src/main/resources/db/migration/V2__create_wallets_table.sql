
CREATE TABLE assets (
  id         BIGSERIAL PRIMARY KEY,
  symbol VARCHAR(20) NOT NULL UNIQUE,
  name VARCHAR(100) NOT NULL,
  decimals SMALLINT NOT NULL DEFAULT 8, --số chữ thập phân coin đó hỗ trợ
  status VARCHAR(20) NOT NULL DEFAULT 'active', --cho phép ngưng giao dịch 1 coin vd khi hệ thống có sự cố mà không cần xoá assets khỏi hệ thống
  created_at TIMESTAMP NOT NULL DEFAULT NOW() -- thời điểm coin vào hệ thống

);

CREATE TABLE wallets
(
    id         BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    asset_id BIGINT NOT NULL REFERENCES assets(id) ON DELETE RESTRICT, 
    available_balance NUMERIC(36,18) NOT NULL DEFAULT 0 CHECK (available_balance >= 0), -- số dư có thể dùng ngay
    locked_balance NUMERIC(36,18) NOT NULL DEFAULT 0 CHECK (locked_balance >= 0), -- số dư đang bị khoá ví dụ như đang trong lệnh buy, sell 
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(), -- lần cập nhật số dư gần nhất
    UNIQUE (user_id,asset_id) -- đảm bảo 1 user chỉ có đúng 1 dòng ví cho mỗi loại cọin
);

CREATE INDEX idx_wallets_user_id ON wallets(user_id);

CREATE TABLE trading_pairs (
    id              BIGSERIAL PRIMARY KEY,
    symbol          VARCHAR(20) NOT NULL UNIQUE,   -- BTCUSDT
    base_asset_id   BIGINT NOT NULL REFERENCES assets(id),   -- Coin được mua/bán (ví dụ BTC trong cặp BTC/USDT) — số lượng lệnh tính theo coin này.
    quote_asset_id  BIGINT NOT NULL REFERENCES assets(id),   -- Coin dùng để định giá/thanh toán (ví dụ USDT) — giá lệnh tính theo coin này.
    min_price       NUMERIC(36, 18) NOT NULL DEFAULT 0,
    max_price       NUMERIC(36, 18),
    tick_size       NUMERIC(36, 18) NOT NULL DEFAULT 0.01,  -- 	Bước nhảy giá nhỏ nhất — ví dụ 0.01 nghĩa là giá chỉ được đặt 60000.00, 60000.01... không được 60000.005. Giống quy tắc thật của Binance.
    min_qty         NUMERIC(36, 18) NOT NULL DEFAULT 0, --Giới hạn khối lượng lệnh — tránh lệnh quá nhỏ (spam) hoặc quá lớn (rủi ro).
    max_qty         NUMERIC(36, 18),
    step_size       NUMERIC(36, 18) NOT NULL DEFAULT 0.00000001, -- Bước nhảy khối lượng nhỏ nhất — ví dụ 0.00000100 nghĩa là quantity phải là bội số của giá trị này.
    status          VARCHAR(20) NOT NULL DEFAULT 'trading', -- trading (đang giao dịch bình thường) / halted (tạm dừng, ví dụ khi bảo trì hoặc sự cố) — chặn đặt lệnh mới khi cần.
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    CHECK (base_asset_id <> quote_asset_id)
);

CREATE INDEX idx_trading_pairs_symbol ON trading_pairs(symbol);

CREATE TABLE orders (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id          BIGINT NOT NULL REFERENCES users(id),
    pair_id          BIGINT NOT NULL REFERENCES trading_pairs(id), 	-- Lệnh thuộc cặp giao dịch nào.
    side             VARCHAR(4)  NOT NULL CHECK (side IN ('buy', 'sell')),  	--buy (mua) hoặc sell (bán).
    type             VARCHAR(10) NOT NULL CHECK (type IN ('limit', 'market')), --limit (đặt giá cụ thể, chờ khớp) hoặc market (khớp ngay theo giá thị trường hiện tại, không cần chỉ định giá).
    price            NUMERIC(36, 18),              -- NULL nếu là market order
    quantity         NUMERIC(36, 18) NOT NULL CHECK (quantity > 0), --	Tổng khối lượng muốn mua/bán khi đặt lệnh.
    filled_quantity  NUMERIC(36, 18) NOT NULL DEFAULT 0, --Khối lượng đã khớp được tính đến hiện tại. Ban đầu = 0, tăng dần mỗi lần có trade khớp vào lệnh này.
    status           VARCHAR(20) NOT NULL DEFAULT 'open', --	open (chưa khớp gì), partially_filled (khớp một phần), filled (khớp hết), cancelled (bị hủy).
    created_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    CHECK (filled_quantity >= 0 AND filled_quantity <= quantity)
);

CREATE INDEX idx_orders_pair_side_price ON orders(pair_id, side, price)
  WHERE status IN ('open','partially_filled');
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);

CREATE TABLE trades (
    id             BIGSERIAL PRIMARY KEY,
    pair_id        BIGINT NOT NULL REFERENCES trading_pairs(id),
    buy_order_id   UUID NOT NULL REFERENCES orders(id), --	Lệnh mua nào tham gia vào trade này.
    sell_order_id  UUID NOT NULL REFERENCES orders(id), --Lệnh bán nào tham gia vào trade này.
    price          NUMERIC(36, 18) NOT NULL,
    quantity       NUMERIC(36, 18) NOT NULL CHECK (quantity > 0), --	Khối lượng thực tế khớp trong lần này (có thể nhỏ hơn quantity gốc của cả 2 lệnh nếu là partial fill).
    taker_side     VARCHAR(4) NOT NULL CHECK (taker_side IN ('buy', 'sell')), --Bên nào là "người khớp vào" (taker) — bên đặt lệnh sau, chủ động khớp vào lệnh đã có sẵn (maker). Dùng để tính phí giao dịch khác nhau giữa maker/taker giống Binance thật.
    created_at     TIMESTAMP NOT NULL DEFAULT NOW()
);
 
CREATE INDEX idx_trades_pair_id ON trades(pair_id);
CREATE INDEX idx_trades_buy_order_id ON trades(buy_order_id);
CREATE INDEX idx_trades_sell_order_id ON trades(sell_order_id);
CREATE INDEX idx_trades_created_at ON trades(created_at);
 
CREATE TABLE ledger_entries (
    id             BIGSERIAL PRIMARY KEY, --	Định danh bản ghi sổ cái.
    user_id        BIGINT NOT NULL REFERENCES users(id), --Biến động số dư này của user nào.
    asset_id       BIGINT NOT NULL REFERENCES assets(id), --	Biến động trên coin nào.
    amount         NUMERIC(36, 18) NOT NULL,   -- Số tiền thay đổi — dương (+) là cộng, âm (-) là trừ.
    type           VARCHAR(20) NOT NULL,--Lý do biến động: trade (do khớp lệnh), lock (khóa tiền khi đặt lệnh), unlock (mở khóa khi hủy lệnh), deposit (nạp), withdraw (rút).
    reference_id   VARCHAR(50),         -- rỏ tới bản ghi gây ra biến động này — id của orders hoặc trades tương ứng.
    reference_type VARCHAR(20),         -- 	Cho biết reference_id trỏ tới bảng nào (order hay trade) — vì đây là khóa ngoại "đa hình" (polymorphic), không trỏ cố định 1 bảng.
    balance_after  NUMERIC(36, 18) NOT NULL,   -- Số dư available_balance sau khi ghi nhận thay đổi này — giúp bạn dò lại lịch sử số dư tại bất kỳ thời điểm nào mà không cần tính toán lại từ đầu.
    created_at     TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_ledger_user_id ON ledger_entries(user_id);
CREATE INDEX idx_ledger_reference ON ledger_entries(reference_type, reference_id);
 