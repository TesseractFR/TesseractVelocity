package onl.tesseract.tesseractVelocity.repository.admin.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import onl.tesseract.tesseractVelocity.domain.admin.PlayerInfo
import onl.tesseract.tesseractVelocity.utils.UUIDUtil.fromStringOrFix
import onl.tesseract.tesseractVelocity.utils.UUIDUtil.uuidToString
import org.hibernate.annotations.ColumnDefault
import java.time.Instant

@Entity
@Table(name = "t_admin_players")
data class PlayerEntity (
    @Column(name = "BAT_player", nullable = false, length = 30)
    var batPlayer: String = "",

    @Id
    @Column(name = "UUID", nullable = false, length = 32)
    var uuid: String = "",

    @Column(name = "lastip", nullable = false, length = 50)
    var lastip: String = "",
    @Column(name = "firstlogin")
    var firstlogin: Instant = Instant.now(),

    @ColumnDefault("current_timestamp()")
    @Column(name = "lastlogin", nullable = false)
    var lastlogin: Instant = Instant.now(),
){
    fun toModel() : PlayerInfo{
        return PlayerInfo(fromStringOrFix(uuid), batPlayer, lastip, firstlogin, lastlogin)
    }
}

fun PlayerInfo.toEntity() : PlayerEntity{
    return PlayerEntity(name, uuidToString(uuid), lastIp, firstJoin, lastSeen)
}