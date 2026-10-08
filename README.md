# TechChallengeApi

Back-end monolítico (MVP) do sistema de atendimento e execução de serviços da oficina — FIAP SOAT, Tech Challenge Fase 1.

> Este README está em construção; as seções abaixo cobrem o banco de dados e a execução local.

## Pré-requisitos

- Java 21
- Docker e Docker Compose

## Configuração

```bash
cp .env.example .env   # ajuste os valores, principalmente DB_PASSWORD
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
├── clientes/
│   ├── cadastrar/    POST   /clientes
│   ├── consultar/    GET    /clientes, /clientes/{id}
│   ├── atualizar/    PUT    /clientes/{id}
│   ├── remover/      DELETE /clientes/{id}
│   ├── dominio/      ClienteRepository, ClienteResponse, validação CPF/CNPJ, exceções
│   └── entidades/    Cliente
├── config/           JPA e segurança
└── shared/           BaseEntity (persistência) e tratamento global de erros (web)
```

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
curl -i -X POST http://localhost:8080/clientes -H "Content-Type: application/json" \
  -d '{"firstName":"Maria","lastName":"Silva","email":"maria@email.com","documento":"529.982.247-25","telefone":"11999990000"}'
```

> A autenticação JWT das APIs administrativas ainda não foi implementada; por enquanto a API está aberta.

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
