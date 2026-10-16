# TechChallengeApi

Back-end monolítico (MVP) do sistema de atendimento e execução de serviços da oficina — FIAP SOAT, Tech Challenge Fase 1.

> Este README está em construção; as seções abaixo cobrem o banco de dados e a execução local.

## Pré-requisitos

- Java 21
- Docker e Docker Compose

## Configuração

```bash
cp .env.example .env   # ajuste os valores, principalmente DB_PASSWORD e JWT_SECRET
```

O `.env` fica fora do versionamento (`.gitignore`). Ele é lido pelo `docker-compose` e, quando a aplicação roda fora do Docker, também pelo Spring (`spring.config.import`).

## Executando

**Tudo no Docker (banco + aplicação):**

```bash
docker compose up -d --build
```

**Aplicação local + banco no Docker:**

```bash
docker compose up -d db
cd oficina
./mvnw spring-boot:run
```

- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (usuário/senha padrão `admin/admin`)

## Testes

Os testes de integração sobem um PostgreSQL descartável via Testcontainers (requer Docker em execução):

```bash
cd oficina
./mvnw test
```

**Cobertura (JaCoCo):** para rodar os testes e gerar o relatório de cobertura, use `verify`:

```bash
cd oficina
./mvnw verify
```

- O relatório fica em `oficina/target/site/jacoco/index.html` (abra no navegador).
- O build falha se a cobertura de linhas ficar abaixo de **80%** (regra `check` do JaCoCo no `pom.xml`).
- O `./mvnw test` sozinho não gera o relatório nem aplica a regra dos 80%.

## Arquitetura

Monolito em **Vertical Slice**: o código é organizado por funcionalidade, e cada operação (fatia) concentra seu controller, request e service. O que é comum ao módulo fica em `dominio/` (repositório, DTO de resposta, regras/exceções) e `entidades/`.

```
oficina/src/main/java/com/techchallenge/oficina/
├── autenticacao/
│   ├── login/        POST   /autenticacao/login (emite o token JWT com o perfil)
│   └── dominio/      Perfil (ADMIN, CLIENTE) e ControleDeAcesso (proteção contra BOLA)
├── clientes/
│   ├── cadastrar/    POST   /clientes
│   ├── consultar/    GET    /clientes, /clientes/{id}
│   ├── atualizar/    PUT    /clientes/{id}
│   ├── remover/      DELETE /clientes/{id}
│   ├── dominio/      ClienteRepository, ClienteResponse, validação CPF/CNPJ, exceções
│   └── entidades/    Cliente
├── pecasinsumos/
│   ├── cadastrar/    POST   /pecas-insumos
│   ├── consultar/    GET    /pecas-insumos, /pecas-insumos/{id}
│   ├── atualizar/    PUT    /pecas-insumos/{id}
│   ├── repor/        POST   /pecas-insumos/{id}/entradas
│   ├── baixar/       POST   /pecas-insumos/{id}/saidas
│   ├── remover/      DELETE /pecas-insumos/{id}
│   ├── dominio/      PecaInsumoRepository, PecaInsumoResponse, TipoItem, UnidadeMedida, exceções
│   └── entidades/    PecaInsumo (regras de estoque)
├── config/           JPA, segurança (JWT) e Swagger
└── shared/           BaseEntity (persistência), exceções base (excecoes) e tratamento global de erros (web)
```

## Autenticação e autorização (JWT)

Todas as rotas exigem um token JWT no header `Authorization: Bearer <token>`, exceto o login e a documentação (Swagger). O token é emitido por `POST /autenticacao/login` e carrega o **perfil** do usuário.

**Usuários de demonstração** (definidos no `.env`, ver `.env.example`):

| Usuário | Senha | Perfil | Acesso |
|---|---|---|---|
| `admin` | `admin123` | `ADMIN` | Todas as APIs |
| `cliente` | `cliente123` | `CLIENTE` | Só rotas liberadas ao cliente e só recursos do próprio CPF/CNPJ (`CLIENTE_DOCUMENTO`) |

