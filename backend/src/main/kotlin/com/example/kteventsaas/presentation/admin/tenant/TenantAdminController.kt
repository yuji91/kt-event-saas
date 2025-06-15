package com.example.kteventsaas.presentation.admin.tenant

import com.example.kteventsaas.application.tenant.service.TenantApplicationService
import com.example.kteventsaas.domain.tenant.valueobject.TenantName
import com.example.kteventsaas.presentation.admin.tenant.dto.CreateTenantRequest
import com.example.kteventsaas.presentation.admin.tenant.dto.TenantResponse
import com.example.kteventsaas.presentation.common.exception.ErrorCodes
import com.example.kteventsaas.presentation.common.exception.ErrorResponse
import com.example.kteventsaas.presentation.common.exception.NotFoundException
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.*
import org.springframework.dao.DataIntegrityViolationException
import java.util.UUID

/**
 * 管理者向けテナント管理APIのエントリポイント
 *
 * - 外部からのHTTPリクエストを最初に受け取る「Presentation層」のControllerであり、
 * `/admin/tenants` 以下のエンドポイント群を提供する。
 *
 * ---
 * 【役割】
 * - 管理画面（Adminドメイン）からのリクエストを受け取り、
 *   テナントの作成・取得・一覧取得などの処理を提供する。
 *
 * 【設計方針】
 * - Controller層の責務は、DTOバリデーションとアプリケーションサービスの呼び出しに限定する。
 * - 業務ロジックや永続化の責務は他レイヤ（Service / Domain / Infrastructure）に委譲。
 *
 * 【補足】
 * - 一時的に例外ハンドリングを本クラス内で定義しているが、将来的には
 *   グローバル例外ハンドラ（@ControllerAdvice）への移行を想定。
 */
@RestController
@RequestMapping("/admin/tenants")
class TenantAdminController(
    private val tenantApplicationService: TenantApplicationService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createTenant(@RequestBody @Valid request: CreateTenantRequest): TenantResponse {
        val tenant = tenantApplicationService.createTenant(request.name)
        return TenantResponse.from(tenant)
    }

    @GetMapping("/{id}")
    fun getTenant(@PathVariable id: UUID): TenantResponse {
        val tenant = tenantApplicationService.getTenant(id)
            ?: throw NotFoundException("Tenant not found", errorCode = ErrorCodes.TENANT_NOT_FOUND)
        return TenantResponse.from(tenant)
    }

    @GetMapping("/name/{name}")
    fun getTenantByName(@PathVariable name: String): TenantResponse {
        val tenant = tenantApplicationService.getTenantByName(TenantName(name))
            ?: throw NotFoundException("Tenant not found", errorCode = ErrorCodes.TENANT_NOT_FOUND)
        return TenantResponse.from(tenant)
    }

    @GetMapping
    fun listTenants(): List<TenantResponse> {
        return tenantApplicationService.listTenants()
            .map { TenantResponse.from(it) }
    }

    // TODO(@ControllerAdvice): 一時的に Controller でハンドリングする。
    //   将来的には GlobalExceptionHandler（@ControllerAdvice）へ移行予定
    //
    // ■ 400 Bad Request（DTOバリデーションエラー）----------------------------
    //   Controller で @Valid があり、DTOで @field:NotBlank などがあるので
    //   MethodArgumentNotValidException が投げられるのでハンドリングする
    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidationException(e: MethodArgumentNotValidException): ErrorResponse {
        val details = e.bindingResult.fieldErrors
            .joinToString("; ") { "${it.field}: ${it.defaultMessage}" }
        return ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            errorCode = "VALIDATION_FAILED",
            message = details
        )
    }

    // ■ 400 Bad Request（ドメイン層の IllegalArgumentException）-------------
    //   「空文字」「不正な値」のチェックをドメインで行い（init require）
    //   IllegalArgumentException を投げている場合にキャッチ
    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleIllegalArgument(e: IllegalArgumentException): ErrorResponse {
        return ErrorResponse(
            status    = HttpStatus.BAD_REQUEST.value(),
            errorCode = "INVALID_ARGUMENT",
            message   = e.message
        )
    }

    // ■ 404 Not Found -------------------------------------------------------
    @ExceptionHandler(NotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFoundException(e: NotFoundException): ErrorResponse {
        return ErrorResponse(
            status = HttpStatus.NOT_FOUND.value(),
            errorCode = e.errorCode,
            message = e.message
        )
    }

    // ■ 409 Conflict（重複エラー用の独自例外）-------------------------------
    // TODO: @ExceptionHandler(ConflictException::class) では補足できない理由を知りたい
    @ExceptionHandler(DataIntegrityViolationException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleConflictException(e: DataIntegrityViolationException): ErrorResponse {
        return ErrorResponse(
            status    = HttpStatus.CONFLICT.value(),
            errorCode = ErrorCodes.TENANT_ALREADY_EXISTS, // Springの例外を扱うため、固定値で渡す
            message   = "Tenant already exists" // e.message だと SQLエラーをパースする必要がある
        )
    }
}
