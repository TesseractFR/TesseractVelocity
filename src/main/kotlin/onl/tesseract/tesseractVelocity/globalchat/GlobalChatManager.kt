package onl.tesseract.tesseractVelocity.globalchat

import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import java.util.*

object GlobalChatManager {
    private val enabledGlobalChats: MutableSet<UUID> = HashSet()

    private val PREFIX: Component = Component.text("[", NamedTextColor.DARK_PURPLE)
        .decorate(TextDecoration.BOLD)
        .append(Component.text("Global", NamedTextColor.LIGHT_PURPLE))
        .append(Component.text("]", NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD))
        .append(Component.text(" ", NamedTextColor.GRAY))

    fun toggleGlobalChat(player: Player): Boolean {
        val id = player.uniqueId
        return if (enabledGlobalChats.contains(id)) {
            enabledGlobalChats.remove(id)
            false
        } else {
            enabledGlobalChats.add(id)
            true
        }
    }

    fun isGlobalChatToggled(player: Player): Boolean = enabledGlobalChats.contains(player.uniqueId)

    fun sendGlobalMessage(server: ProxyServer, from: Player, message: String) {
        val serverName = from.currentServer.map { it.serverInfo.name }.orElse("-")
        val formatted = PREFIX
            .append(Component.text("(", NamedTextColor.GRAY).decorate(TextDecoration.ITALIC))
            .append(Component.text(serverName, NamedTextColor.GRAY).decorate(TextDecoration.ITALIC))
            .append(Component.text(") ", NamedTextColor.GRAY).decorate(TextDecoration.ITALIC))
            .append(Component.text(from.username, NamedTextColor.AQUA))
            .append(Component.text(" » ", NamedTextColor.GRAY))
            .append(Component.text(message, NamedTextColor.WHITE))
        server.allPlayers.forEach { it.sendMessage(formatted) }
    }

    fun sendGlobalMessageFromConsole(server: ProxyServer, message: String) {
        val formatted = PREFIX
            .append(Component.text("(global) ", NamedTextColor.GRAY).decorate(TextDecoration.ITALIC))
            .append(Component.text("CONSOLE", NamedTextColor.GOLD))
            .append(Component.text(" » ", NamedTextColor.GRAY))
            .append(Component.text(message, NamedTextColor.WHITE))
        server.allPlayers.forEach { it.sendMessage(formatted) }
    }
}