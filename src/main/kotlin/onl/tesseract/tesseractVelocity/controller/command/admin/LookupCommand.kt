package onl.tesseract.tesseractVelocity.command

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.command.BrigadierCommand
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.*
import onl.tesseract.tesseractVelocity.service.admin.AdminService
import onl.tesseract.tesseractVelocity.utils.IpUtil
import java.util.concurrent.CompletableFuture

class LookupCommandHandler(
    private val adminService: AdminService
) {
    fun createBrigadierCommand(): BrigadierCommand {
        val node = LiteralArgumentBuilder.literal<CommandSource>("lookup")
                .requires { source -> source.hasPermission("tesseract.admin.lookup") }
                .then(
                    com.mojang.brigadier.builder.RequiredArgumentBuilder.argument<CommandSource, String>("target", StringArgumentType.word())
                            .executes { ctx ->
                                val source = ctx.source
                                val target = StringArgumentType.getString(ctx, "target")
                                handleLookup(source, target)
                                1
                            }
                            .suggests(::playerSuggestions)
                )
                .build()

        return BrigadierCommand(node)
    }

    private fun handleLookup(source: CommandSource, target: String) {
        if (isIp(target)) {
            handleIpLookup(source, target)
        } else {
            handlePlayerLookup(source, target)
        }
    }

    private fun isIp(input: String): Boolean {
        return IpUtil.isValidIPv4(input)
    }

    private fun handlePlayerLookup(source: CommandSource, playerName: String) {
        val info = adminService.getPlayerInfo(playerName)
        if (info == null) {
            source.sendMessage(Component.text("Aucun joueur trouvé avec ce pseudo.", RED))
            return
        }

        val activeBan = adminService.getActiveBan(info.uuid, info.lastIp, null)
        val isMuted = adminService.isMuted(info.uuid)
        val sanctions = adminService.countTotalSanctions(info.uuid)

        val banText = when {
            activeBan == null -> Component.text("Aucun", GREEN)
            activeBan.server == null -> Component.text("Oui (global)", RED)
            else -> Component.text("Oui (serveur: ${activeBan.server})", YELLOW)
        }

        val muteText = if (isMuted) Component.text("Oui", RED) else Component.text("Non", GREEN)

        val msg = Component.text()
                .append(Component.text("§7[LOOKUP] ", GRAY))
                .append(Component.text("Joueur : ", GRAY)).append(Component.text(info.name, WHITE)).append(Component.newline())
                .append(Component.text("Première connexion : ", GRAY)).append(Component.text("${info.firstJoin}", WHITE)).append(Component.newline())
                .append(Component.text("Dernière connexion : ", GRAY)).append(Component.text("${info.lastSeen}", WHITE)).append(Component.newline())
                .append(Component.text("Dernière IP : ", GRAY)).append(Component.text("${info.lastIp ?: "inconnue"}", WHITE)).append(Component.newline())
                .append(Component.text("Ban actif : ", GRAY)).append(banText).append(Component.newline())
                .append(Component.text("Mute actif : ", GRAY)).append(muteText).append(Component.newline())
                .append(Component.text("Sanctions totales : ", GRAY))
                .append(Component.text("${sanctions.bans} ban(s), ${sanctions.mutes} mute(s), ${sanctions.kicks} kick(s)", WHITE)).append(Component.newline())
                .append(Component.text("Astuce: /history ", GRAY)).append(Component.text(info.name, WHITE)).append(Component.text(" pour les détails.", GRAY))

        source.sendMessage(msg)
    }

    private fun handleIpLookup(source: CommandSource, ip: String) {
        val players = adminService.getPlayersByIp(ip)
        val activeBan = adminService.getActiveBan(null, ip, null)
        val sanctions = adminService.countActiveSanctionsByIp(ip)

        val banText = when {
            activeBan == null -> Component.text("Aucun", GREEN)
            activeBan.server == null -> Component.text("Oui (global)", RED)
            else -> Component.text("Oui (serveur: ${activeBan.server})", YELLOW)
        }

        val playerList = if (players.isEmpty()) {
            Component.text("Aucun", RED)
        } else {
            Component.text(players.joinToString(", ") { it.name }, WHITE)
        }

        val msg = Component.text()
                .append(Component.text("§7[LOOKUP] ", GRAY))
                .append(Component.text("Adresse IP : ", GRAY)).append(Component.text(ip, WHITE)).append(Component.newline())
                .append(Component.text("Joueurs connus : ", GRAY)).append(playerList).append(Component.newline())
                .append(Component.text("Ban actif : ", GRAY)).append(banText).append(Component.newline())
                .append(Component.text("Sanctions actives : ", GRAY))
                .append(Component.text("${sanctions.bans} ban(s), ${sanctions.mutes} mute(s), ${sanctions.kicks} kick(s)", WHITE))

        source.sendMessage(msg)
    }
    private fun playerSuggestions(
        ctx: CommandContext<CommandSource>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        adminService.getPlayers(builder.remaining)
                .map { it.name }
                .filter { it.startsWith(builder.remaining, ignoreCase = true) }
                .forEach { builder.suggest(it) }

        return builder.buildFuture()
    }

}
