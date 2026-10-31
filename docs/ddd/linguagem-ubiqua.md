# Glossário de Linguagem Ubíqua

## Geral

1. **Cliente** → Pessoa, dona ou responsável pelo veículo, interessada nos serviços da mecânica
2. **Veículo** → Automóvel referente ao serviço realizado
3. **Orçamento** → Proposta detalhada contendo uma lista dos serviços sugeridos, peças e valor total, enviado ao cliente para aprovação
4. **Diagnóstico** → Lista de problemas identificados  no veículo
5. **Peça** → Itens de reparo para os veículos que temos em estoque, que serão utilizados para fazer o  Reparo nos veículos
6. **Insumo** → Recurso material necessário para realizar o serviço
7. **Mecânico** → Profissional contratado pela mecânica,  capacitado a analisar, identificar e solucionar  problemas nos veículos
8. **Operador** → Abstração de um funcionário da mecânica, que pode ser o mecânico ou outro profissional responsável por registrar as informações do cliente e veículo
8. **Serviço** → Ação realizada pelo mecânico no veículo do cliente
9. **Estoque** → Local onde é controlado a quantidade disponíveis de peças e insumos

## Cadastro de Cliente

1. **CPF** → Registro identificador do cliente, caso esteja buscando serviços no veículo próprio ou que esteja responsável
2. **CNPJ** → Registro identificador do cliente, caso esteja buscando serviços no veículo vinculado a uma empresa

## Cadastro de Veículo

1. **Placa** → Placa de identificação do veículo
2. **Marca** → Fabricante do veículo
3. **Modelo** → Modelo do veículo respectivamente vinculado ao fabricante
4. **Ano** → Ano de fabricação do veículo

## Ordem de Serviço

1. **OS** → Ordem de Serviço
2. **Status da OS** → Etapas em que a ordem de serviço pode se apresentar:
   - **Recebida** → Veículo chegou na oficina mecânica, o diagnóstico ainda não foi concluído, apenas o veículo recebido.
   - **Em diagnóstico** → Mecânico está analisando o  veículo para identificação dos defeitos
   - **Aguardando aprovação** → Mecânico finalizou a identificação dos problemas, orçamento enviado ao cliente e aguardando aprovação de valores e sugestão de serviços
   - **Em execução** → Mecânico está realizando o serviço no veículo do cliente
   - **Finalizada** → Mecânico finalizou o serviço no veículo do cliente, cliente foi notificado sobre a conclusão do serviço
   - **Entregue** → Cliente retirou o veículo na mecânica