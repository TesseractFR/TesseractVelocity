package onl.tesseract.tesseractVelocity.domain.admin

sealed class BanTarget {
    data class Player(val player: com.velocitypowered.api.proxy.Player) : BanTarget()
    data class Ip(val address: String) : BanTarget()

    override fun toString(): String = when (this) {
        is Player -> player.uniqueId.toString()
        is Ip -> address
    }
}