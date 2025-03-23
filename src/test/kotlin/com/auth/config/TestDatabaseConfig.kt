package com.auth.config

import com.auth.config.container.PostgresqlTestContainer
import com.zaxxer.hikari.HikariDataSource
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import javax.sql.DataSource

@TestConfiguration
class TestDatabaseConfig {

    @Bean
    fun dataSource(): DataSource {
        val container = PostgresqlTestContainer.instance
        return HikariDataSource().apply {
            driverClassName = "org.postgresql.Driver"
            jdbcUrl = container.jdbcUrl
            username = container.username
            password = container.password
            maximumPoolSize = 5
            minimumIdle = 1
            idleTimeout = 30000
            connectionTimeout = 20000
            maxLifetime = 1200000
        }
    }

    companion object {
        init {
            // 컨테이너 시작 로직을 한 번만 실행
            val container = PostgresqlTestContainer.instance
            if (!container.isRunning) {
                container.start()
            }
        }

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            val container = PostgresqlTestContainer.instance
            if (!container.isRunning) {
                throw IllegalStateException("PostgreSQL container is not running!")
            }
            registry.add("spring.jpa.hibernate.ddl-auto") { "create-drop" }
            registry.add("spring.jpa.show-sql") { "true" }
            registry.add("spring.jpa.properties.hibernate.format_sql") { "true" }
        }
    }
}
