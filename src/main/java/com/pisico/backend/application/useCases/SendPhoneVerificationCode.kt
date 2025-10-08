package com.pisico.backend.application.useCases

import com.fasterxml.jackson.databind.ObjectMapper
import com.pisico.backend.application.ports.out.CachePort
import com.pisico.backend.application.ports.out.TextSender
import com.pisico.backend.domain.exceptions.RateLimitExceededException
import com.pisico.backend.infraestructure.`in`.dto.auth.VerifyPhoneRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.time.Instant

@Service
class SendPhoneVerificationCode(
    private val cachePort: CachePort,
    private val textSender: TextSender,
    private val objectMapper: ObjectMapper
) {
    companion object {
        const val VERIFICATION_PREFIX = "verification:"
        const val RATE_LIMIT_PREFIX = "ratelimit:send:"

        const val CODE_TTL_SECONDS = 120L // 2 minutos
        const val RATE_LIMIT_TTL_SECONDS = 60L // 1 minuto entre envíos

        private val logger = LoggerFactory.getLogger(SendPhoneVerificationCode::class.java)
    }

    fun execute(request: VerifyPhoneRequest) {
        // 1. Validar formato del número (sanitización)
        val sanitizedPhone = sanitizePhoneNumber(request.phoneNumber)
        validatePhoneNumber(sanitizedPhone)

        // 2. Rate limiting - prevenir spam
        checkRateLimit(sanitizedPhone)

        // 3. Generar código seguro
        val verificationCode = generateSecureCode()

        // 4. Crear datos de verificación
        val verificationData = VerificationData(
            code = verificationCode,
            attempts = 0,
            createdAt = Instant.now().toString()
        )

        // 5. Guardar en caché
        val cacheKey = "$VERIFICATION_PREFIX$sanitizedPhone"
        cachePort.save(
            key = cacheKey,
            value = objectMapper.writeValueAsString(verificationData),
            ttlSeconds = CODE_TTL_SECONDS
        )

        // 6. Enviar SMS
        try {
            textSender.sendText(request.language, sanitizedPhone, verificationCode)
            logger.info("Verification code sent to phone: ${maskPhoneNumber(sanitizedPhone)}")
        } catch (e: Exception) {
            // Si falla el envío, eliminar de caché
            cachePort.delete(cacheKey)
            logger.error("Failed to send SMS to ${maskPhoneNumber(sanitizedPhone)}", e)
            throw e
        }

        // 7. Establecer rate limit para este número
        val rateLimitKey = "$RATE_LIMIT_PREFIX$sanitizedPhone"
        cachePort.save(rateLimitKey, "1", RATE_LIMIT_TTL_SECONDS)
    }

    private fun checkRateLimit(phoneNumber: String) {
        val rateLimitKey = "$RATE_LIMIT_PREFIX$phoneNumber"

        if (cachePort.exists(rateLimitKey)) {
            logger.warn("Rate limit exceeded for phone: ${maskPhoneNumber(phoneNumber)}")
            throw RateLimitExceededException(
                "Too many requests. Please wait before requesting another code."
            )
        }
    }

    private fun generateSecureCode(): String {
        val secureRandom = SecureRandom()
        val min = 100000
        val max = 999999
        return secureRandom.nextInt(max - min + 1).plus(min).toString()
    }

    private fun validatePhoneNumber(phoneNumber: String) {
        if (phoneNumber.isBlank() || !phoneNumber.startsWith("+") || phoneNumber.length < 10) {
            throw IllegalArgumentException("Invalid phone number format. Must start with '+' and have at least 10 digits.")
        }
    }

    fun sanitizePhoneNumber(phoneNumber: String): String {
        return phoneNumber.replace(Regex("[^+\\d]"), "")
    }

    fun maskPhoneNumber(phoneNumber: String): String {
        // Enmascarar para logs: +34***345678 -> +34***678
        return if (phoneNumber.length > 6) {
            "${phoneNumber.substring(0, 3)}***${phoneNumber.takeLast(3)}"
        } else {
            "***"
        }
    }
}
