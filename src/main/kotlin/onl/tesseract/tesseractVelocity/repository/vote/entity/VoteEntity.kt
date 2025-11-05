package onl.tesseract.tesseractVelocity.repository.vote.entity

import VoteSiteEntity
import jakarta.persistence.*
import onl.tesseract.tesseractVelocity.domain.vote.Vote
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.hibernate.type.SqlTypes
import toEntity
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "t_vote")
open class VoteEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    open var id: Int = 0,

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "player_uuid", nullable = false, length = 36)
    open var playerUuid: UUID = UUID.randomUUID(),

    @Column(name = "date", nullable = false)
    open var date: Instant = Instant.now(),

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "service_name", nullable = false)
    open var service: VoteSiteEntity = VoteSiteEntity()
){

}

fun Vote.toEntity(): VoteEntity {
    return VoteEntity(
        playerUuid =this.playerUuid,
        date = this.date,
        service = this.service.toEntity())
}