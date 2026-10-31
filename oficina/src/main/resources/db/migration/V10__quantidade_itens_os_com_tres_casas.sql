-- A quantidade dos itens da OS passa a ter a mesma escala do estoque de peças e insumos (numeric(12,3)):
-- a OS reserva, libera e consome do estoque exatamente a quantidade gravada aqui.
ALTER TABLE ordem_servico_itens     ALTER COLUMN quantidade TYPE numeric(12,3);
ALTER TABLE ordem_servico_itens_aud ALTER COLUMN quantidade TYPE numeric(12,3);
