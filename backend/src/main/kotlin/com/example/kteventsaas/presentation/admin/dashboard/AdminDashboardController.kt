package com.example.kteventsaas.presentation.admin.dashboard

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

/**
 * 管理者ダッシュボード画面の表示制御を行う Controller
 */
@Controller
@RequestMapping("/admin")
class AdminDashboardController {

    /**
     * ダッシュボード画面の表示処理
     *
     * @return dashboard.html テンプレート名
     */
    @GetMapping("/dashboard")
    fun showDashboard(): String {
        return "admin/dashboard"
    }
} 