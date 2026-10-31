CREATE TABLE veiculos (
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    placa       varchar(7)  NOT NULL,
    marca       varchar(75) NOT NULL,
    modelo      varchar(75) NOT NULL,
    ano         integer     NOT NULL,
    created_at  timestamptz NOT NULL,
    updated_at  timestamptz NOT NULL,
    version     bigint      NOT NULL,
    CONSTRAINT uk_veiculos_placa UNIQUE (placa)
);
