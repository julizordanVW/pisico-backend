package com.pisico.backend.application.ports.out

interface TextSender {
    fun sendText(language: String, phoneNumber: String, code: String)
}