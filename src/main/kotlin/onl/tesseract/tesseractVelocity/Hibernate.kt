package onl.tesseract.tesseractVelocity

import onl.tesseract.tesseractVelocity.config.DatabaseConfig
import onl.tesseract.tesseractVelocity.repository.admin.entity.BanEntity
import onl.tesseract.tesseractVelocity.repository.admin.entity.KickEntity
import onl.tesseract.tesseractVelocity.repository.admin.entity.MuteEntity
import onl.tesseract.tesseractVelocity.repository.admin.entity.PlayerEntity
import org.hibernate.SessionFactory
import org.hibernate.boot.MetadataSources
import org.hibernate.boot.registry.StandardServiceRegistryBuilder

object Hibernate {
    lateinit var sessionFactory: SessionFactory

    fun init(config: DatabaseConfig) {
        val registry = StandardServiceRegistryBuilder()
                .applySetting("hibernate.connection.driver_class", config.driver)
                .applySetting("hibernate.connection.url", config.url)
                .applySetting("hibernate.connection.username", config.user)
                .applySetting("hibernate.connection.password", config.password)
                .applySetting("hibernate.hbm2ddl.auto", "validate")
                .applySetting("hibernate.dialect", config.dialect)
                .applySetting("hibernate.show_sql", "true")
                .applySetting("hibernate.models.jpa-compliant", "true")
                .build()

        sessionFactory = MetadataSources(registry)
                .addAnnotatedClass(BanEntity::class.java)
                .addAnnotatedClass(MuteEntity::class.java)
                .addAnnotatedClass(PlayerEntity::class.java)
                .addAnnotatedClass(KickEntity::class.java)
                .buildMetadata()
                .buildSessionFactory()
    }

    fun <T> inTransaction(action: (session: org.hibernate.Session) -> T): T {
        val session = sessionFactory.openSession()
        val transaction = session.beginTransaction()
        return try {
            val result = action(session)
            transaction.commit()
            result
        } catch (ex: Exception) {
            transaction.rollback()
            throw ex
        } finally {
            session.close()
        }
    }
}