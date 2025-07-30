package onl.tesseract.tesseractVelocity.controller.listener.admin

import com.velocitypowered.api.event.ResultedEvent
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.LoginEvent
import com.velocitypowered.api.event.player.ServerPreConnectEvent
import onl.tesseract.tesseractVelocity.domain.admin.buildBanMessage
import onl.tesseract.tesseractVelocity.service.admin.AdminService

class BanListener(private val adminService: AdminService) {
    @Subscribe
    fun onLogin(event: LoginEvent) {
        println(">>> LoginEvent capté pour ${event.player.username}")
        val player = event.player

        val ban = adminService.getActiveBan(player, null)

        if (ban != null) {
            event.result = ResultedEvent.ComponentResult.denied(buildBanMessage(ban))
        }
    }

    @Subscribe
    fun onServerPreConnect(event: ServerPreConnectEvent) {
        println(">>> LoginEvent capté pour ${event.player.username}")
        val player = event.player
        val ban = adminService.getActiveBan(player, event.originalServer.serverInfo.name)
        if (ban != null) {
            // kick si ban est encore actif (par sécurité au changement de serveur)
            player.disconnect(buildBanMessage(ban))
        }
    }

}