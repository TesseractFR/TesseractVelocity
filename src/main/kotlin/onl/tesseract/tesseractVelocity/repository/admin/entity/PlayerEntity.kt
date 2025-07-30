package onl.tesseract.tesseractVelocity.repository.admin.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import onl.tesseract.tesseractVelocity.domain.admin.PlayerInfo
import org.hibernate.annotations.ColumnDefault
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "BAT_players")
data class PlayerEntity (
    @Column(name = "BAT_player", nullable = false, length = 30)
    var batPlayer: String,

    @Id
    @Column(name = "UUID", nullable = false, length = 100)
    var uuid: UUID,

    @Column(name = "lastip", nullable = false, length = 50)
    var lastip: String,

    @Column(name = "firstlogin")
    var firstlogin: Instant,

    @ColumnDefault("current_timestamp()")
    @Column(name = "lastlogin", nullable = false)
    var lastlogin: Instant,
){
    fun toModel() : PlayerInfo{
        return PlayerInfo(uuid,batPlayer,lastip,firstlogin,lastlogin)
    }
}

fun PlayerInfo.toEntity() : PlayerEntity{
    return PlayerEntity(name,uuid,lastIp,firstJoin,lastSeen)
}