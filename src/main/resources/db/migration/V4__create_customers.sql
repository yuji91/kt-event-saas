-- V4__create_customers.sql

-- ========================================
-- customers テーブルの作成
-- - イベント参加者（Customer）向けの認証基盤
-- - Organizer と同様に、マルチテナント構成を前提とする
-- - テナント内ユニークな email によるログインをサポート
-- - ステータス管理（is_active）による退会・無効化にも対応
-- ========================================

CREATE TABLE customers (
                           id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                           tenant_id UUID NOT NULL,
                           email VARCHAR(255) NOT NULL,
                           password_digest VARCHAR(255) NOT NULL,
                           role VARCHAR(50) NOT NULL,
                           is_active BOOLEAN NOT NULL DEFAULT TRUE,
                           created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
                           updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
                           version BIGINT NOT NULL DEFAULT 0,

    -- 外部キー制約：tenants テーブルとの参照整合性を保証（別途 migration ファイルを作成して対応する）
                           CONSTRAINT fk_customers_tenant FOREIGN KEY (tenant_id)
                               REFERENCES tenants (id),

    -- 同一テナント内でのメール重複を防止
                           CONSTRAINT uq_customers_tenant_email UNIQUE (tenant_id, email)
);

-- ========================================
-- MEMO:
-- - is_active: アカウント一時停止・論理削除などに活用可能
-- - version: JPAの @Version による更新整合性チェック用
-- - email: GraphQL の loginCustomer にて識別子として使用
-- ========================================