Hoje todas as APIs (clientes, veículos, serviços, peças e insumos) são administrativas: o `cliente` recebe `403` nelas.

| Situação | Status |
|---|---|
| Sem token, token inválido ou expirado | `401 Unauthorized` (não autenticado) |
| Token válido, mas o perfil não tem permissão para a rota | `403 Forbidden` (não autorizado) |
| Cliente acessando recurso de outro cliente | `403 Forbidden` |

O token é assinado com HS256 usando o `JWT_SECRET` (mínimo de 32 caracteres) e expira após `JWT_EXPIRACAO_MINUTOS` (padrão: 60). Claims: `sub` (usuário), `roles` (perfil) e, no token do cliente, `documento` (CPF/CNPJ sem máscara).

```bash
curl -s -X POST http://localhost:8080/autenticacao/login -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
# {"accessToken":"eyJ...","tokenType":"Bearer","expiresIn":3600}

TOKEN=eyJ...   # valor de accessToken
curl -i http://localhost:8080/clientes -H "Authorization: Bearer $TOKEN"
```

| Operação | Endpoint | Sucesso | Erros |
|---|---|---|---|
| Login | `POST /autenticacao/login` | `200` + token | `400` campos ausentes, `401` usuário ou senha inválidos |

**No Swagger UI:** execute o `POST /autenticacao/login`, copie o `accessToken`, clique em **Authorize** e cole o token (sem o prefixo `Bearer`).

### Proteção contra BOLA (OWASP API1:2023)

