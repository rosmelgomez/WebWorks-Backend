-- Moneda usada por los planes y los metodos de pago
INSERT INTO public.money (id, name) VALUES (1, 'Soles') ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('public.money', 'id'), (SELECT MAX(id) FROM public.money));
