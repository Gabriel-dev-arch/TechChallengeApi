-- Usuários das APIs administrativas. A senha é guardada só como hash BCrypt.
-- O administrador de demonstração é criado pela aplicação na inicialização (ADMIN_USERNAME/ADMIN_PASSWORD),
-- nunca por migration: nenhum usuário, senha ou hash deve ser versionado aqui.
CREATE TABLE usuarios (
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    username    varchar(50)  NOT NULL,
    senha_hash  varchar(100) NOT NULL,
    perfil      varchar(20)  NOT NULL CHECK (perfil IN ('ADMIN')),
    ativo       boolean      NOT NULL DEFAULT true,
    created_at  timestamptz  NOT NULL,
    updated_at  timestamptz  NOT NULL,
    version     bigint       NOT NULL,
    CONSTRAINT uk_usuarios_username UNIQUE (username)
);
