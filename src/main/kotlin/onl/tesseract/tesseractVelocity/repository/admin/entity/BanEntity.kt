@file:OptIn(ExperimentalTime::class)

package onl.tesseract.tesseractVelocity.repository.admin.entity

import jakarta.persistence.*
import onl.tesseract.tesseractVelocity.domain.admin.Ban
import org.hibernate.annotations.ColumnDefault
import kotlin.time.ExperimentalTime

@Entity
@Table(name = "BAT_ban")
data class BanEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ban_id", nullable = false)
    var id: Int? = null,

    @Column(name = "UUID", length = 100)
    var uuid: String? = null,

    @Column(name = "ban_ip", length = 50)
    var ip: String? = null,

    @Column(name = "ban_staff", nullable = false, length = 30)
    var staff: String? = null,

    @Column(name = "ban_reason", length = 100)
    var reason: String? = null,

    @Column(name = "ban_server", nullable = false, length = 30)
    var server: String? = null,

    @ColumnDefault("current_timestamp()")
    @Column(name = "ban_begin", nullable = false)
    var begin: java.time.Instant? = null,

    @Column(name = "ban_end")
    var end: java.time.Instant? = null,

    @ColumnDefault("1")
    @Column(name = "ban_state", nullable = false)
    var state: Boolean? = false,

    @Column(name = "ban_unbandate")
    var unbandate: java.time.Instant? = null,

    @Column(name = "ban_unbanstaff", length = 30)
    var unbanstaff: String? = null,

    @Column(name = "ban_unbanreason", length = 100)
    var unbanreason: String? = null,
) {
    fun toModel(): Ban? {
        return Ban(
            id,
            uuid,
            ip,
            staff,
            reason,
            server = if (this.server == "(global)") null else this.server,
            begin,
            end,
            state,
            unbandate,
            unbanstaff,
            unbanreason)
    }

}

fun Ban.toEntity(): BanEntity = BanEntity(
    id = this.id,
    uuid = this.uuid,
    ip = this.ip,
    staff = this.staff,
    reason = this.reason,
    server = this.server ?: "(global)",
    begin = this.begin,
    end = this.end,
    state = this.state,
    unbandate = this.unbandate,
    unbanstaff = this.unbanstaff,
    unbanreason = this.unbanreason
)

