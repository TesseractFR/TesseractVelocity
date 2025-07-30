package onl.tesseract.tesseractVelocity.domain.admin

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

fun buildBanMessage(ban: Ban): Component {
    return Component.text()
            .append(Component.text("§cVous êtes banni du serveur.\n", NamedTextColor.RED))
            .append(Component.text("§7Raison: ", NamedTextColor.GRAY))
            .append(Component.text(ban.reason + "\n", NamedTextColor.WHITE))
            .append(Component.text(if (ban.end != null) {
                "Expire le ${ban.end}"
            } else {
                "Ban définitif"
            }, NamedTextColor.GRAY))
            .build()
}