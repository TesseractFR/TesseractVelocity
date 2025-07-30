package onl.tesseract.tesseractVelocity.repository.admin


import onl.tesseract.tesseractVelocity.Hibernate
import onl.tesseract.tesseractVelocity.domain.admin.Ban
import onl.tesseract.tesseractVelocity.domain.admin.PlayerInfo
import onl.tesseract.tesseractVelocity.domain.admin.Sanctions
import onl.tesseract.tesseractVelocity.repository.admin.entity.BanEntity
import onl.tesseract.tesseractVelocity.repository.admin.entity.MuteEntity
import onl.tesseract.tesseractVelocity.repository.admin.entity.PlayerEntity
import onl.tesseract.tesseractVelocity.repository.admin.entity.toEntity
import onl.tesseract.tesseractVelocity.utils.UUIDUtil.uuidToString
import java.util.*


const val globalServer = "(global)"

class AdminRepository() {

    fun getActiveBan(uuid: UUID?, ip: String?, server: String?): Ban? {
        return Hibernate.inTransaction { session ->
            val query = when {
                uuid != null -> session.createQuery(
                    "FROM BanEntity WHERE uuid = :uuid AND (server = :server OR server IS NULL) AND state = true",
                    BanEntity::class.java
                ).apply {
                    setParameter("uuid", uuidToString(uuid))
                    setParameter("server", server)
                }
                ip != null -> session.createQuery(
                    "FROM BanEntity WHERE ip = :ip AND (server = :server OR server IS NULL) AND state = true",
                    BanEntity::class.java
                ).apply {
                    setParameter("ip", ip)
                    setParameter("server", server)
                }
                else -> return@inTransaction null
            }

            query.resultList.firstOrNull { it.toModel()?.isActive() == true }?.toModel()
        }
    }

    fun insertBan(ban: Ban): Boolean {
        return try {
            Hibernate.inTransaction { session ->
                session.persist(ban.toEntity())
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun findPlayerByName(name: String): PlayerInfo? {
        return Hibernate.inTransaction { session ->
            session.createQuery(
                "FROM PlayerEntity WHERE batPlayer = :name",
                PlayerEntity::class.java
            ).setParameter("name", name).uniqueResult().toModel()
        }
    }

    fun findPlayersByIp(ip: String): List<PlayerInfo> {
        return Hibernate.inTransaction { session ->
            session.createQuery(
                "FROM PlayerEntity WHERE lastip = :ip",
                PlayerEntity::class.java
            ).setParameter("ip", ip).list().map { it.toModel() }
        }
    }

    fun isPlayerMuted(uuid: UUID): Boolean {
        return Hibernate.inTransaction { session ->
            session.createQuery(
                "FROM MuteEntity WHERE uuid = :uuid AND muteState = true",
                MuteEntity::class.java
            ).setParameter("uuid", uuidToString(uuid)).uniqueResult() != null
        }
    }

    fun countTotalSanctions(uuid: UUID): Sanctions {
        return Hibernate.inTransaction { session ->
            val bans = session.createQuery(
                "SELECT COUNT(*) FROM BanEntity WHERE uuid = :uuid",
                Long::class.java
            ).setParameter("uuid", uuidToString(uuid)).uniqueResult() ?: 0

            val mutes = session.createQuery(
                "SELECT COUNT(*) FROM MuteEntity WHERE uuid = :uuid",
                Long::class.java
            ).setParameter("uuid", uuidToString(uuid)).uniqueResult() ?: 0

            val kicks = session.createQuery(
                "SELECT COUNT(*) FROM KickEntity WHERE uuid = :uuid",
                Long::class.java
            ).setParameter("uuid", uuidToString(uuid)).uniqueResult() ?: 0

            Sanctions(bans.toInt(), mutes.toInt(), kicks.toInt())
        }
    }

    fun countSanctionsForIp(ip: String): Sanctions {
        return Hibernate.inTransaction { session ->
            val bans = session.createQuery(
                "SELECT COUNT(*) FROM BanEntity WHERE ip = :ip AND state = true",
                Long::class.java
            ).setParameter("ip", ip).uniqueResult() ?: 0

            val mutes = session.createQuery(
                "SELECT COUNT(*) FROM MuteEntity WHERE muteIp = :ip AND muteState = true",
                Long::class.java
            ).setParameter("ip", ip).uniqueResult() ?: 0

            val kicks = session.createQuery(
                "SELECT COUNT(*) FROM KickEntity WHERE uuid IN " +
                        "(SELECT uuid FROM PlayerEntity WHERE lastip = :ip)",
                Long::class.java
            ).setParameter("ip", ip).uniqueResult() ?: 0

            Sanctions(bans.toInt(), mutes.toInt(), kicks.toInt())
        }
    }



}