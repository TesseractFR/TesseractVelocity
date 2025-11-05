package onl.tesseract.tesseractVelocity.repository.admin


import onl.tesseract.tesseractVelocity.Hibernate
import onl.tesseract.tesseractVelocity.domain.admin.Ban
import onl.tesseract.tesseractVelocity.domain.admin.Mute
import onl.tesseract.tesseractVelocity.domain.admin.PlayerInfo
import onl.tesseract.tesseractVelocity.domain.admin.Sanctions
import onl.tesseract.tesseractVelocity.repository.admin.entity.BanEntity
import onl.tesseract.tesseractVelocity.repository.admin.entity.KickEntity
import onl.tesseract.tesseractVelocity.repository.admin.entity.MuteEntity
import onl.tesseract.tesseractVelocity.repository.admin.entity.PlayerEntity
import onl.tesseract.tesseractVelocity.repository.admin.entity.toEntity
import onl.tesseract.tesseractVelocity.utils.UUIDUtil.uuidToString
import java.time.Instant
import java.util.*


const val globalServer = "(global)"

class AdminRepository() {

    fun getActiveMute(uuid: UUID?, ip: String?, server: String?): Mute? {
        return Hibernate.inTransaction { session ->
            val query = when {
                uuid != null -> session.createQuery(
                    "FROM MuteEntity WHERE uuid = :uuid AND (muteServer = :server OR muteServer = '$globalServer') AND muteState = true",
                    MuteEntity::class.java
                ).apply {
                    setParameter("uuid", uuidToString(uuid))
                    setParameter("server", server ?: globalServer)
                }
                ip != null -> session.createQuery(
                    "FROM MuteEntity WHERE muteIp = :ip AND (muteServer = :server OR muteServer = '$globalServer') AND muteState = true",
                    MuteEntity::class.java
                ).apply {
                    setParameter("ip", ip)
                    setParameter("server", server ?: globalServer)
                }
                else -> return@inTransaction null
            }

            query.resultList.firstOrNull { it.toModel()?.isActive() == true }?.toModel()
        }
    }

    fun updateMute(mute: Mute): Boolean {
        return try {
            Hibernate.inTransaction { session ->
                val entity = mute.toEntity()
                if (entity.id != null) {
                    session.merge(entity)
                    true
                } else {
                    false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun listActiveBans(server: String?): List<Ban> {
        return Hibernate.inTransaction { session ->
            val query = if (server == null) {
                session.createQuery(
                    "FROM BanEntity WHERE state = true",
                    BanEntity::class.java
                )
            } else {
                session.createQuery(
                    "FROM BanEntity WHERE state = true AND (server = :server OR server = '$globalServer')",
                    BanEntity::class.java
                ).apply { setParameter("server", server) }
            }
            query.list().mapNotNull { it.toModel() }.filter { it.isActive() }
        }
    }

    fun listActiveMutes(server: String?): List<Mute> {
        return Hibernate.inTransaction { session ->
            val query = if (server == null) {
                session.createQuery(
                    "FROM MuteEntity WHERE muteState = true",
                    MuteEntity::class.java
                )
            } else {
                session.createQuery(
                    "FROM MuteEntity WHERE muteState = true AND (muteServer = :server OR muteServer = '$globalServer')",
                    MuteEntity::class.java
                ).apply { setParameter("server", server) }
            }
            query.list().mapNotNull { it.toModel() }.filter { it.isActive() }
        }
    }

    fun insertKick(uuid: UUID, staff: String, reason: String?, server: String): Boolean {
        return try {
            Hibernate.inTransaction { session ->
                val entity = KickEntity(
                    id = null,
                    uuid = uuidToString(uuid),
                    kickStaff = staff,
                    kickReason = reason,
                    kickServer = server,
                    kickDate = Instant.now()
                )
                session.persist(entity)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun findBansByUuidOrIp(uuid: UUID?, ip: String?): List<Ban> {
        return Hibernate.inTransaction { session ->
            val query = when {
                uuid != null -> session.createQuery(
                    "FROM BanEntity WHERE uuid = :uuid",
                    BanEntity::class.java
                ).apply { setParameter("uuid", uuidToString(uuid)) }
                ip != null -> session.createQuery(
                    "FROM BanEntity WHERE ip = :ip",
                    BanEntity::class.java
                ).apply { setParameter("ip", ip) }
                else -> return@inTransaction emptyList()
            }
            query.list().mapNotNull { it.toModel() }
        }
    }

    fun findMutesByUuidOrIp(uuid: UUID?, ip: String?): List<Mute> {
        return Hibernate.inTransaction { session ->
            val query = when {
                uuid != null -> session.createQuery(
                    "FROM MuteEntity WHERE uuid = :uuid",
                    MuteEntity::class.java
                ).apply { setParameter("uuid", uuidToString(uuid)) }
                ip != null -> session.createQuery(
                    "FROM MuteEntity WHERE muteIp = :ip",
                    MuteEntity::class.java
                ).apply { setParameter("ip", ip) }
                else -> return@inTransaction emptyList()
            }
            query.list().mapNotNull { it.toModel() }
        }
    }

    fun getActiveBan(uuid: UUID?, ip: String?, server: String?): Ban? {
        return Hibernate.inTransaction { session ->
            val query = when {
                uuid != null -> session.createQuery(
                    "FROM BanEntity WHERE uuid = :uuid AND (server = :server OR server = '$globalServer') AND state = true",
                    BanEntity::class.java
                ).apply {
                    setParameter("uuid", uuidToString(uuid))
                    setParameter("server", server)
                }
                ip != null -> session.createQuery(
                    "FROM BanEntity WHERE ip = :ip AND (server = :server OR server = '$globalServer') AND state = true",
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

    fun updateBan(ban: Ban): Boolean {
        return try {
            Hibernate.inTransaction { session ->
                val entity = ban.toEntity()
                if (entity.id != null) {
                    session.merge(entity)
                    true
                } else {
                    false
                }
            }
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

    fun insertMute(mute: Mute): Boolean {
        return try {
            Hibernate.inTransaction { session ->
                session.persist(mute.toEntity())
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun updatePlayer(playerEntity: PlayerEntity) {
        Hibernate.inTransaction { session ->
            session.merge(playerEntity)
        }
    }

    fun findPlayerByUuid(uuid: UUID) : PlayerEntity?{
        return Hibernate.inTransaction { session ->
            session.find(PlayerEntity::class.java,uuidToString(uuid))
        }
    }
}