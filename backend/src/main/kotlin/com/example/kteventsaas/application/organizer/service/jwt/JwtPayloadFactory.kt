package com.example.kteventsaas.application.organizer.service.jwt

import com.example.kteventsaas.domain.organizer.entity.Organizer
import org.springframework.stereotype.Component

/**
 * JWT ペイロード生成ユーティリティ
 *
 * 【役割】
 * - 認証対象の Organizer ドメインモデルから JWT クレームマップを組み立てる。
 *
 * 【責務】
 * - Organizer の属性（メールアドレス、ロール、テナントID）を抽出し、
 *   JWT 仕様に則ったペイロード構造を提供する。
 * - ペイロード生成に関わるドメイン固有の変換ロジックをカプセル化する。
 *
 * 【補足】
 * - Issuer や署名ロジックは JwtIssuer/JwtTokenProvider に委譲し、
 *   本コンポーネントはあくまでクレームデータの整形に専念する。
 * - テナントや権限情報が増える場合、本クラスを拡張しやすい設計とする。
 */
@Component
class JwtPayloadFactory {
    fun createPayload(organizer: Organizer): Map<String, Any> {

        /**
         *  MEMO: mutableMapOf でなく、mapOf で不変な配列を返す。tokenType などの付与は呼び出し側で toMutableMap で行う
         * 「知っても害がない」かつ「受け取った側で必要な認可に使う情報」を JWT に含めるため、メールアドレスではなく ID を sub に指定
        */
        return mapOf(
            "sub" to organizer.id.toString(),
            "role" to organizer.role.name,
            "tenantId" to organizer.tenantId.toString()
        )
    }
}
