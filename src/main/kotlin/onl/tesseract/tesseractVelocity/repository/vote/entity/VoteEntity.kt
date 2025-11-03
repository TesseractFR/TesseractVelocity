package onl.tesseract.tesseractVelocity.repository.vote.entity

import VoteSiteEntity
import jakarta.persistence.*
import onl.tesseract.tesseractVelocity.domain.vote.Vote
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
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

    @Column(name = "player_uuid", nullable = false, length = 36)
    open var playerUuid: UUID,

    @Column(name = "date", nullable = false)
    open var date: Instant,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "service_name", nullable = false)
    open var service: VoteSiteEntity
){

}

fun Vote.toEntity(): VoteEntity {
    return VoteEntity(
        playerUuid =this.playerUuid,
        date = this.date,
        service = this.service.toEntity())
}