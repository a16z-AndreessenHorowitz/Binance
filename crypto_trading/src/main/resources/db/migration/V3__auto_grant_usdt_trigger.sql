-- Tạo đồng USDT mặc định (Tiền pháp định) nếu chưa có
INSERT INTO assets (symbol, name, decimals, status, created_at) 
VALUES ('USDT', 'Tether USD', 8, 'active', NOW())
ON CONFLICT (symbol) DO NOTHING;

CREATE OR REPLACE FUNCTION grant_initial_usdt()
RETURNS TRIGGER AS $$ 
DECLARE
  v_usdt_asset_id BIGINT;
BEGIN

  SELECT id INTO v_usdt_asset_id FROM assets WHERE symbol= 'USDT';

  IF v_usdt_asset_id IS NOT NULL THEN
    
    INSERT INTO wallets (user_id, asset_id, available_balance, locked_balance,updated_at)
    VALUES(NEW.id, v_usdt_asset_id, 1000, 0, NOW());

    INSERT INTO ledger_entries (user_id, asset_id, amount, type, reference_id, reference_type, balance_after, created_at)
    VALUES(NEW.id, v_usdt_asset_id, 1000, 'signup_bonus', NULL, NULL, 1000, NOW());
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER triggers_grant_initial_usdt
AFTER INSERT ON users
FOR EACH ROW
EXECUTE FUNCTION grant_initial_usdt();
