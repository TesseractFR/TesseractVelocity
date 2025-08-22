package onl.tesseract.tesseractVelocity.service.admin

import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.text.Component
import onl.tesseract.tesseractVelocity.domain.admin.*
import onl.tesseract.tesseractVelocity.repository.admin.AdminRepository
import java.net.InetSocketAddress
import java.time.Duration
import java.time.Instant
import java.util.*

class AdminService(val adminRepository: AdminRepository, private val server: com.velocitypowered.api.proxy.ProxyServer) {



    fun getActiveBan(player: Player, server: String?) : Ban?{
        val ip = (player.remoteAddress as InetSocketAddress).address.hostAddress
        return getActiveBan(player.uniqueId,ip,server)
    }
    fun getActiveBan(playeruuid: UUID?, ip: String? = null, server: String?) : Ban?{
        return adminRepository.getActiveBan(playeruuid,ip,server)
    }

    fun mute(
        target: BanTarget,
        reason: String,
        server: String?,
        duration: Duration?,
        staff: String?
    ): Boolean {
        val playerUUID: UUID? = when (target) {
            is BanTarget.Player -> target.player.uniqueId
            else -> null
        }
        val ip: String? = when (target) {
            is BanTarget.Ip -> target.address
            else -> null
        }

        // Vérifier si le joueur est déjà muté
        if (playerUUID != null && adminRepository.isPlayerMuted(playerUUID)) return false

        val mute = Mute(
            uuid = playerUUID?.toString(),
            muteIp = ip,
            muteStaff = staff ?: "Console",
            muteReason = reason,
            muteServer = server,
            muteBegin = Instant.now(),
            muteEnd = duration?.let { Instant.now().plus(it) },
            muteState = true
        )

        return adminRepository.insertMute(mute)
    }

    fun ban(
        target: BanTarget,
        reason: String,
        server: String?,
        duration: Duration?,
        staff : String?
    ): Boolean {
        val playerUUID: UUID? = when (target) {
            is BanTarget.Player -> target.player.uniqueId
            else -> null
        }
        val ip: String? = when (target) {
            is BanTarget.Ip -> target.address
            else -> null
        }
        if (adminRepository.getActiveBan(playerUUID,ip,server)!=null) return false

        val ban = Ban(
            uuid = playerUUID,
            ip = ip,
            staff = staff ?: "Console", // Ou tu passes un staff dynamique (CommandSource.name ?)
            reason = reason,
            server = server,
            begin = Instant.now(),
            end = duration?.let { Instant.now().plus(it) },
            state = true // Ban actif
        )

        val success = adminRepository.insertBan(ban)

        if (success) {
            // Créer le message de ban
            val banMessage = createBanMessage(reason, server, duration, staff)

            when (target) {
                // Si c'est un joueur spécifique qui est banni
                is BanTarget.Player -> {
                    target.player.disconnect(banMessage)
                }

                // Si c'est une IP qui est bannie
                is BanTarget.Ip -> {
                    // Kick tous les joueurs connectés avec cette IP
                    kickAllPlayersWithIp(ip, banMessage, server)
                }
            }
        }

        return success
    }

    fun unban(
        target: BanTarget,
        reason: String,
        server: String?,
        staff: String?
    ): Boolean {
        val playerUUID: UUID? = when (target) {
            is BanTarget.Player -> target.player.uniqueId
            else -> null
        }
        val ip: String? = when (target) {
            is BanTarget.Ip -> target.address
            else -> null
        }

        val activeBan = adminRepository.getActiveBan(playerUUID, ip, server) ?: return false

        activeBan.state = false
        activeBan.unbanreason = reason
        activeBan.unbanstaff = staff ?: "Console"
        activeBan.unbandate = Instant.now()

        return adminRepository.updateBan(activeBan)
    }
    fun getPlayerInfo(playerName: String): PlayerInfo? {
        return adminRepository.findPlayerByName(playerName)
    }
    fun isMuted(playerUUID: UUID): Boolean {
        return adminRepository.isPlayerMuted(playerUUID)
    }
    fun countTotalSanctions(playerUUID: UUID): Sanctions {
        return adminRepository.countTotalSanctions(playerUUID)
    }
    fun countActiveSanctionsByIp(ipAddress: String): Sanctions {
        return adminRepository.countSanctionsForIp(ipAddress)
    }

    fun getPlayersByIp(ipAddress: String): List<PlayerInfo> {
        return adminRepository.findPlayersByIp(ipAddress)
    }

    private fun createBanMessage(reason: String, server: String?, duration: Duration?, staff: String?): Component {
        val durationText = if (duration != null) " pour ${formatDuration(duration)}" else ""
        val scopeText = if (server == null) "globalement" else "du serveur ${server}"

        return Component.text("§cVous avez été banni ${scopeText}${durationText}\n§cRaison: ${reason}\n§cPar: ${staff ?: "Console"}")
    }

    private fun kickAllPlayersWithIp(ip: String?, banMessage: Component, serverName: String?) {
        if (ip == null) return

        server.allPlayers.forEach { player ->
            val playerIp = (player.remoteAddress as InetSocketAddress).address.hostAddress
            val onTargetServer = serverName == null || player.currentServer.map { it.serverInfo.name }.orElse(null) == serverName

            if (playerIp == ip && onTargetServer) {
                player.disconnect(banMessage)
            }
        }
    }

    private fun formatDuration(duration: Duration): String {
        return when {
            duration.toDays() >= 30 -> "${duration.toDays() / 30} mois"
            duration.toDays() >= 1 -> "${duration.toDays()} jours"
            duration.toHours() >= 1 -> "${duration.toHours()} heures"
            duration.toMinutes() >= 1 -> "${duration.toMinutes()} minutes"
            else -> "${duration.seconds} secondes"
        }
    }
}