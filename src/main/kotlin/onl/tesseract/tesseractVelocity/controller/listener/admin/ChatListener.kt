package onl.tesseract.tesseractVelocity.controller.listener.admin

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.PlayerChatEvent
import net.kyori.adventure.text.Component
import onl.tesseract.tesseractVelocity.service.admin.AdminService
import java.time.Duration
import java.time.Instant

class ChatListener(private val adminService: AdminService) {

    @Subscribe
    fun onPlayerChat(event: PlayerChatEvent) {
        val player = event.player
        if (player.hasPermission("tesseract.admin.mute.bypass")) return

        val currentServer = player.currentServer.map { it.serverInfo.name }.orElse(null)
        val mute = adminService.getActiveMute(player, currentServer) ?: return

        // Build remaining time text if temporary
        val remainingText = mute.muteEnd?.let { end ->
            val now = Instant.now()
            if (end.isAfter(now)) {
                val dur = Duration.between(now, end)
                " (${formatDuration(dur)} restantes)"
            } else {
                ""
            }
        } ?: ""

        player.sendMessage(Component.text("§cVous êtes mute${remainingText}. Raison: ${mute.muteReason ?: "-"}"))
        event.result = PlayerChatEvent.ChatResult.denied()
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
