package com.example.kteventsaas.presentation.admin.tenant.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * テナント新規作成時のリクエストDTO
 *
 * ---
 * 【役割】
 * - 管理者向けAPIで受け取るテナント名入力を保持する。
 *
 * 【責務】
 * - 入力された値に対してプレゼンテーション層での構文バリデーションを実行する。
 * - 不正な入力（空文字・文字数超過）を早期に弾き、アプリケーション層にドメイン変換可能な状態で渡す。
 *
 * 【補足】
 * - このDTOは Controller にてドメインの `TenantName` へ変換される前提で設計されている。
 * - 値のビジネスルール（例：重複不可、予約語チェック等）はアプリケーション/ドメイン層が担う。
 * （現状は ApplicationService のコードを最低限にしたい意図があり、インフラ層の TenantJpaEntity で対応している）
 */
data class CreateTenantRequest(
    @field:NotBlank(message = "Tenant name must not be blank")
    @field:Size(max = 255, message = "Tenant name must be 255 characters or less")
    val name: String
)
