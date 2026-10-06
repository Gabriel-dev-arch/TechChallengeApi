CREATE TABLE pecas_insumos (
    id                    uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    tipo                  varchar(10)   NOT NULL CHECK (tipo IN ('PECA', 'INSUMO')),
    codigo                varchar(40)   NOT NULL,
    nome                  varchar(120)  NOT NULL,
    descricao             varchar(500),
    unidade_medida        varchar(10)   NOT NULL CHECK (unidade_medida IN ('UN', 'JOGO', 'L', 'ML', 'KG', 'G')),
    preco_unitario        numeric(12,2) NOT NULL CHECK (preco_unitario >= 0),
    quantidade_total      numeric(12,3) NOT NULL DEFAULT 0 CHECK (quantidade_total >= 0),
    quantidade_reservada  numeric(12,3) NOT NULL DEFAULT 0
        CHECK (quantidade_reservada >= 0 AND quantidade_reservada <= quantidade_total),
    ativo                 boolean       NOT NULL DEFAULT true,
    created_at            timestamptz   NOT NULL,
    updated_at            timestamptz   NOT NULL,
    version               bigint        NOT NULL,
    CONSTRAINT uk_pecas_insumos_codigo UNIQUE (codigo)
);