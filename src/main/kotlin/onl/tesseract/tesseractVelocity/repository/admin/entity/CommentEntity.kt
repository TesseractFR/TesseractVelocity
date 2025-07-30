package onl.tesseract.tesseractVelocity.repository.admin.entity

import jakarta.persistence.*
import org.hibernate.annotations.ColumnDefault
import java.time.Instant

@Entity
@Table(name = "bat_comments")
data class CommentEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Int? = null,

    @Column(name = "entity", nullable = false, length = 100)
     var entity: String? = null,

    @Column(name = "note", nullable = false)
     var note: String? = null,

    @Column(name = "type", nullable = false, length = 7)
     var type: String? = null,

    @Column(name = "staff", nullable = false, length = 30)
     var staff: String? = null,

    @ColumnDefault("current_timestamp()")
    @Column(name = "date", nullable = false)
     var date: Instant? = null,

    @Column(name = "notified", nullable = false)
     var notified: Boolean? = false
)