*Broken Object Level Authorization* é a vulnerabilidade nº 1 do [OWASP API Security Top 10](https://owasp.org/API-Security/editions/2023/en/0xa1-broken-object-level-authorization/): um usuário autenticado acessa dados de outro só trocando o id na URL (ex.: a OS de outro cliente). Ter um token válido não basta; é preciso verificar se o recurso pertence a quem está pedindo.

- O CPF/CNPJ do cliente vai **dentro do token** (claim `documento`), assinado pelo servidor. Ele não vem de parâmetro da requisição, então o cliente não consegue trocá-lo.
- `ControleDeAcesso.verificarDono(documentoDoDono)` (`autenticacao/dominio`) libera o `ADMIN`, libera o `CLIENTE` só quando o recurso é do documento do token e nega todo o resto com `403`. Na dúvida (sem perfil, sem documento), nega.
- Para liberar uma rota ao cliente (ex.: consulta e aprovação da OS): declarar a rota com `hasAnyRole("ADMIN", "CLIENTE")` no `SecurityConfig` e, no service, chamar `verificarDono` com o documento do cliente dono do recurso antes de devolvê-lo ou alterá-lo.

**Limitações conhecidas (MVP):** usuários e senhas ficam no `.env` (sem banco e sem hash de senha), há um único cliente de demonstração e o login não limita tentativas. Numa próxima versão, cada cliente teria login próprio, com senha em hash na tabela `clientes`.

## API de Clientes

| Operação | Endpoint | Sucesso | Erros |
|---|---|---|---|
| Criar | `POST /clientes` | `201` + header `Location` | `400` dados inválidos, `409` documento já cadastrado |
| Listar | `GET /clientes?documento=&page=&size=` | `200` (paginado, ordenado por nome; `documento` filtra por CPF/CNPJ) | `400` |
| Detalhar | `GET /clientes/{id}` | `200` | `400` id inválido, `404` |
| Atualizar | `PUT /clientes/{id}` | `200` | `400`, `404`, `409` |
| Remover | `DELETE /clientes/{id}` | `204` | `400`, `404` |

Campos: `firstName`, `lastName` (o `fullName` é gerado), `email`, `documento` (CPF ou CNPJ, com ou sem máscara; armazenado sem máscara; único) e `telefone` (opcional). CPF e CNPJ (inclusive o CNPJ alfanumérico) têm os dígitos verificadores validados. Erros seguem o formato `application/problem+json` (RFC 9457).

```bash
curl -i -X POST http://localhost:8080/clientes -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"firstName":"Maria","lastName":"Silva","email":"maria@email.com","documento":"529.982.247-25","telefone":"11999990000"}'
```

## API de Peças e Insumos

| Operação | Endpoint | Sucesso | Erros |
|---|---|---|---|
| Criar | `POST /pecas-insumos` | `201` + header `Location` | `400` dados inválidos, `409` código já cadastrado |
| Listar | `GET /pecas-insumos?tipo=&nome=&page=&size=` | `200` (paginado, ordenado por nome, só itens ativos; `tipo` filtra por `PECA`/`INSUMO` e `nome` busca por trecho, sem diferenciar maiúsculas) | `400` |
| Detalhar | `GET /pecas-insumos/{id}` | `200` (inclusive item removido, com `ativo: false`) | `400` id inválido, `404` |
| Atualizar | `PUT /pecas-insumos/{id}` | `200` (código, nome, descrição e preço; tipo, unidade e saldo não mudam) | `400`, `404`, `409` código já cadastrado |
| Entrada de estoque | `POST /pecas-insumos/{id}/entradas` | `200` | `400`, `404`, `409` item removido |
| Saída de estoque | `POST /pecas-insumos/{id}/saidas` | `200` | `400`, `404`, `409` estoque insuficiente ou item removido |
| Remover | `DELETE /pecas-insumos/{id}` | `204` (remoção lógica: o item fica inativo) | `400`, `404`, `409` item já removido ou com reserva |

Campos: `tipo` (`PECA` ou `INSUMO`), `codigo` (único, gravado em maiúsculas), `nome`, `descricao` (opcional), `unidadeMedida` (`UN`, `JOGO`, `L`, `ML`, `KG` ou `G`), `precoUnitario` (até 2 casas decimais) e `quantidadeInicial` (opcional, até 3 casas decimais). Entradas e saídas recebem `{"quantidade": ...}`, também com até 3 casas decimais.

**Controle de estoque:** cada item tem `quantidadeTotal`, `quantidadeReservada` e `quantidadeDisponivel` (total menos reservada). Saídas só podem usar o disponível, e `UN` e `JOGO` não aceitam quantidades fracionadas. Reservar, liberar e consumir quantidades reservadas são regras da entidade sem endpoint próprio: quem vai usá-las é a Ordem de Serviço, por exemplo para reservar as peças de um orçamento aprovado.

```bash
curl -i -X POST http://localhost:8080/pecas-insumos -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"tipo":"INSUMO","codigo":"OLEO-5W30","nome":"Óleo 5W30","unidadeMedida":"L","precoUnitario":45.90,"quantidadeInicial":20}'

curl -i -X POST http://localhost:8080/pecas-insumos/{id}/saidas -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"quantidade":2.5}'
```

## Banco de dados

**PostgreSQL 17**, acessado via Spring Data JPA/Hibernate, com schema versionado pelo **Flyway**.

**Por que PostgreSQL:**

- O domínio é fortemente relacional (cliente → veículo → ordem de serviço → serviços/peças) e exige integridade referencial.
- Transações ACID são necessárias em fluxos como aprovação do orçamento e baixa de estoque de peças.
- Suporta restrições e índices únicos (CPF/CNPJ, placa), consultas agregadas (tempo médio de execução) e tipos como `uuid` e `timestamptz`.
- Open source, com imagem oficial leve para o `docker-compose`.

**Como evoluir o schema:**

- Cada feature adiciona sua migration em `oficina/src/main/resources/db/migration/` (`V<n>__descricao.sql`). Convenções em `V1__baseline.sql`.
- `spring.jpa.hibernate.ddl-auto=validate`: o Hibernate só confere o mapeamento, nunca altera o schema.
- Entidades devem estender `shared.persistence.BaseEntity` (id UUID, `created_at`, `updated_at` e `version` para concorrência otimista).
- Testes de integração usam `@Import(PostgresTestConfiguration.class)`.
