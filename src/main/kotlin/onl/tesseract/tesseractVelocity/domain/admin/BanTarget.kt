package onl.tesseract.tesseractVelocity.domain.admin

import java.util.UUID

sealed class BanTarget {
    data class Player(val playerUUID: UUID) : BanTarget()
    data class Ip(val address: String) : BanTarget()

    override fun toString(): String = when (this) {
        is Player -> playerUUID.toString()
        is Ip -> address
    }
}