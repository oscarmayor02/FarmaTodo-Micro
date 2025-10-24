INSERT INTO products (id, name, price, stock) VALUES
    (10, 'Acetaminofén 500mg', 29900, 50) ON CONFLICT DO NOTHING,
  (11, 'Vitamina C 1g',     70000, 20) ON CONFLICT DO NOTHING,
    (12, 'Cepillo dental',    15000,  0) ON CONFLICT DO NOTHING;
