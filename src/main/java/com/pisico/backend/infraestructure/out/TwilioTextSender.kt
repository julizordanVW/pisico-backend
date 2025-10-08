package com.pisico.backend.infraestructure.out

import com.pisico.backend.application.ports.out.TextSender
import com.twilio.Twilio
import com.twilio.rest.api.v2010.account.Message
import com.twilio.type.PhoneNumber
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class TwilioTextSender(
    @Value("\${twilio.account-sid}") private val accountSid: String,
    @Value("\${twilio.auth-token}") private val authToken: String,
    @Value("\${twilio.phone-number}") private val twilioPhoneNumber: String
) : TextSender {

    companion object {
        private val logger = LoggerFactory.getLogger(TwilioTextSender::class.java)

        private val messages = mapOf(
            "en" to "Pisico: Your verification code is: %s.",
            "es" to "Pisico: Tu código de verificación es: %s.",
            "fr" to "Pisico:Votre code de vérification est: %s.",
            "de" to "Pisico: Ihr Bestätigungscode lautet: %s.",
            "it" to "Pisico: Il tuo codice di verifica è: %s.",
            "pt" to "Pisico: Seu código de verificação é: %s"
        )
    }

    @PostConstruct
    fun init() {
        Twilio.init(accountSid, authToken)
        logger.info("Twilio initialized successfully")
    }

    override fun sendText(language: String, phoneNumber: String, code: String) {
        try {
            val messageTemplate = messages[language.lowercase()] ?: messages["en"]!!
            val messageBody = String.format(messageTemplate, code)

            val message = Message.creator(
                PhoneNumber(phoneNumber),
                PhoneNumber(twilioPhoneNumber),
                messageBody
            ).create()

            logger.info("SMS sent successfully. SID: ${message.sid}")
        } catch (e: Exception) {
            logger.error("Failed to send SMS via Twilio to $phoneNumber", e)
            throw SmsDeliveryException("Failed to send verification code", e)
        }
    }
}

class SmsDeliveryException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)