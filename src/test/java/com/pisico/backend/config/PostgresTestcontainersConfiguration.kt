package com.pisico.backend.config

import org.flywaydb.core.Flyway
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
abstract class AbstractIntegrationTest {

    companion object {
        private val postgres = PostgresTestContainer.getInstance()
        private var flywayInitialized = false

        @DynamicPropertySource
        @JvmStatic
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
            registry.add("spring.datasource.driver-class-name") { "org.postgresql.Driver" }

            if (!flywayInitialized) {
                initializeFlyway()
                flywayInitialized = true
            }
        }

        private fun initializeFlyway() {
            val flyway = Flyway.configure()
                .dataSource(postgres.jdbcUrl, postgres.username, postgres.password)
                .locations("classpath:db/migration")
                .load()

            flyway.migrate()
        }
    }

    object PostgresTestContainer {

        private val container = PostgreSQLContainer("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass")
            .withReuse(true)

        fun getInstance(): PostgreSQLContainer<*> {
            if (!container.isRunning) {
                container.start()
            }
            return container
        }

        init {
            Runtime.getRuntime().addShutdownHook(Thread {
                if (container.isRunning) {
                    container.stop()
                }
            })
        }
    }
}