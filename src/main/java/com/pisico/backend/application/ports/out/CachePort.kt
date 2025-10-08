package com.pisico.backend.application.ports.out

interface CachePort {
    fun save(key: String, value: String, ttlSeconds: Long)
    fun get(key: String): String?
    fun delete(key: String)
    fun exists(key: String): Boolean
    fun increment(key: String, ttlSeconds: Long): Long
}