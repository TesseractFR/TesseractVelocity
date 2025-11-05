package onl.tesseract.tesseractVelocity.repository.admin.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import onl.tesseract.tesseractVelocity.domain.admin.Mute
import org.hibernate.annotations.ColumnDefault
import java.time.Instant

@Entity
@Table(name = "t_admin_mute")
data class MuteEntity(
    @Id
    @Column(name = "id", nullable = false)
    var id: Int? = null,

    @Column(name = "uuid", length = 100)
    var uuid: String? = null,

    @Column(name = "ip", length = 50)
    var muteIp: String? = null,

    @Column(name = "staff", nullable = false, length = 30)
    var muteStaff: String? = null,

    @Column(name = "reason", length = 100)
    var muteReason: String? = null,

    @Column(name = "server", nullable = false, length = 30)
    var muteServer: String? = null,

    @ColumnDefault("current_timestamp()")
    @Column(name = "begin", nullable = false)
    var muteBegin: Instant? = null,

    @Column(name = "end")
    var muteEnd: Instant? = null,

    @ColumnDefault("1")
    @Column(name = "state", nullable = false)
    var muteState: Boolean? = false,

    @Column(name = "unmutedate")
    var muteUnmutedate: Instant? = null,

    @Column(name = "unmutestaff", length = 30)
    var muteUnmutestaff: String? = null,

    @Column(name = "unmutereason", length = 100)
    var muteUnmutereason: String? = null,
) {
    fun toModel(): Mute? {
        return Mute(
            id,
            uuid,
            muteIp,
            muteStaff,
            muteReason,
            muteServer = if (this.muteServer == "(global)") null else this.muteServer,
            muteBegin,
            muteEnd,
            muteState,
            muteUnmutedate,
            muteUnmutestaff,
            muteUnmutereason
        )
    }
}

fun Mute.toEntity(): MuteEntity = MuteEntity(
    id = this.id,
    uuid = this.uuid,
    muteIp = this.muteIp,
    muteStaff = this.muteStaff,
    muteReason = this.muteReason,
    muteServer = this.muteServer ?: "(global)",
    muteBegin = this.muteBegin,
    muteEnd = this.muteEnd,
    muteState = this.muteState,
    muteUnmutedate = this.muteUnmutedate,
    muteUnmutestaff = this.muteUnmutestaff,
    muteUnmutereason = this.muteUnmutereason
)