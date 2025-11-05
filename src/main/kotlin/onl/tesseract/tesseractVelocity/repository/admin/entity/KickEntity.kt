package onl.tesseract.tesseractVelocity.repository.admin.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.ColumnDefault
import java.time.Instant

@Entity
@Table(name = "t_admin_kick")
data class KickEntity (
    @Id
    @Column(name = "id", nullable = false)
    var id: Int? = null,

    @Column(name = "uuid", nullable = false, length = 100)
    var uuid: String? = null,

    @Column(name = "staff", nullable = false, length = 30)
    var kickStaff: String? = null,

    @Column(name = "reason", length = 100)
    var kickReason: String? = null,

    @Column(name = "server", nullable = false, length = 30)
    var kickServer: String? = null,

    @ColumnDefault("current_timestamp()")
    @Column(name = "date", nullable = false)
    var kickDate: Instant? = null
){}