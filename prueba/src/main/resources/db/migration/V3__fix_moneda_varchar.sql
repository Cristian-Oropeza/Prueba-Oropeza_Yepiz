-- ============================================================================
-- Fix: Hibernate 6 valida String como VARCHAR por default; el CHAR(3) de V2
-- produce "wrong column type: found bpchar, expecting VARCHAR".
-- Cambiamos ambas columnas moneda a VARCHAR(3) manteniendo el CHECK regex.
-- ============================================================================

ALTER TABLE catalogo_productos
    ALTER COLUMN moneda TYPE VARCHAR(3)
    USING TRIM(moneda)::VARCHAR(3);

ALTER TABLE transacciones
    ALTER COLUMN moneda TYPE VARCHAR(3)
    USING TRIM(moneda)::VARCHAR(3);

-- Nota: el default y el CHECK siguen vigentes.
