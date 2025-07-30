package onl.tesseract.tesseractVelocity.service.admin

import com.velocitypowered.api.proxy.Player
import onl.tesseract.tesseractVelocity.domain.admin.Ban
import onl.tesseract.tesseractVelocity.domain.admin.BanTarget
import onl.tesseract.tesseractVelocity.domain.admin.PlayerInfo
import onl.tesseract.tesseractVelocity.domain.admin.Sanctions
import onl.tesseract.tesseractVelocity.repository.admin.AdminRepository
import java.net.InetSocketAddress
import java.time.Duration
import java.time.Instant
import java.util.*

class AdminService(val adminRepository: AdminRepository) {



    fun getActiveBan(player: Player, server: String?) : Ban?{
        val ip = (player.remoteAddress as InetSocketAddress).address.hostAddress
        return getActiveBan(player.uniqueId,ip,server)
    }
    fun getActiveBan(playeruuid: UUID?, ip: String? = null, server: String?) : Ban?{
        return adminRepository.getActiveBan(playeruuid,ip,server)
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
            uuid = playerUUID?.toString(),
            ip = ip,
            staff = staff ?: "Console", // Ou tu passes un staff dynamique (CommandSource.name ?)
            reason = reason,
            server = server,
            begin = Instant.now(),
            end = duration?.let { Instant.now().plus(it) },
            state = true // Ban actif
        )
        return adminRepository.insertBan(ban)
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






}