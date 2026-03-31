CREATE TABLE IF NOT EXISTS items (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  owner_username TEXT NOT NULL,
  title TEXT NOT NULL,
  description TEXT NOT NULL,
  condition TEXT NOT NULL,
  keywords TEXT DEFAULT '',
  shipping_cost REAL NOT NULL DEFAULT 10.0,
  expedited_shipping_cost REAL DEFAULT 0.0,
  shipping_days INTEGER NOT NULL DEFAULT 7,
  image_url TEXT DEFAULT '',
  status TEXT NOT NULL DEFAULT 'ACTIVE',
  created_at_epoch INTEGER NOT NULL DEFAULT 0
);
