package com.example.kteventsaas.presentation.common.exception

import java.time.LocalDateTime

data class ErrorResponse(
    val status: Int,
    val errorCode: String,
    val message: String?,
    val timestamp: LocalDateTime = LocalDateTime.now()
)
