package onl.tesseract.tesseractVelocity.domain.admin

import java.time.Instant
import java.util.UUID

data class PlayerInfo(
    val uuid: UUID,
    val name: String,
    val lastIp: String,
    val firstJoin: Instant,
    var lastSeen: Instant
)
