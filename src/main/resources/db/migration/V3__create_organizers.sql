-- V3__create_organizers.sql

-- Organizer テーブル作成
CREATE TABLE organizers (
                            id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                            tenant_id UUID NOT NULL,
                            email VARCHAR(255) NOT NULL,
                            password_digest VARCHAR(255) NOT NULL,
                            role VARCHAR(50) NOT NULL,
                            created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
                            updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
                            version     BIGINT NOT NULL DEFAULT 0,

    -- 外部キー制約
                            CONSTRAINT fk_organizers_tenant FOREIGN KEY (tenant_id)
                                REFERENCES tenants (id),

    -- 同一テナント内の email 重複を防止
                            CONSTRAINT uq_organizers_tenant_email UNIQUE (tenant_id, email)
);
