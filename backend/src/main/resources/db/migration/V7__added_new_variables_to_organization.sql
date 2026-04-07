-- Añadir columnas emailConfirmado y codigoVerificacion a la tabla organizacion
ALTER TABLE organizacion ADD COLUMN email_confirmado BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE organizacion ADD COLUMN codigo_verificacion INTEGER NOT NULL DEFAULT 0;

-- Actualizar datos de ejemplo existentes con valores para email_confirmado y codigo_verificacion
UPDATE organizacion SET email_confirmado = TRUE, codigo_verificacion = 123456 WHERE id = 1001;
UPDATE organizacion SET email_confirmado = TRUE, codigo_verificacion = 234567 WHERE id = 1002;
UPDATE organizacion SET email_confirmado = FALSE, codigo_verificacion = 345678 WHERE id = 1003;
