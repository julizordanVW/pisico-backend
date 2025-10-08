package com.pisico.backend.infraestructure.out

import com.pisico.backend.application.ports.out.CachePort
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RedisCacheAdapter(
    private val redisTemplate: RedisTemplate<String, String>
) : CachePort {

    override fun save(key: String, value: String, ttlSeconds: Long) {
        redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(ttlSeconds))
    }

    override fun get(key: String): String? {
        return redisTemplate.opsForValue().get(key)
    }

    override fun delete(key: String) {
        redisTemplate.delete(key)
    }

    override fun exists(key: String): Boolean {
        return redisTemplate.hasKey(key) ?: false
    }

    override fun increment(key: String, ttlSeconds: Long): Long {
        val value = redisTemplate.opsForValue().increment(key) ?: 1L

        if (value == 1L) {
            redisTemplate.expire(key, Duration.ofSeconds(ttlSeconds))
        }

        return value
    }
}