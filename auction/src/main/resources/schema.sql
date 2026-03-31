CREATE TABLE IF NOT EXISTS auctions (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  item_id INTEGER NOT NULL UNIQUE,
  seller_username TEXT NOT NULL,
  starting_price REAL NOT NULL,
  current_highest_bid REAL NOT NULL DEFAULT 0,
  highest_bidder_username TEXT,
  status TEXT NOT NULL DEFAULT 'ACTIVE',
  result TEXT,
  created_at_epoch INTEGER NOT NULL,
  duration_seconds INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS bids (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  auction_id INTEGER NOT NULL,
  bidder_username TEXT NOT NULL,
  amount INTEGER NOT NULL,
  placed_at_epoch INTEGER NOT NULL,
  FOREIGN KEY (auction_id) REFERENCES auctions(id)
);
