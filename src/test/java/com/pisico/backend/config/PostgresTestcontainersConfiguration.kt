package com.pisico.backend.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

@TestConfiguration(proxyBeanMethods = false)
open class PostgresTestcontainersConfiguration {

    companion object {
        private val postgresContainer = PostgreSQLContainer(DockerImageName.parse("postgres:13")).apply {
            withDatabaseName("pisico-backend")
            withUsername("pisico-backend")
            withPassword("pisico-backend")
            withReuse(false)
            start()
        }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgresContainer::getJdbcUrl)
            registry.add("spring.datasource.username", postgresContainer::getUsername)
            registry.add("spring.datasource.password", postgresContainer::getPassword)
            registry.add("spring.datasource.driver-class-name") { "org.postgresql.Driver" }
        }
    }
}

