package onl.tesseract.tesseractVelocity.controller.listener.admin

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.LoginEvent
import onl.tesseract.tesseractVelocity.domain.admin.PlayerInfo
import onl.tesseract.tesseractVelocity.service.admin.AdminService
import java.net.InetSocketAddress
import java.time.Instant

class PlayerListener(private val adminService: AdminService) {
    @Subscribe
    fun onLogin(event: LoginEvent) {
        println(">>> LoginEvent capté pour ${event.player.username}")
        val player = event.player

        var playerInfo = adminService.getPlayerInfo(player.uniqueId)
        val ip = (player.remoteAddress as InetSocketAddress).address.hostAddress
        if(playerInfo == null){
            playerInfo = PlayerInfo(player.uniqueId,player.username,ip, Instant.now(), Instant.now())
        }
        playerInfo.lastSeen = Instant.now()
        adminService.updatePlayer(playerInfo)


    }

}