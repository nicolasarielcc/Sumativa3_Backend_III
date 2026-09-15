-- Esquema de datos del Banco XYZ (equivalente al generado por JPA en backend-core).
-- Se usa CREATE TABLE IF NOT EXISTS para que sea idempotente.

CREATE TABLE IF NOT EXISTS cuenta (
    cuenta_id BIGINT NOT NULL,
    nombre     VARCHAR(255),
    saldo      DECIMAL(19, 2),
    edad       INTEGER,
    tipo       VARCHAR(255),
    PRIMARY KEY (cuenta_id)
);

CREATE TABLE IF NOT EXISTS transaccion (
    id          BIGINT NOT NULL AUTO_INCREMENT,
    cuenta_id   BIGINT,
    fecha       DATE,
    tipo        VARCHAR(255),
    monto       DECIMAL(19, 2),
    descripcion VARCHAR(255),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS movimiento (
    id    BIGINT NOT NULL,
    fecha DATE,
    monto DECIMAL(19, 2),
    tipo  VARCHAR(255),
    PRIMARY KEY (id)
);
