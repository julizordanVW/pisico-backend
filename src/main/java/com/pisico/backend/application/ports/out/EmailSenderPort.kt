package com.pisico.backend.application.ports.out

interface EmailSenderPort {
    fun sendVerificationEmail(email: String, token: String)
}