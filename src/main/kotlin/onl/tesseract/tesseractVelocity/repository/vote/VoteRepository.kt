package onl.tesseract.tesseractVelocity.repository.vote

import VoteSiteEntity
import onl.tesseract.tesseractVelocity.Hibernate
import onl.tesseract.tesseractVelocity.repository.vote.entity.VoteBufferEntity
import onl.tesseract.tesseractVelocity.repository.vote.entity.VoteEntity
import onl.tesseract.tesseractVelocity.repository.vote.entity.VotePointEntity
import java.util.*

class VoteRepository {
    fun getPlayerBuffer(playerName: String): List<VoteBufferEntity> {
        return Hibernate.inTransaction { session ->
            session.createQuery("FROM VoteBufferEntity WHERE pseudo = :playerName", VoteBufferEntity::class.java)
                    .apply {
                        setParameter("playerName", playerName)
                    }
                    .list()
        }
    }

    fun getPlayerVotes(playerUuid: UUID): List<VoteEntity> {
        return Hibernate.inTransaction { session ->
            session.createQuery("FROM VoteEntity WHERE playerUuid = :playerUuid", VoteEntity::class.java)
                    .apply {
                        setParameter("playerUuid", playerUuid)
                    }
                    .list()
        }
    }

    fun getPlayerLastVotes(playerUuid: UUID): List<VoteEntity> {
        return Hibernate.inTransaction { session ->
            session.createQuery(
                "FROM VoteEntity v WHERE v.playerUuid = :playerUuid AND v.date = (" +
                        "SELECT MAX(v2.date) FROM VoteEntity v2 WHERE v2.playerUuid = v.playerUuid AND v2.service = v.service" +
                        ")", VoteEntity::class.java)
                    .apply {
                        setParameter("playerUuid", playerUuid)
                    }
                    .list()
        }
    }

    fun registerVote(vote: VoteEntity){
        Hibernate.inTransaction { session ->
            session.persist(vote)
        }
    }

    fun clearBuffer(playerName: String) {
        Hibernate.inTransaction { session ->
            getPlayerBuffer(playerName).forEach {
                session.remove(it)
            }
        }
    }

    fun addPoint(playerUUID: UUID) {
        Hibernate.inTransaction { session ->
            session.createQuery("INSERT INTO VotePointEntity (playerUuid, amount) VALUES (:playerUuid, 1) " +
                    "ON CONFLICT DO UPDATE SET amount = amount + 1", VotePointEntity::class.java)
                    .setParameter("playerUuid",playerUUID)
                    .executeUpdate()
            }
        }

    fun getVoteService(serviceName: String): VoteSiteEntity {
        return Hibernate.inTransaction { session ->
                session.createQuery("FROM VoteSiteEntity WHERE serviceName = :service",VoteSiteEntity::class.java)
                        .setParameter("service",serviceName)
                        .singleResult
        }
    }

    fun registerBufferVote(voteBuffer: VoteBufferEntity) {
        Hibernate.inTransaction { session ->
            session.persist(voteBuffer)
        }
    }


}
