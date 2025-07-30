package onl.tesseract.tesseractVelocity.repository.admin.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.ColumnDefault
import java.time.Instant

@Entity
@Table(name = "BAT_mute")
data class MuteEntity(
    @Id
    @Column(name = "mute_id", nullable = false)
    var id: Int? = null,

    @Column(name = "UUID", length = 100)
    var uuid: String? = null,

    @Column(name = "mute_ip", length = 50)
    var muteIp: String? = null,

    @Column(name = "mute_staff", nullable = false, length = 30)
    var muteStaff: String? = null,

    @Column(name = "mute_reason", length = 100)
    var muteReason: String? = null,

    @Column(name = "mute_server", nullable = false, length = 30)
    var muteServer: String? = null,

    @ColumnDefault("current_timestamp()")
    @Column(name = "mute_begin", nullable = false)
    var muteBegin: Instant? = null,

    @Column(name = "mute_end")
    var muteEnd: Instant? = null,

    @ColumnDefault("1")
    @Column(name = "mute_state", nullable = false)
    var muteState: Boolean? = false,

    @Column(name = "mute_unmutedate")
    var muteUnmutedate: Instant? = null,

    @Column(name = "mute_unmutestaff", length = 30)
    var muteUnmutestaff: String? = null,

    @Column(name = "mute_unmutereason", length = 100)
    var muteUnmutereason: String? = null,
) 