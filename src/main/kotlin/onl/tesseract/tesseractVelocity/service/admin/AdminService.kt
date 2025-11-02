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

    fun getActiveMute(player: Player, server: String?): Mute? {
        val ip = (player.remoteAddress as InetSocketAddress).address.hostAddress
        return getActiveMute(player.uniqueId, ip, server)
    }

    fun getActiveMute(playeruuid: UUID?, ip: String? = null, server: String?): Mute? {
        return adminRepository.getActiveMute(playeruuid, ip, server)
    }

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

    fun unmute(target: BanTarget, reason: String?, server: String?, staff: String?): Boolean {
        val playerUUID: UUID? = when (target) {
            is BanTarget.Player -> target.player.uniqueId
            else -> null
        }
        val ip: String? = when (target) {
            is BanTarget.Ip -> target.address
            else -> null
        }
        val activeMute = adminRepository.getActiveMute(playerUUID, ip, server) ?: return false
        activeMute.muteState = false
        activeMute.muteUnmutereason = reason
        activeMute.muteUnmutestaff = staff ?: "Console"
        activeMute.muteUnmutedate = Instant.now()
        return adminRepository.updateMute(activeMute)
    }

    fun listActiveBans(server: String?): List<Ban> = adminRepository.listActiveBans(server)
    fun listActiveMutes(server: String?): List<Mute> = adminRepository.listActiveMutes(server)

    fun getHistory(uuid: UUID?, ip: String?): Pair<List<Ban>, List<Mute>> {
        val bans = adminRepository.findBansByUuidOrIp(uuid, ip)
        val mutes = adminRepository.findMutesByUuidOrIp(uuid, ip)
        return bans to mutes
    }

    fun kick(target: BanTarget, reason: String?, server: String?, staff: String?): Boolean {
        val kickServer = server ?: "(global)"
        return when (target) {
            is BanTarget.Player -> {
                val uuid = target.player.uniqueId
                val ok = adminRepository.insertKick(uuid, staff ?: "Console", reason, kickServer)
                if (ok) {
                    target.player.disconnect(Component.text("§cExpulsé: ${reason ?: "Aucune raison"}"))
                }
                ok
            }
            is BanTarget.Ip -> {
                // Kick all players matching IP and log per player
                val players = server?.let { s ->
                    this.server.allPlayers.filter { p ->
                        val ipAddr = (p.remoteAddress as InetSocketAddress).address.hostAddress
                        val onServer = p.currentServer.map { it.serverInfo.name }.orElse(null) == s
                        ipAddr == target.address && onServer
                    }
                } ?: this.server.allPlayers.filter { p ->
                    val ipAddr = (p.remoteAddress as InetSocketAddress).address.hostAddress
                    ipAddr == target.address
                }
                var any = false
                players.forEach { p ->
                    val ok = adminRepository.insertKick(p.uniqueId, staff ?: "Console", reason, kickServer)
                    if (ok) {
                        p.disconnect(Component.text("§cExpulsé: ${reason ?: "Aucune raison"}"))
                        any = true
                    }
                }
                any
            }
        }
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