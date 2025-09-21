package com.pisico.backend.infraestructure.out

import com.pisico.backend.application.ports.out.EmailSenderPort
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ResourceLoader
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service

@Service
open class EmailSenderAdapter(
    private val javaMailSender: JavaMailSender,
    private val resourceLoader: ResourceLoader,
    @Value("\${app.email.logo-url}") private val logoUrl: String
) : EmailSenderPort {

    override fun sendVerificationEmail(email: String, token: String) {
        val verificationLink = "pisico://verify?token=$token"
        val subject = "Verifica tu email en pisiCo"
        val templatePath = "classpath:templates/emails/email_verification.html"
        val logoUrl = "http://localhost:8080/logo.png"

        try {
            val htmlContent = loadTemplate(templatePath)
                .replace("{{cta_url}}", verificationLink)
                .replace("{{logo_url}}", logoUrl)
                .replace("{{titulo}}", subject)
                .replace("{{primary_color}}", "#008080")
                .replace("{{secondary_color}}", "#008080")

            sendEmail(email, subject, htmlContent)
            println("Sending verification email to $email with token: $token")
        } catch (e: Exception) {
            println("Failed to send verification email to $email: ${e.message}")
        }
    }

    private fun loadTemplate(path: String): String {
        val resource = resourceLoader.getResource(path)
        return resource.file.readText(Charsets.UTF_8)
    }

    private fun sendEmail(to: String, subject: String, htmlContent: String) {
        val message = javaMailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true, "UTF-8")

        helper.setFrom("no-reply@pisico.com")
        helper.setTo(to)
        helper.setSubject(subject)
        helper.setText(htmlContent, true)

        javaMailSender.send(message)
    }
}
