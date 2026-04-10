ALTER TABLE suscripcion
ADD COLUMN transaccion_id VARCHAR(255),
ADD COLUMN estado_pago VARCHAR(50);

-- Actualizar las suscripciones viejas a PAGADA para que no den error
UPDATE suscripcion SET estado_pago = 'PAGADA' WHERE estado_pago IS NULL;