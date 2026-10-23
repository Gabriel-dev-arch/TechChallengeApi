CREATE TABLE public.ordem_servico_aud (
	id uuid                 NOT NULL,
	cliente_id uuid         NOT NULL,
	veiculo_id uuid         NOT NULL,
	orcamento numeric(38, 2) NULL,
	status varchar(255)     NOT NULL,
	diagnostico varchar(255) NULL,
	revtype int2            NOT NULL,
	created_at timestamptz(6) NULL,
	rev timestamptz(6)      NOT NULL,
	updated_at timestamptz(6) NULL,
	CONSTRAINT ordem_servico_aud_pkey PRIMARY KEY (rev, id));

--------------------------------------------------------------------------------------------------------

CREATE TABLE public.ordem_servico_itens_aud (
	quantidade numeric(38, 2) NOT NULL,
	revtype int2              NOT NULL,
	rev timestamptz(6)        NOT NULL,
	itens_id uuid             NOT NULL,
	ordem_servico_id uuid     NOT NULL,
	CONSTRAINT ordem_servico_itens_aud_pkey PRIMARY KEY (quantidade, rev, itens_id, ordem_servico_id)
);

--------------------------------------------------------------------------------------------------------

CREATE TABLE public.ordem_servico_servicos_aud (
	servicos_id uuid      NOT NULL,
	ordem_servico_id uuid NOT NULL,
	revtype int2          NOT NULL,
	rev timestamptz(6)    NOT NULL,
	CONSTRAINT ordem_servico_servicos_aud_pkey PRIMARY KEY (rev, ordem_servico_id, servicos_id)
);