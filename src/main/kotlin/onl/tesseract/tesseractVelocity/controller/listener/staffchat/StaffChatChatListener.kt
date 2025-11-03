package onl.tesseract.tesseractVelocity.controller.listener.staffchat

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.PlayerChatEvent
import onl.tesseract.tesseractVelocity.staffchat.StaffChatManager
import com.velocitypowered.api.proxy.ProxyServer

class StaffChatChatListener(private val proxy: ProxyServer) {

    @Subscribe
    fun onPlayerChat(event: PlayerChatEvent) {
        val player = event.player

        val staffToggled = StaffChatManager.isChatToggled(player)
        val globalToggled = StaffChatManager.isGlobalChatToggled(player)
        if (!staffToggled && !globalToggled) return

        val msg = event.message

        if (globalToggled) {
            StaffChatManager.sendGlobalStaffMessage(proxy, player, msg)
        } else if (staffToggled) {
            StaffChatManager.sendStaffMessage(proxy, player, msg)
        }

        event.result = PlayerChatEvent.ChatResult.denied()
    }
}
