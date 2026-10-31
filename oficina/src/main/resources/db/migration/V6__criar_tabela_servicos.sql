CREATE TABLE servicos (

	id 			uuid PRIMARY KEY DEFAULT gen_random_uuid(),
	created_at  timestamptz(6) NOT NULL,
	updated_at  timestamptz(6) NOT NULL,
	"version"   int8 		   NOT NULL,
	descricao 	varchar(255)   NOT NULL,
	valor 		numeric(10, 2) NOT NULL
);