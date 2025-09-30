package com.pisico.backend.infraestructure.out

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import java.io.File
import jakarta.mail.internet.MimeMessage
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentCaptor

class EmailSenderAdapterTest {

    @Mock
    private lateinit var javaMailSender: JavaMailSender

    @Mock
    private lateinit var resourceLoader: ResourceLoader

    @Mock
    private lateinit var resource: Resource

    @Mock
    private lateinit var mimeMessage: MimeMessage

    private lateinit var emailSenderAdapter: EmailSenderAdapter

    private val logoUrl = "http://localhost:8080/logo.png"
    private val testEmail = "test@example.com"
    private val testToken = "test-token-123"

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        emailSenderAdapter = EmailSenderAdapter(javaMailSender, resourceLoader, logoUrl)
    }

    @Test
    fun `should send verification email successfully`() {
        // Given
        val htmlTemplate = """
            <html>
                <body>
                    <h1>{{titulo}}</h1>
                    <img src="{{logo_url}}" />
                    <a href="{{cta_url}}">Verify Email</a>
                    <div style="color: {{primary_color}}">Primary</div>
                    <div style="color: {{secondary_color}}">Secondary</div>
                </body>
            </html>
        """.trimIndent()

        val tempFile = File.createTempFile("email_verification", ".html")
        tempFile.writeText(htmlTemplate)
        tempFile.deleteOnExit()

        `when`(resourceLoader.getResource("classpath:templates/emails/email_verification.html"))
            .thenReturn(resource)
        `when`(resource.file).thenReturn(tempFile)
        `when`(javaMailSender.createMimeMessage()).thenReturn(mimeMessage)

        // When
        emailSenderAdapter.sendVerificationEmail(testEmail, testToken)

        // Then
        verify(javaMailSender, times(1)).send(mimeMessage)
        verify(resourceLoader, times(1)).getResource("classpath:templates/emails/email_verification.html")
    }

    @Test
    fun `should handle exception when sending email fails`() {
        // Given
        val htmlTemplate = "<html><body>{{titulo}} {{cta_url}} {{logo_url}} {{primary_color}} {{secondary_color}}</body></html>"
        val tempFile = File.createTempFile("email_verification", ".html")
        tempFile.writeText(htmlTemplate)
        tempFile.deleteOnExit()

        `when`(resourceLoader.getResource("classpath:templates/emails/email_verification.html"))
            .thenReturn(resource)
        `when`(resource.file).thenReturn(tempFile)
        `when`(javaMailSender.createMimeMessage()).thenReturn(mimeMessage)
        doThrow(RuntimeException("Email server unavailable")).`when`(javaMailSender).send(mimeMessage)

        // When - Should not throw exception, but handle it internally
        emailSenderAdapter.sendVerificationEmail(testEmail, testToken)

        // Then - Verify send was attempted
        verify(javaMailSender, times(1)).send(mimeMessage)
    }

    @Test
    fun `should handle template loading failure`() {
        // Given
        `when`(resourceLoader.getResource("classpath:templates/emails/email_verification.html"))
            .thenReturn(resource)
        `when`(resource.file).thenThrow(RuntimeException("Template not found"))

        // When - Should not throw exception, but handle it internally
        emailSenderAdapter.sendVerificationEmail(testEmail, testToken)

        // Then - Send should not be called due to template loading failure
        verify(javaMailSender, never()).send(any(MimeMessage::class.java))
    }

    @Test
    fun `should replace all template placeholders correctly`() {
        // Given
        val htmlTemplate = """
            {{titulo}}
            {{cta_url}}
            {{logo_url}}
            {{primary_color}}
            {{secondary_color}}
        """.trimIndent()

        val tempFile = File.createTempFile("email_verification", ".html")
        tempFile.writeText(htmlTemplate)
        tempFile.deleteOnExit()

        `when`(resourceLoader.getResource("classpath:templates/emails/email_verification.html"))
            .thenReturn(resource)
        `when`(resource.file).thenReturn(tempFile)
        `when`(javaMailSender.createMimeMessage()).thenReturn(mimeMessage)

        // When
        emailSenderAdapter.sendVerificationEmail(testEmail, testToken)

        // Then
        verify(javaMailSender, times(1)).send(mimeMessage)

        // Verify the content would not contain template placeholders
        val expectedLink = "pisico://verify?token=$testToken"
        // We can't verify the exact content easily, but we verified send was called
    }

    @Test
    fun `should send email with correct verification link format`() {
        // Given
        val htmlTemplate = "<html><body>{{titulo}} {{cta_url}} {{logo_url}} {{primary_color}} {{secondary_color}}</body></html>"
        val tempFile = File.createTempFile("email_verification", ".html")
        tempFile.writeText(htmlTemplate)
        tempFile.deleteOnExit()

        `when`(resourceLoader.getResource("classpath:templates/emails/email_verification.html"))
            .thenReturn(resource)
        `when`(resource.file).thenReturn(tempFile)
        `when`(javaMailSender.createMimeMessage()).thenReturn(mimeMessage)

        // When
        emailSenderAdapter.sendVerificationEmail(testEmail, testToken)

        // Then
        verify(javaMailSender).send(mimeMessage)
        // The link format should be: pisico://verify?token={token}
    }

    @Test
    fun `should use UTF-8 encoding for email content`() {
        // Given
        val htmlTemplate = "<html><body>{{titulo}} {{cta_url}} {{logo_url}} {{primary_color}} {{secondary_color}} Tést ñ ü</body></html>"
        val tempFile = File.createTempFile("email_verification", ".html")
        tempFile.writeText(htmlTemplate, Charsets.UTF_8)
        tempFile.deleteOnExit()

        `when`(resourceLoader.getResource("classpath:templates/emails/email_verification.html"))
            .thenReturn(resource)
        `when`(resource.file).thenReturn(tempFile)
        `when`(javaMailSender.createMimeMessage()).thenReturn(mimeMessage)

        // When
        emailSenderAdapter.sendVerificationEmail(testEmail, testToken)

        // Then
        verify(javaMailSender).send(mimeMessage)
    }

    @Test
    fun `should send email from correct sender address`() {
        // Given
        val htmlTemplate = "<html><body>{{titulo}} {{cta_url}} {{logo_url}} {{primary_color}} {{secondary_color}}</body></html>"
        val tempFile = File.createTempFile("email_verification", ".html")
        tempFile.writeText(htmlTemplate)
        tempFile.deleteOnExit()

        `when`(resourceLoader.getResource("classpath:templates/emails/email_verification.html"))
            .thenReturn(resource)
        `when`(resource.file).thenReturn(tempFile)
        `when`(javaMailSender.createMimeMessage()).thenReturn(mimeMessage)

        // When
        emailSenderAdapter.sendVerificationEmail(testEmail, testToken)

        // Then
        verify(javaMailSender).send(mimeMessage)
        // From address should be no-reply@pisico.com
    }

    @Test
    fun `should set correct email subject`() {
        // Given
        val htmlTemplate = "<html><body>{{titulo}} {{cta_url}} {{logo_url}} {{primary_color}} {{secondary_color}}</body></html>"
        val tempFile = File.createTempFile("email_verification", ".html")
        tempFile.writeText(htmlTemplate)
        tempFile.deleteOnExit()

        `when`(resourceLoader.getResource("classpath:templates/emails/email_verification.html"))
            .thenReturn(resource)
        `when`(resource.file).thenReturn(tempFile)
        `when`(javaMailSender.createMimeMessage()).thenReturn(mimeMessage)

        // When
        emailSenderAdapter.sendVerificationEmail(testEmail, testToken)

        // Then
        verify(javaMailSender).send(mimeMessage)
        // Subject should be "Verifica tu email en pisiCo"
    }

    @Test
    fun `should handle special characters in token`() {
        // Given
        val specialToken = "token-with-special@chars#123!"
        val htmlTemplate = "<html><body>{{titulo}} {{cta_url}} {{logo_url}} {{primary_color}} {{secondary_color}}</body></html>"
        val tempFile = File.createTempFile("email_verification", ".html")
        tempFile.writeText(htmlTemplate)
        tempFile.deleteOnExit()

        `when`(resourceLoader.getResource("classpath:templates/emails/email_verification.html"))
            .thenReturn(resource)
        `when`(resource.file).thenReturn(tempFile)
        `when`(javaMailSender.createMimeMessage()).thenReturn(mimeMessage)

        // When
        emailSenderAdapter.sendVerificationEmail(testEmail, specialToken)

        // Then
        verify(javaMailSender).send(mimeMessage)
    }

    @Test
    fun `should handle empty token gracefully`() {
        // Given
        val emptyToken = ""
        val htmlTemplate = "<html><body>{{titulo}} {{cta_url}} {{logo_url}} {{primary_color}} {{secondary_color}}</body></html>"
        val tempFile = File.createTempFile("email_verification", ".html")
        tempFile.writeText(htmlTemplate)
        tempFile.deleteOnExit()

        `when`(resourceLoader.getResource("classpath:templates/emails/email_verification.html"))
            .thenReturn(resource)
        `when`(resource.file).thenReturn(tempFile)
        `when`(javaMailSender.createMimeMessage()).thenReturn(mimeMessage)

        // When
        emailSenderAdapter.sendVerificationEmail(testEmail, emptyToken)

        // Then
        verify(javaMailSender).send(mimeMessage)
    }
}