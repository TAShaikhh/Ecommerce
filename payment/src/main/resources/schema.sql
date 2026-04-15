CREATE TABLE IF NOT EXISTS payments (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  auction_id INTEGER NOT NULL UNIQUE,
  item_id INTEGER NOT NULL,
  winner_username TEXT NOT NULL,
  item_title TEXT NOT NULL,
  item_price REAL NOT NULL,
  shipping_cost REAL NOT NULL,
  expedited_shipping_cost REAL NOT NULL DEFAULT 0.0,
  total_paid REAL NOT NULL,
  card_name TEXT NOT NULL,
  card_last_four TEXT NOT NULL,
  shipping_days INTEGER NOT NULL,
  expedited INTEGER NOT NULL DEFAULT 0,
  status TEXT NOT NULL DEFAULT 'COMPLETED',
  created_at_epoch INTEGER NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_payments_auction_id ON payments(auction_id);
