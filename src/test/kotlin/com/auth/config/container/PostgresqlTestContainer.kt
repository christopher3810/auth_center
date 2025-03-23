package com.auth.config.container

import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

object PostgresqlTestContainer {
    private const val DATABASE_NAME = "testdb"
    private const val USERNAME = "test"
    private const val PASSWORD = "test"
    
    val instance: PostgreSQLContainer<*> by lazy {
        PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine")).apply {
            withDatabaseName(DATABASE_NAME)
            withUsername(USERNAME)
            withPassword(PASSWORD)
            // 컨테이너 재사용은 환경변수로 제어
            withReuse(System.getProperty("testcontainers.reuse.enable")?.toBoolean() ?: false)
            withInitScript("sql/init.sql")
        }.also { container ->
            Runtime.getRuntime().addShutdownHook(Thread {
                if (container.isRunning) {
                    container.stop()
                }
            })
        }
    }

    fun reset() {
        if (instance.isRunning) {
            try {
                instance.createConnection("").use { connection ->
                    connection.createStatement().execute(
                        """
                    DROP SCHEMA public CASCADE;
                    CREATE SCHEMA public;
                    GRANT ALL ON SCHEMA public TO $USERNAME;
                    GRANT ALL ON SCHEMA public TO public;
                    """.trimIndent()
                    )
                }
                // 초기화 스크립트 경로를 상수로 관리
                val initScriptPath = "/testinitscript.sql"
                val result = instance.execInContainer("psql", "-U", USERNAME, "-d", DATABASE_NAME, "-f", initScriptPath)
                if (result.exitCode != 0) {
                    throw IllegalStateException("Database reset failed: ${result.stderr}")
                }
            } catch (ex: Exception) {
                // 로깅 및 재처리 로직 추가 (로깅 라이브러리 사용 권장)
                throw IllegalStateException("Failed to reset database", ex)
            }
        }
    }

} 