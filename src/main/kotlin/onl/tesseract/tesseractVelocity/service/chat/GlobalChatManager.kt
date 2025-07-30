package onl.tesseract.tesseractVelocity.service.chat

import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import onl.tesseract.tesseractVelocity.TesseractVelocity
import onl.tesseract.tesseractVelocity.plus
import java.util.*

class GlobalChatService(val plugin : TesseractVelocity) {
    private val enabledGlobalChats = HashSet<UUID?>()

    private fun getGlobalChatPrefix() : Component {
        return Component.text("[", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD) +
                Component.text("Global", NamedTextColor.LIGHT_PURPLE) +
                Component.text("] ", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD)
    }

    fun sendGlobalMessage(player: Player, message: String) {
        plugin.server.allPlayers.forEach {
            player -> player.sendMessage(
                getGlobalChatPrefix() +
                        Component.text("(", NamedTextColor.GRAY,TextDecoration.ITALIC)+
                        player.currentServer.get().serverInfo.name+ ") "+
                        Component.text(player.username , NamedTextColor.DARK_AQUA)+
                        Component.text(" » ", NamedTextColor.GRAY) +
                        Component.text(message ,NamedTextColor.WHITE)
            )}
    }


    fun sendGlobalMessage(message: String) {
        plugin.server.allPlayers.forEach {
                player -> player.sendMessage(
            getGlobalChatPrefix() +
                    Component.text("(global)", NamedTextColor.GRAY,TextDecoration.ITALIC)+
                    Component.text("CONSOLE" , NamedTextColor.DARK_AQUA)+
                    Component.text(" » ", NamedTextColor.GRAY) +
                    Component.text(message ,NamedTextColor.WHITE)
        )}
    }


    fun toggleGlobalChat(player: Player): Boolean {
        if (enabledGlobalChats.contains(player.uniqueId)) {
            enabledGlobalChats.remove(player.uniqueId)
            return false
        }
        return enabledGlobalChats.add(player.uniqueId)
    }

    fun isGlobalChatToggled(player: Player): Boolean {
        return enabledGlobalChats.contains(player.uniqueId)
    }
}