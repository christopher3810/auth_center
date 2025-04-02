package com.auth.integration.container

import com.auth.annotation.IntegrationTest
import com.auth.config.container.PostgresqlTestContainer
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.springframework.jdbc.core.JdbcTemplate
import javax.sql.DataSource

@IntegrationTest
class PostgresqlContainerTest(
    private val dataSource: DataSource,
) : DescribeSpec({

        describe("PostgreSQL 테스트 컨테이너는") {
            context("싱글톤 인스턴스로 관리될 때") {
                it("동일한 인스턴스를 반환해야 한다") {
                    val instance1 = PostgresqlTestContainer.instance
                    val instance2 = PostgresqlTestContainer.instance

                    instance1 shouldBe instance2
                }

                it("컨테이너가 실행 중이어야 한다") {
                    PostgresqlTestContainer.instance.isRunning shouldBe true
                }
            }

            context("컨테이너 설정이 적용될 때") {
                it("데이터베이스 접속 정보가 올바르게 설정되어야 한다") {
                    val container = PostgresqlTestContainer.instance
                    container.databaseName shouldBe "testdb"
                    container.username shouldBe "test"
                    container.password shouldBe "test"
                    container.jdbcUrl shouldContain "jdbc:postgresql"
                }
            }

            context("데이터베이스가 정상적으로 동작할 때") {
                it("쿼리를 실행할 수 있어야 한다") {
                    val jdbcTemplate = JdbcTemplate(dataSource)
                    val result =
                        jdbcTemplate.queryForObject(
                            "SELECT 1",
                            Int::class.java,
                        )
                    result shouldBe 1
                }

                it("테이블을 생성하고 조회할 수 있어야 한다") {
                    val jdbcTemplate = JdbcTemplate(dataSource)
                    jdbcTemplate.execute(
                        """
                    CREATE TABLE test_container_check (
                        id SERIAL PRIMARY KEY,
                        test_column VARCHAR(255)
                    )
                """,
                    )

                    val tableExists =
                        jdbcTemplate.queryForObject(
                            """
                    SELECT EXISTS (
                        SELECT FROM information_schema.tables 
                        WHERE table_name = 'test_container_check'
                    )
                """,
                            Boolean::class.java,
                        )

                    tableExists shouldBe true
                }
            }
        }
    })
