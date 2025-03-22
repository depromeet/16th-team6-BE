package com.deepromeet.atcha.support

import jakarta.persistence.EntityManager
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.springframework.context.ApplicationContext
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.support.TransactionTemplate

class DatabaseCleanerExtension : BeforeEachCallback {
    override fun beforeEach(extensionContext: ExtensionContext?) {
        val context = extensionContext?.let { SpringExtension.getApplicationContext(extensionContext) }
        context?.let { cleanup(it) }
    }

    private fun cleanup(context: ApplicationContext) {
        val em = context.getBean(EntityManager::class.java)
        val tt = context.getBean(TransactionTemplate::class.java)

        tt.execute { action ->
            em.clear()
            truncateTable(em)
        }
    }

    private fun truncateTable(em: EntityManager) {
        em.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate()
        for (tableName in findTableNames(em)) {
            em.createNativeQuery("TRUNCATE TABLE $tableName RESTART IDENTITY").executeUpdate()
        }
        em.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate()
    }

    private fun findTableNames(em: EntityManager): List<String> {
        val query =
            """
            SELECT TABLE_NAME
            FROM INFORMATION_SCHEMA.TABLES
            WHERE TABLE_SCHEMA = 'PUBLIC'
            """.trimIndent()

        return em.createNativeQuery(query)
            .resultList
            .map { it.toString() }
    }
}
