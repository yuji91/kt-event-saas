package com.example.kteventsaas.presentation.admin.auth

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

/**
 * 管理者ログイン画面の表示制御を行う Controller
 *
 * ---
 * 【役割】
 * - 管理画面（/admin/login）のログインフォーム表示処理を担うPresentation層のコントローラ。
 *
 * 【責務】
 * - GETリクエストでログイン画面（login.html）を表示する。
 * - 認証失敗時やログアウト後のメッセージ表示に対応する。
 *
 * 【補足】
 * - 認証処理自体は Spring Security に委ねており、本コントローラではフォーム表示のみに限定。
 * - `AdminSecurityConfig` にて `/admin/login` がログインページに設定されている前提で機能。
 * - `login?error` や `login?logout` のようなパラメータは、Spring Security が自動で付与する。
 *
 * 【注意】
 * - 認証POST処理は Spring Security の `UsernamePasswordAuthenticationFilter` が内部で処理するため、
 *   本クラスでは定義不要。
 * - CSRF対策やセッションIDの固定化（Session Fixation Protection）は `AdminSecurityConfig` 側で設定する。
 */
@Controller
@RequestMapping("/admin")
class AdminLoginController {

    /**
     * ログインフォームの表示処理
     *
     * @param error ログインエラー時に付与されるクエリパラメータ（例: login?error）
     * @param logout ログアウト完了時に付与されるクエリパラメータ（例: login?logout）
     * @param model Viewに渡すメッセージや状態を保持するModel
     * @return login.html テンプレート名
     */
    @GetMapping("/login")
    fun redirectLoginPage(
        @RequestParam(name = "error", required = false) error: String?,
        @RequestParam(name = "logout", required = false) logout: String?,
        model: Model
    ): String {
        if (error != null) {
            model.addAttribute("loginError", true)
        }
        if (logout != null) {
            model.addAttribute("logoutSuccess", true)
        }
        // 明示的にテンプレートを返す（redirect: ではない）
        return "admin/login"
    }
}
