CREATE TABLE public.ordem_servico (
	orcamento                   numeric(38, 2) NULL,
	created_at                  timestamptz(6) NULL,
	updated_at                  timestamptz(6) NULL,
	"version"                   int8           NOT NULL,
	cliente_id                  uuid           NOT NULL,
	id                          uuid           NOT NULL,
	veiculo_id                  uuid           NULL,
	diagnostico                 varchar(255)   NULL,
	first_name                  varchar(255)   NULL,
	last_name                   varchar(255) NULL,
	status                      varchar(255) NULL,
	CONSTRAINT ordem_servico_cliente_id_key UNIQUE (cliente_id),
	CONSTRAINT ordem_servico_pkey PRIMARY KEY (id),
    CONSTRAINT ordem_servico_veiculo_id_key UNIQUE (veiculo_id)
);


-- public.ordem_servico foreign keys

ALTER TABLE public.ordem_servico ADD CONSTRAINT fk_os_client FOREIGN KEY (cliente_id) REFERENCES public.clientes(id);
ALTER TABLE public.ordem_servico ADD CONSTRAINT fk_os_veiculo FOREIGN KEY (veiculo_id) REFERENCES public.veiculos(id);


-----------------------------------------------------------------------------------------------------------------------------------------------------------------

CREATE TABLE public.ordem_servico_itens (
	itens_id         uuid NOT NULL,
	ordem_servico_id uuid NOT NULL,
	CONSTRAINT ordem_servico_itens_itens_id_key UNIQUE (itens_id)
);


-- public.ordem_servico_itens foreign keys

ALTER TABLE public.ordem_servico_itens ADD CONSTRAINT fk_os_pecas_insumos FOREIGN KEY (itens_id) REFERENCES public.pecas_insumos(id);
ALTER TABLE public.ordem_servico_itens ADD CONSTRAINT fk_ordem_servicos FOREIGN KEY (ordem_servico_id) REFERENCES public.ordem_servico(id);

-----------------------------------------------------------------------------------------------------------------------------------------------------------------

CREATE TABLE public.ordem_servico_servicos (
	ordem_servico_id uuid NOT NULL,
	servicos_id      uuid NOT NULL,
	CONSTRAINT ordem_servico_servicos_servicos_id_key UNIQUE (servicos_id)
);


ALTER TABLE public.ordem_servico_servicos ADD CONSTRAINT fk_servicos FOREIGN KEY (servicos_id) REFERENCES public.servicos(id);
ALTER TABLE public.ordem_servico_servicos ADD CONSTRAINT fk_ordem_servicos FOREIGN KEY (ordem_servico_id) REFERENCES public.ordem_servico(id);