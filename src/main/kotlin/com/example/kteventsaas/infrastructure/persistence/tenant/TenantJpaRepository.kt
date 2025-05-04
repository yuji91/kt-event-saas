package com.example.kteventsaas.infrastructure.persistence.tenant

import com.example.kteventsaas.domain.tenant.entity.Tenant
import com.example.kteventsaas.domain.tenant.repository.TenantRepository
import com.example.kteventsaas.domain.tenant.valueobject.TenantName
import com.example.kteventsaas.infrastructure.persistence.tenant.mapper.TenantMapper
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * Spring Data JPA を用いて `TenantRepository` を実装するインフラ層の具象リポジトリ
 *
 * ---
 * 【役割】
 * - ドメイン層の `TenantRepository` インターフェースに対して、
 *   技術的な永続化処理（JPA / RDB）を提供するアダプター。
 *
 * 【責務】
 * - JPAエンティティを操作して、テナント情報の保存・取得を行う。
 * - MapStructによる `TenantMapper` を通じて、
 *   ドメインエンティティとの相互変換を担う。
 *
 * 【設計意図】
 * - ドメイン層がSpringやJPAに直接依存しないよう、技術的関心はインフラ層に集約する。
 * - 将来的にDB技術を変更した場合でも、ドメインやユースケースは影響を受けない構成とする。
 */
@Repository
class TenantJpaRepository(
    private val tenantSpringDataRepository: TenantSpringDataRepository,
    private val tenantMapper : TenantMapper
) : TenantRepository {

    override fun save(tenant: Tenant): Tenant {
        val savedEntity = tenantSpringDataRepository.save(tenantMapper.toEntity(tenant))
        return tenantMapper.toDomain(savedEntity)
    }

    override fun findById(id: UUID): Tenant? {
        return tenantSpringDataRepository.findByIdOrNull(id)?.let { tenantMapper.toDomain(it) }
    }

    override fun findByName(name: TenantName): Tenant? {
        return tenantSpringDataRepository.findByName(name)?.let { tenantMapper.toDomain(it) }
    }

    override fun findAll(): List<Tenant> {
        return tenantSpringDataRepository.findAll().map { tenantMapper.toDomain(it) }
    }
}
