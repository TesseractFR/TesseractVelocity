@file:OptIn(ExperimentalTime::class)

package onl.tesseract.tesseractVelocity.repository.admin.entity

import jakarta.persistence.*
import onl.tesseract.tesseractVelocity.domain.admin.Ban
import onl.tesseract.tesseractVelocity.utils.UUIDUtil
import org.hibernate.annotations.ColumnDefault
import org.hibernate.annotations.JdbcTypeCode
import java.sql.Types
import kotlin.time.ExperimentalTime

@Entity
@Table(name = "t_admin_ban")
data class BanEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Int? = null,

    @Column(name = "uuid", length = 36, columnDefinition = "VARCHAR(36)")
    @JdbcTypeCode(Types.VARCHAR)
    var uuid: String? = null,

    @Column(name = "ip", length = 50)
    var ip: String? = null,

    @Column(name = "staff", nullable = false, length = 30)
    var staff: String? = null,

    @Column(name = "reason", length = 100)
    var reason: String? = null,

    @Column(name = "server", nullable = false, length = 30)
    var server: String? = null,

    @ColumnDefault("current_timestamp()")
    @Column(name = "begin", nullable = false)
    var begin: java.time.Instant? = null,

    @Column(name = "end")
    var end: java.time.Instant? = null,

    @ColumnDefault("1")
    @Column(name = "state", nullable = false)
    var state: Boolean? = false,

    @Column(name = "unbandate")
    var unbandate: java.time.Instant? = null,

    @Column(name = "unbanstaff", length = 30)
    var unbanstaff: String? = null,

    @Column(name = "unbanreason", length = 100)
    var unbanreason: String? = null,
) {
    fun toModel(): Ban? {
        return Ban(
            id,
            uuid?.let {UUIDUtil.fromStringOrFix(it)},
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
    uuid = this.uuid?.let { UUIDUtil.uuidToString(it) },
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

