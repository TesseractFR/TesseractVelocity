package onl.tesseract.tesseractVelocity.repository.vote.entity

import VoteSiteEntity
import jakarta.persistence.*
import org.hibernate.annotations.ColumnDefault
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.Instant

@Entity
@Table(name = "t_vote_buffer")
open class VoteBufferEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    open var id: Int? = null,

    @Column(name = "pseudo", nullable = false, length = 64)
    open var pseudo: String = "",

    @ColumnDefault("current_timestamp()")
    @Column(name = "date", nullable = false)
    open var date: Instant = Instant.now(),

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "service_name", nullable = false)
    open var serviceName: VoteSiteEntity
)
