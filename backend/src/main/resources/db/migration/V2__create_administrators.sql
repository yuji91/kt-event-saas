-- V2__create_administrators.sql

-- 拡張が未有効の場合の保険
CREATE EXTENSION IF NOT EXISTS "citext";
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- 管理者（Administrator）テーブル作成
CREATE TABLE administrators (
                                id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                email            CITEXT NOT NULL UNIQUE,                 -- 大文字小文字を区別しない一意性
                                password_digest  TEXT NOT NULL,                          -- BCrypt形式（例：$2a$10$...）
                                role             TEXT NOT NULL DEFAULT 'SYS_ADMIN',      -- 現在は単一ロール想定（ENUM化は後続）
                                created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                version          BIGINT NOT NULL DEFAULT 0
);

-- ロールのCHECK制約（後でENUMへ変更可能）
ALTER TABLE administrators
    ADD CONSTRAINT chk_admin_role
        CHECK (role IN ('SYS_ADMIN'));

-- 操作ユーザー用のインデックス（検索高速化）
CREATE INDEX idx_admin_email ON administrators (email);
