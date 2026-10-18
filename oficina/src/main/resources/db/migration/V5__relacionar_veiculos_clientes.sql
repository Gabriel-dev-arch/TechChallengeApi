ALTER TABLE veiculos ADD COLUMN cliente_id uuid NOT NULL;

ALTER TABLE veiculos ADD CONSTRAINT fk_veiculos_cliente
    FOREIGN KEY (cliente_id) REFERENCES clientes (id);

CREATE INDEX idx_veiculos_cliente_id ON veiculos (cliente_id);
