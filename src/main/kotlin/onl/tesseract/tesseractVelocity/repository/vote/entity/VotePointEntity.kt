package onl.tesseract.tesseractVelocity.repository.vote.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.ColumnDefault

@Entity
@Table(name = "t_vote_points")
open class VotePointEntity {
    @Id
    @Column(name = "player_uuid", nullable = false, length = 36)
    open var playerUuid: String? = null

    @ColumnDefault("0")
    @Column(name = "amount")
    open var amount: Int? = null
}