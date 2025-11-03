package onl.tesseract.tesseractVelocity.staffchat

import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import java.util.*

object StaffChatManager {
    private val enabledChats: MutableSet<UUID> = HashSet()
    private val enabledGlobalChats: MutableSet<UUID> = HashSet()

    private val PREFIX: Component = Component.text("[", NamedTextColor.GOLD)
        .decorate(TextDecoration.BOLD)
        .append(Component.text("Staff", NamedTextColor.YELLOW))
        .append(Component.text("]", NamedTextColor.GOLD).decorate(TextDecoration.BOLD))
        .append(Component.text(" ", NamedTextColor.GRAY))

    fun toggleStaffChat(player: Player): Boolean {
        val id = player.uniqueId
        return if (enabledChats.contains(id)) {
            enabledChats.remove(id)
            false
        } else {
            enabledChats.add(id)
            true
        }
    }

    fun isChatToggled(player: Player): Boolean = enabledChats.contains(player.uniqueId)

    fun toggleGlobalStaffChat(player: Player): Boolean {
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

    fun sendStaffMessage(server: ProxyServer, from: Player, message: String) {
        val current = from.currentServer.orElse(null) ?: return
        val formatted = formatFrom(from.username, message)
        current.server.playersConnected
            .filter { it.hasPermission("tesseract.velocity.staffchat") }
            .forEach { it.sendMessage(formatted) }
    }

    fun sendGlobalStaffMessage(server: ProxyServer, from: Player, message: String) {
        val formatted = formatServer(from, message)
        server.allPlayers
            .filter { it.hasPermission("tesseract.velocity.staffchat") }
            .forEach { it.sendMessage(formatted) }
    }

    fun sendGlobalStaffMessageFromConsole(server: ProxyServer, message: String) {
        val formatted = formatGlobal(message)
        server.allPlayers
            .filter { it.hasPermission("tesseract.velocity.staffchat") }
            .forEach { it.sendMessage(formatted) }
    }

    private fun formatFrom(playerName: String, message: String): Component {
        return PREFIX
            .append(Component.text(playerName, NamedTextColor.GOLD))
            .append(Component.text(" » ", NamedTextColor.GRAY))
            .append(Component.text(message, NamedTextColor.WHITE))
    }

    private fun formatServer(player: Player, message: String): Component {
        val serverName = player.currentServer.map { it.serverInfo.name }.orElse("-")
        return PREFIX
            .append(Component.text("(", NamedTextColor.GRAY).decorate(TextDecoration.ITALIC))
            .append(Component.text(serverName, NamedTextColor.GRAY).decorate(TextDecoration.ITALIC))
            .append(Component.text(") ", NamedTextColor.GRAY).decorate(TextDecoration.ITALIC))
            .append(Component.text(player.username, NamedTextColor.GOLD))
            .append(Component.text(" » ", NamedTextColor.GRAY))
            .append(Component.text(message, NamedTextColor.WHITE))
    }

    private fun formatGlobal(message: String): Component {
        return PREFIX
            .append(Component.text("(global) ", NamedTextColor.GRAY).decorate(TextDecoration.ITALIC))
            .append(Component.text("CONSOLE", NamedTextColor.GOLD))
            .append(Component.text(" » ", NamedTextColor.GRAY))
            .append(Component.text(message, NamedTextColor.WHITE))
    }
}
