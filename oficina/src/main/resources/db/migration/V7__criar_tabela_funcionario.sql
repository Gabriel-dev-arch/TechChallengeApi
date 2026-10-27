CREATE TABLE public.funcionarios (
	id          uuid           NOT NULL,
	nome        varchar(255)   NOT NULL,
	email       varchar(50) NULL,
	senha_hash  int2           NOT NULL,
	admin       boolean        NOT NULL,
	ativo       boolean        NOT NULL,
	created_at  timestamptz(6) NULL,
	updated_at  timestamptz(6) NULL,
	"version"   int8           NOT NULL,
	CONSTRAINT funcionarios_aud_pkey PRIMARY KEY (id));
