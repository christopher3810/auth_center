package com.auth.config

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.test.context.TestContext
import org.springframework.test.context.TestExecutionListener
import org.springframework.test.context.support.AbstractTestExecutionListener
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

private val logger = KotlinLogging.logger {}

class TestTransactionEventListener : AbstractTestExecutionListener() {

    override fun getOrder(): Int = 2000

    override fun beforeTestClass(testContext: TestContext) {
        logger.info { "테스트 클래스 시작: ${testContext.testClass.simpleName}" }
    }

    override fun afterTestClass(testContext: TestContext) {
        logger.info { "테스트 클래스 종료 및 롤백 시작: ${testContext.testClass.simpleName}" }
        
        val transactionManager = testContext.applicationContext.getBean(PlatformTransactionManager::class.java)
        TransactionTemplate(transactionManager).execute { status ->
            status.setRollbackOnly()
            logger.info { "테스트 데이터 롤백 완료: ${testContext.testClass.simpleName}" }
            null
        }
    }

    override fun beforeTestMethod(testContext: TestContext) {
        logger.info { "테스트 메소드 시작: ${testContext.testMethod.name}" }
    }

    override fun afterTestMethod(testContext: TestContext) {
        logger.info { "테스트 메소드 종료: ${testContext.testMethod.name}" }
    }
} 