CREATE TABLE servicos (

	id uuid NOT NULL,
	veiculo_id uuid NOT NULL,
	cliente_id uuid NOT NULL,
	status int2 NOT NULL,
	created_at timestamptz(6) NOT NULL,
	orcamento_decidido_em timestamptz(6) NULL,
	updated_at timestamptz(6) NOT NULL,
	"version" int8 NOT NULL,
	codigo_acompanhamento uuid NOT NULL,
	diagnostico varchar(255) NOT NULL,
	orcamento_total numeric(38, 2) NULL,
	delivered_at numeric(38, 2) null,
	
	CONSTRAINT servicos_cliente_id_key UNIQUE (cliente_id),
	CONSTRAINT servicos_pkey PRIMARY KEY (id),
	CONSTRAINT servicos_veiculo_id_key UNIQUE (veiculo_id)
);


ALTER TABLE public.servicos ADD CONSTRAINT fk_veiculo FOREIGN KEY (veiculo_id) REFERENCES public.veiculos(id);
ALTER TABLE public.servicos ADD CONSTRAINT fk_cliente FOREIGN KEY (cliente_id) REFERENCES public.clientes(id);