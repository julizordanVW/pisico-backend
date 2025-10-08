package com.pisico.backend.application.useCases

import com.fasterxml.jackson.databind.ObjectMapper
import com.pisico.backend.application.ports.out.CachePort
import com.pisico.backend.domain.exceptions.InvalidVerificationCodeException
import com.pisico.backend.domain.exceptions.MaxAttemptsExceededException
import com.pisico.backend.domain.exceptions.VerificationCodeExpiredException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ValidatePhoneVerificationCode(
    private val cachePort: CachePort,
    private val objectMapper: ObjectMapper,
    private val sendPhoneVerificationCode: SendPhoneVerificationCode
) {
    companion object {
        private const val MAX_ATTEMPTS = 3
        private val logger = LoggerFactory.getLogger(ValidatePhoneVerificationCode::class.java)
    }

    fun execute(phoneNumber: String, code: String) {
        val sanitizedPhone = sendPhoneVerificationCode.sanitizePhoneNumber(phoneNumber)
        val cacheKey = "${SendPhoneVerificationCode.VERIFICATION_PREFIX}$sanitizedPhone"

        val cachedData = cachePort.get(cacheKey) ?: run {
            logger.warn("No verification code found for phone: ${sendPhoneVerificationCode.maskPhoneNumber(sanitizedPhone)}")
            throw VerificationCodeExpiredException("Verification code expired or not found.")
        }

        val verificationData = objectMapper.readValue(cachedData, VerificationData::class.java)

        // Verificar intentos máximos
        if (verificationData.attempts >= MAX_ATTEMPTS) {
            cachePort.delete(cacheKey)
            logger.warn("Max attempts exceeded for phone: ${sendPhoneVerificationCode.maskPhoneNumber(sanitizedPhone)}")
            throw MaxAttemptsExceededException("Maximum verification attempts exceeded.")
        }

        // Validar código
        if (verificationData.code == code) {
            cachePort.delete(cacheKey)
            logger.info("Phone verified successfully: ${sendPhoneVerificationCode.maskPhoneNumber(sanitizedPhone)}")
            return
        }

        // Incrementar intentos fallidos
        val updatedData = verificationData.copy(attempts = verificationData.attempts + 1)
        cachePort.save(
            key = cacheKey,
            value = objectMapper.writeValueAsString(updatedData),
            ttlSeconds = SendPhoneVerificationCode.CODE_TTL_SECONDS
        )

        logger.warn("Invalid verification code for phone: ${sendPhoneVerificationCode.maskPhoneNumber(sanitizedPhone)}")
        throw InvalidVerificationCodeException("Invalid verification code.")
    }
}