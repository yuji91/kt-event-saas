package com.example.kteventsaas.presentation.common.exception

class NotFoundException(message: String, val errorCode: String) : RuntimeException(message)
