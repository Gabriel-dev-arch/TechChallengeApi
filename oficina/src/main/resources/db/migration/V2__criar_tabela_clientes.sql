-- Clientes da oficina. O documento (CPF ou CNPJ) é armazenado sem máscara e identifica o cliente.
CREATE TABLE clientes (
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name  varchar(75)  NOT NULL,
    last_name   varchar(75)  NOT NULL,
    full_name   varchar(151) NOT NULL,
    email       varchar(254) NOT NULL,
    documento   varchar(14)  NOT NULL,
    telefone    varchar(20),
    created_at  timestamptz  NOT NULL,
    updated_at  timestamptz  NOT NULL,
    version     bigint       NOT NULL,
    CONSTRAINT uk_clientes_documento UNIQUE (documento)
);

CREATE INDEX idx_clientes_full_name ON clientes (full_name);
