# TechChallengeApi

Back-end monolítico (MVP) do sistema de atendimento e execução de serviços da oficina — FIAP SOAT, Tech Challenge Fase 1.

## Sumário

- [Objetivo do projeto](#objetivo-do-projeto)
- [Descrição da solução](#descrição-da-solução)
- [Arquitetura](#arquitetura)
- [Tecnologias utilizadas](#tecnologias-utilizadas)
- [Pré-requisitos](#pré-requisitos)
- [Configuração](#configuração)
- [Execução](#execução)
- [Testes](#testes)
- [Documentação da API (Swagger)](#documentação-da-api-swagger)
- [Banco de dados](#banco-de-dados)
- [Autenticação e usuários de demonstração](#autenticação-e-usuários-de-demonstração)
- [Estrutura de diretórios](#estrutura-de-diretórios)
- [Documentação DDD](#documentação-ddd)
- [Vídeo da entrega](#vídeo-da-entrega)
- [Referência das APIs](#referência-das-apis)
- [Integração contínua](#integração-contínua)
- [Licença](#licença)

## Objetivo do projeto

Construir o MVP do back-end de um sistema integrado de atendimento e execução de serviços para uma oficina mecânica. O sistema organiza o trabalho da oficina, do recebimento do veículo à entrega, e dá ao cliente transparência sobre o orçamento e o andamento do serviço.

## Descrição da solução

API REST única (monolito) em Spring Boot que cobre:

- **Clientes e veículos:** cadastro de clientes (CPF ou CNPJ validados) e dos veículos de cada cliente (placa no padrão antigo ou Mercosul).
- **Catálogo de serviços:** serviços oferecidos pela oficina, com valor.
- **Peças e insumos:** cadastro e controle de estoque (entradas, saídas, quantidade reservada e disponível).
- **Ordens de serviço (OS):** abertura da OS para um veículo, inclusão de serviços e peças/insumos, orçamento calculado a partir desses itens e acompanhamento do status (recebida, em diagnóstico, aguardando aprovação, em execução, finalizada e entregue), com aprovação ou recusa do orçamento.
- **Segurança:** autenticação com JWT e autorização por perfil (`ADMIN` e `CLIENTE`).

Os termos do domínio seguem a [linguagem ubíqua](docs/ddd/linguagem-ubiqua.md) definida na modelagem DDD.

## Arquitetura

- **Monolito modular em Vertical Slice:** o código é organizado por funcionalidade (clientes, veículos, serviços, peças e insumos, ordem de serviço). Cada operação (fatia) concentra seu controller, request e service. O que é comum ao módulo fica em `dominio/` (repositório, DTO de resposta, regras/exceções) e `entidades/`. A estrutura completa está em [Estrutura de diretórios](#estrutura-de-diretórios).
- **Camada compartilhada (`shared/`):** `BaseEntity` (id UUID, auditoria de datas e versão para concorrência otimista), exceções base e tratamento global de erros no formato `application/problem+json` (RFC 9457).
- **Persistência:** PostgreSQL via Spring Data JPA, com o schema versionado pelo Flyway.
- **Segurança:** API *stateless*, protegida por token JWT (Spring Security OAuth2 Resource Server).
- **Execução:** aplicação e banco em contêineres Docker orquestrados pelo `docker-compose`, com Prometheus/Grafana e SonarQube opcionais.

Diagramas C4 do sistema: [contexto](docs/ddd/diagramas/c4/01_context_c4_model.png), [contêineres](docs/ddd/diagramas/c4/02_container_c4_model.png) e [componentes](docs/ddd/diagramas/c4/03_component_c4_model.png).

## Tecnologias utilizadas

- Java 21 e Spring Boot 4.1 (Web MVC, Data JPA, Validation, Security com OAuth2 Resource Server/JWT, Actuator)
- PostgreSQL 17 com Flyway
- springdoc-openapi 3.1 (Swagger UI)
- Lombok (habilite o *annotation processing* na IDE)
- Testes: JUnit 5, Testcontainers e JaCoCo
- Qualidade: PMD, SpotBugs, Checkstyle e SonarQube
- Observabilidade: Micrometer, Prometheus e Grafana
- Docker, Docker Compose e GitHub Actions

## Pré-requisitos

- Java 21 (o Maven vem pelo wrapper `./mvnw`)
- Docker e Docker Compose

## Configuração

```bash
cp .env.example .env   # ajuste os valores, principalmente DB_PASSWORD e JWT_SECRET
```

O `.env` fica fora do versionamento (`.gitignore`). Ele é lido pelo `docker-compose` e, quando a aplicação roda fora do Docker, também pelo Spring (`spring.config.import`).

Opcionais (não constam no `.env.example`): `DB_POOL_MAX_SIZE` (padrão `10`) e `DB_POOL_MIN_IDLE` (padrão `2`), que ajustam o pool de conexões (Hikari).

## Execução

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

**Serviços opcionais** (profiles do `docker-compose`, sobem só quando pedidos):

| Profile | Comando | Serviços |
|---|---|---|
| `observabilidade` | `docker compose --profile observabilidade up -d` | Prometheus em http://localhost:9090 e Grafana em http://localhost:3000 (usuário/senha padrão `admin/admin`), com datasource e dashboard provisionados a partir de `oficina/src/main/resources/grafana/` |
| `qualidade` | `docker compose --profile qualidade up -d sonarqube` | SonarQube em http://localhost:9000 |

### Dados de exemplo

O arquivo `oficina/src/main/resources/data.sql` traz dados de exemplo (serviços, peças e insumos, clientes, veículos e funcionários). Ele **não** é executado automaticamente (`spring.sql.init.mode: never`). Para carregá-lo, suba a aplicação uma vez (o Flyway cria as tabelas) e rode:

```bash
docker cp oficina/src/main/resources/data.sql oficina-db:/tmp/data.sql
```

```bash
docker exec oficina-db psql -U oficina -d oficina -f /tmp/data.sql
```

Troque `oficina` pelo `DB_USER`/`DB_NAME` do seu `.env`, se forem diferentes. Os ids são fixos, então rode o script uma única vez, num banco recém-criado. No Git Bash (Windows), rode o segundo comando com `MSYS_NO_PATHCONV=1` na frente; sem isso, o `/tmp/data.sql` é convertido num caminho do Windows.

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

Testes de integração novos devem usar `@Import(PostgresTestConfiguration.class)` (`src/test/.../support`), que sobe o PostgreSQL via Testcontainers. Os valores de JWT e dos usuários de teste ficam em `src/test/resources/config/application.yaml`, então os testes não dependem do `.env`.

**Análise estática:** os relatórios de PMD, SpotBugs e Checkstyle são gerados em `oficina/target/` e não quebram o build:

```bash
cd oficina
./mvnw compile pmd:pmd spotbugs:spotbugs checkstyle:checkstyle
```

## Documentação da API (Swagger)

Com a aplicação no ar:

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **Especificação OpenAPI (JSON):** http://localhost:8080/v3/api-docs

As duas rotas são públicas. Para chamar as APIs protegidas pelo Swagger UI:

1. execute o `POST /autenticacao/login` com um dos [usuários de demonstração](#autenticação-e-usuários-de-demonstração);
2. copie o `accessToken` da resposta;
3. clique em **Authorize** e cole o token (sem o prefixo `Bearer`).

Um resumo dos endpoints, com status de sucesso e de erro, está em [Referência das APIs](#referência-das-apis).

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

## Autenticação e usuários de demonstração

Todas as rotas exigem um token JWT no header `Authorization: Bearer <token>`, exceto o login e a documentação (Swagger). O token é emitido por `POST /autenticacao/login` e carrega o **perfil** do usuário.

**Usuários de demonstração** (definidos no `.env`, ver `.env.example`):

| Usuário | Senha | Perfil | Acesso |
|---|---|---|---|
| `admin` | `admin123` | `ADMIN` | Todas as APIs |
| `cliente` | `cliente123` | `CLIENTE` | Só rotas liberadas ao cliente e só recursos do próprio CPF/CNPJ (`CLIENTE_DOCUMENTO`) |

Hoje todas as APIs exigem o perfil `ADMIN`: o `cliente` recebe `403` nelas.

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

### Proteção contra BOLA (OWASP API1:2023)

*Broken Object Level Authorization* é a vulnerabilidade nº 1 do [OWASP API Security Top 10](https://owasp.org/API-Security/editions/2023/en/0xa1-broken-object-level-authorization/): um usuário autenticado acessa dados de outro só trocando o id na URL (ex.: a OS de outro cliente). Ter um token válido não basta; é preciso verificar se o recurso pertence a quem está pedindo.

- O CPF/CNPJ do cliente vai **dentro do token** (claim `documento`), assinado pelo servidor. Ele não vem de parâmetro da requisição, então o cliente não consegue trocá-lo.
- `ControleDeAcesso.verificarDono(documentoDoDono)` (`autenticacao/dominio`) libera o `ADMIN`, libera o `CLIENTE` só quando o recurso é do documento do token e nega todo o resto com `403`. Na dúvida (sem perfil, sem documento), nega.
- Para liberar uma rota ao cliente (ex.: consulta e aprovação da OS): declarar a rota com `hasAnyRole("ADMIN", "CLIENTE")` no `SecurityConfig` e, no service, chamar `verificarDono` com o documento do cliente dono do recurso antes de devolvê-lo ou alterá-lo.

**Limitações conhecidas (MVP):** usuários e senhas ficam no `.env` (sem banco e sem hash de senha), há um único cliente de demonstração e o login não limita tentativas. Numa próxima versão, cada cliente teria login próprio, com senha em hash na tabela `clientes`.

## Estrutura de diretórios

```
.
├── .github/workflows/      CI: testes, análise estática e publicação da imagem no GHCR
├── docs/ddd/               documentação DDD (event storming, domain storytelling, C4, linguagem ubíqua)
├── oficina/                aplicação Spring Boot (Maven)
│   ├── src/main/java/      código-fonte (detalhado abaixo)
│   ├── src/main/resources/ application.yaml, migrations Flyway (db/migration), data.sql e provisionamento do Grafana
│   ├── src/test/           testes unitários e de integração
│   ├── Dockerfile          build multi-stage da imagem da aplicação
│   └── pom.xml
├── docker-compose.yml      banco, aplicação e serviços opcionais (profiles)
├── prometheus.yml          configuração de coleta do Prometheus
└── .env.example            modelo das variáveis de ambiente
```

Código-fonte, organizado por funcionalidade (Vertical Slice):

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
├── veiculos/
│   ├── cadastrar/    POST   /veiculos
│   ├── consultar/    GET    /veiculos, /veiculos/{id}
│   ├── atualizar/    PUT    /veiculos/{id}
│   ├── remover/      DELETE /veiculos/{id}
│   ├── dominio/      VeiculoRepository, VeiculoResponse, validação de placa, exceções
│   └── entidades/    Veiculo (pertence a um Cliente)
├── servicos/
│   ├── cadastrar/    POST   /servicos
│   ├── consultar/    GET    /servicos, /servicos/{id}
│   ├── atualizar/    PUT    /servicos/{id}
│   ├── remover/      DELETE /servicos/{id}
│   ├── dominio/      ServicoRepository, ServicoResponse, exceções
│   └── entidades/    Servico (catálogo de serviços com valor)
├── pecasinsumos/
│   ├── cadastrar/    POST   /pecas-insumos
│   ├── consultar/    GET    /pecas-insumos, /pecas-insumos/{id}
│   ├── atualizar/    PUT    /pecas-insumos/{id}
│   ├── repor/        POST   /pecas-insumos/{id}/entradas
│   ├── baixar/       POST   /pecas-insumos/{id}/saidas
│   ├── remover/      DELETE /pecas-insumos/{id}
│   ├── reativar/     POST   /pecas-insumos/{id}/reativacao
│   ├── dominio/      PecaInsumoRepository, PecaInsumoResponse, TipoItem, UnidadeMedida, exceções
│   └── entidades/    PecaInsumo (regras de estoque)
├── ordem_servico/
│   ├── cadastrar/    abertura da OS, inclusão de serviços e peças/insumos e avanço de status
│   ├── consultar/    consultas de OS
│   ├── dominio/      OrdemServicoRepository, DTOs de resposta, exceções e estados da OS (statusos)
│   └── entidades/    OrdemServico, OrdemServicoItem, StatusOS
├── funcionario/
│   ├── dominio/      FuncionarioRepository, FuncionarioResponse, exceções
│   └── entidades/    Funcionario (responsável pela OS; sem API própria)
├── config/           JPA, segurança (JWT) e Swagger
└── shared/           BaseEntity (persistência), exceções base (excecoes) e tratamento global de erros (web)
```

## Documentação DDD

Os artefatos da modelagem do domínio ficam em [`docs/ddd/`](docs/ddd/):

- [Event Storming](docs/ddd/event-storming.pdf)
- [Linguagem ubíqua](docs/ddd/linguagem-ubiqua.md)
- Domain Storytelling: [recepção](docs/ddd/diagramas/domain-storytelling/01_recepcao.png), [ordem de serviço](docs/ddd/diagramas/domain-storytelling/02_ordem_de_servico.png) e [serviços, peças e insumos](docs/ddd/diagramas/domain-storytelling/03_servicos_e_pecas_insumos.png)
- Diagramas C4: [contexto](docs/ddd/diagramas/c4/01_context_c4_model.png), [contêineres](docs/ddd/diagramas/c4/02_container_c4_model.png) e [componentes](docs/ddd/diagramas/c4/03_component_c4_model.png)

## Vídeo da entrega

> Link do vídeo: _a adicionar_

## Referência das APIs

Todas as rotas abaixo exigem o token JWT de um usuário `ADMIN` (ver [Autenticação](#autenticação-e-usuários-de-demonstração)). Erros seguem o formato `application/problem+json` (RFC 9457).

### Clientes

| Operação | Endpoint | Sucesso | Erros |
|---|---|---|---|
| Criar | `POST /clientes` | `201` + header `Location` | `400` dados inválidos, `409` documento já cadastrado |
| Listar | `GET /clientes?documento=&page=&size=` | `200` (paginado, ordenado por nome; `documento` filtra por CPF/CNPJ) | `400` |
| Detalhar | `GET /clientes/{id}` | `200` | `400` id inválido, `404` |
| Atualizar | `PUT /clientes/{id}` | `200` | `400`, `404`, `409` |
| Remover | `DELETE /clientes/{id}` | `204` | `400`, `404`, `409` cliente com veículos ou ordens de serviço vinculados |

Campos: `firstName`, `lastName` (o `fullName` é gerado), `email`, `documento` (CPF ou CNPJ, com ou sem máscara; armazenado sem máscara; único) e `telefone` (opcional). CPF e CNPJ (inclusive o CNPJ alfanumérico) têm os dígitos verificadores validados.

```bash
curl -i -X POST http://localhost:8080/clientes -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"firstName":"Maria","lastName":"Silva","email":"maria@email.com","documento":"529.982.247-25","telefone":"11999990000"}'
```

### Veículos

| Operação | Endpoint | Sucesso | Erros |
|---|---|---|---|
| Criar | `POST /veiculos` | `201` + header `Location` | `400` dados inválidos, `404` cliente não encontrado, `409` placa já cadastrada |
| Listar | `GET /veiculos?placa=&page=&size=` | `200` (paginado, ordenado por placa; `placa` filtra pela placa exata, com ou sem hífen) | `400` |
| Detalhar | `GET /veiculos/{id}` | `200` | `400` id inválido, `404` |
| Atualizar | `PUT /veiculos/{id}` | `200` (placa, marca, modelo e ano; o cliente dono não muda) | `400`, `404`, `409` placa já cadastrada |
| Remover | `DELETE /veiculos/{id}` | `204` | `400`, `404`, `409` veículo com ordens de serviço vinculadas |

Campos: `placa` (padrão antigo `ABC1234`/`ABC-1234` ou Mercosul `ABC1D23`; gravada em maiúsculas, sem hífen; única), `marca` e `modelo` (até 75 caracteres), `ano` (4 dígitos) e `clienteId` (obrigatório no cadastro). A resposta inclui `clienteId`, `createdAt` e `updatedAt` (fuso `America/Sao_Paulo`).

```bash
curl -i -X POST http://localhost:8080/veiculos -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"placa":"BRA2E19","marca":"Chevrolet","modelo":"Onix","ano":2022,"clienteId":"<id do cliente>"}'
```

### Peças e Insumos

| Operação | Endpoint | Sucesso | Erros |
|---|---|---|---|
| Criar | `POST /pecas-insumos` | `201` + header `Location` | `400` dados inválidos, `409` código já cadastrado |
| Listar | `GET /pecas-insumos?tipo=&nome=&page=&size=` | `200` (paginado, ordenado por nome, só itens ativos; `tipo` filtra por `PECA`/`INSUMO` e `nome` busca por trecho, sem diferenciar maiúsculas) | `400` |
| Detalhar | `GET /pecas-insumos/{id}` | `200` (inclusive item removido, com `ativo: false`) | `400` id inválido, `404` |
| Atualizar | `PUT /pecas-insumos/{id}` | `200` (código, nome, descrição e preço; tipo, unidade e saldo não mudam) | `400`, `404`, `409` código já cadastrado |
| Entrada de estoque | `POST /pecas-insumos/{id}/entradas` | `200` | `400`, `404`, `409` item removido |
| Saída de estoque | `POST /pecas-insumos/{id}/saidas` | `200` | `400`, `404`, `409` estoque insuficiente ou item removido |
| Remover | `DELETE /pecas-insumos/{id}` | `204` (remoção lógica: o item fica inativo) | `400`, `404`, `409` item já removido ou com reserva |
| Reativar | `POST /pecas-insumos/{id}/reativacao` | `200` (o item volta à listagem e às movimentações) | `400`, `404`, `409` item já ativo ou código em uso por outro item ativo |

Campos: `tipo` (`PECA` ou `INSUMO`), `codigo` (gravado em maiúsculas; único entre os itens ativos, então o código de um item removido pode ser reutilizado), `nome`, `descricao` (opcional), `unidadeMedida` (`UN`, `JOGO`, `L`, `ML`, `KG` ou `G`), `precoUnitario` (até 2 casas decimais) e `quantidadeInicial` (opcional, até 3 casas decimais). Entradas e saídas recebem `{"quantidade": ...}`, também com até 3 casas decimais.

**Controle de estoque:** cada item tem `quantidadeTotal`, `quantidadeReservada` e `quantidadeDisponivel` (total menos reservada). Saídas só podem usar o disponível, e `UN` e `JOGO` não aceitam quantidades fracionadas. Reservar, liberar e consumir quantidades reservadas são regras da entidade, sem endpoint próprio.

```bash
curl -i -X POST http://localhost:8080/pecas-insumos -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"tipo":"INSUMO","codigo":"OLEO-5W30","nome":"Óleo 5W30","unidadeMedida":"L","precoUnitario":45.90,"quantidadeInicial":20}'

curl -i -X POST http://localhost:8080/pecas-insumos/{id}/saidas -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"quantidade":2.5}'
```

## Integração contínua

O workflow `.github/workflows/docker-publish.yml` roda a cada push (em qualquer branch) e a cada pull request para `main`, `develop` e `release/**`:

1. `mvn test` (com Testcontainers);
2. relatórios de PMD, SpotBugs e Checkstyle, publicados como artefato `static-analysis-reports`;
3. build da imagem Docker e push para o GitHub Container Registry (`ghcr.io/<owner>/<repo>`), com a tag do nome da branch e a do SHA do commit; na `main` também com `latest`.

## Licença

Distribuído sob a licença MIT. Veja [`LICENSE`](LICENSE).
