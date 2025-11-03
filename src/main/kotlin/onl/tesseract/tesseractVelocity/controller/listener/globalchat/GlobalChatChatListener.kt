package onl.tesseract.tesseractVelocity.controller.listener.globalchat

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.PlayerChatEvent
import com.velocitypowered.api.proxy.ProxyServer
import onl.tesseract.tesseractVelocity.globalchat.GlobalChatManager

class GlobalChatChatListener(private val proxy: ProxyServer) {

    @Subscribe
    fun onPlayerChat(event: PlayerChatEvent) {
        val player = event.player
        if (!GlobalChatManager.isGlobalChatToggled(player)) return

        val msg = event.message
        GlobalChatManager.sendGlobalMessage(proxy, player, msg)
        event.result = PlayerChatEvent.ChatResult.denied()
    }
}