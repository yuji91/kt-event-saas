-- V1__initial_schema.sql
-- ① 汎用拡張
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";   -- gen_random_uuid() が使えない RDS 用
CREATE EXTENSION IF NOT EXISTS pgcrypto;      -- crypt() など将来利用可

/* =========================================================
   テナント境界
   ========================================================= */
CREATE TABLE tenants (
                         id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                         name        TEXT NOT NULL UNIQUE,
                         created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE users (
                       id         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                       tenant_id  UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
                       email      TEXT NOT NULL,
                       role       TEXT NOT NULL,                    -- 'OWNER' | 'ADMIN' | 'MEMBER'
                       created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                       UNIQUE (tenant_id, email)
);

/* =========================================================
   イベントとチケット
   ========================================================= */
CREATE TABLE events (
                        id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                        tenant_id     UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
                        title         TEXT NOT NULL,
                        venue         TEXT,
                        starts_at     TIMESTAMPTZ NOT NULL,
                        ends_at       TIMESTAMPTZ NOT NULL,
                        status        TEXT NOT NULL DEFAULT 'DRAFT',           -- enum は後続マイグレーションで
                        created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                        UNIQUE (tenant_id, title, starts_at)
);

CREATE INDEX idx_events_tenant_time ON events (tenant_id, starts_at);

CREATE TABLE ticket_types (
                              id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                              tenant_id     UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
                              event_id        UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
                              name            TEXT NOT NULL,
                              price_cents     INT  NOT NULL CHECK (price_cents >= 0),
                              quantity_total  INT  NOT NULL CHECK (quantity_total > 0),
                              quantity_sold   INT  NOT NULL DEFAULT 0,
                              UNIQUE (event_id, name)
);

CREATE TABLE tickets (
                         id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                         tenant_id     UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
                         ticket_type_id UUID NOT NULL REFERENCES ticket_types(id) ON DELETE CASCADE,
                         purchaser_id   UUID REFERENCES users(id),
                         purchased_at   TIMESTAMPTZ,
                         CONSTRAINT chk_purchase_consistency
                             CHECK ((purchaser_id IS NULL) = (purchased_at IS NULL))
);
CREATE INDEX idx_tickets_purchaser ON tickets (purchaser_id);

/* =========================================================
   行レベルセキュリティでマルチテナント分離
   ========================================================= */
ALTER TABLE tenants ENABLE ROW LEVEL SECURITY;
ALTER TABLE users   ENABLE ROW LEVEL SECURITY;
ALTER TABLE events  ENABLE ROW LEVEL SECURITY;
ALTER TABLE ticket_types ENABLE ROW LEVEL SECURITY;
ALTER TABLE tickets ENABLE ROW LEVEL SECURITY;

-- 現在のセッションにセットされた app.tenant_id 値だけを許可
CREATE POLICY tenant_isolation ON tenants
  USING (id = current_setting('app.tenant_id')::uuid);

DO $$  -- 同じポリシーをすべての子テーブルに適用
DECLARE tbl text;
BEGIN
FOR tbl IN SELECT unnest(ARRAY['users','events','ticket_types','tickets'])
                      LOOP
               EXECUTE format(
      'CREATE POLICY tenant_isolation ON %I
         USING (tenant_id = current_setting(''app.tenant_id'')::uuid);', tbl);
END LOOP;
END $$;
