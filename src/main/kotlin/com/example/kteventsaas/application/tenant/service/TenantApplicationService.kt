package com.example.kteventsaas.application.tenant.service

import com.example.kteventsaas.domain.tenant.entity.Tenant
import com.example.kteventsaas.domain.tenant.repository.TenantRepository
import com.example.kteventsaas.domain.tenant.valueobject.TenantName
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * テナント関連のユースケースを提供するアプリケーションサービス
 *
 * ---
 * 【役割】
 * - プレゼンテーション層（REST API）からの入力を受け取り、
 *   ドメイン層のエンティティ操作や永続化を調整する中間レイヤ。
 *
 * 【責務】
 * - ドメインモデルの生成やリポジトリの呼び出しといった、ユースケース単位の処理をまとめる。
 * - 複雑なビジネスロジックを保持するのではなく、必要に応じてドメインサービスへ委譲する構成を保つ。
 *
 * 【補足】
 * - 現時点では重複チェックは未実装であり、DB制約違反により重複を検出している。
 * （このファイルでは最低限の内容のみ記載して、重複内容のコードを排除して、全体的なファイルの役割を理解したいため）
 * - 上記の内容はアプリケーション層で明示的に行うことで、DBに依存せず柔軟でUX配慮したエラーメッセージを返せる。
 * - 本来はDB側 `unique` 制約と併用して、整合性を二重に保証すべき。
 */
@Service
class TenantApplicationService(
    private val tenantRepository: TenantRepository
) {

    fun createTenant(name: String): Tenant {
        val tenant = Tenant(
            name = TenantName(name)
        )
        return tenantRepository.save(tenant)
    }

    fun getTenant(id: UUID): Tenant? {
        return tenantRepository.findById(id)
    }

    fun getTenantByName(name: TenantName): Tenant? {
        return tenantRepository.findByName(name)
    }

    fun listTenants(): List<Tenant> {
        return tenantRepository.findAll()
    }
}
