package commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import com.mojang.brigadier.arguments.StringArgumentType.word
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.ProxyServer
import net.kyori.adventure.text.Component
import onl.tesseract.tesseractVelocity.service.admin.AdminService

class AltsCommands(
    private val proxy: ProxyServer,
    private val adminService: AdminService
) {
    fun registerAll() {
        proxy.commandManager.register(
            proxy.commandManager.metaBuilder("alts").build(),
            BrigadierCommand(buildAlts())
        )
    }

    private fun buildAlts(): LiteralArgumentBuilder<CommandSource> {
        return literal<CommandSource>("alts")
            .requires { it.hasPermission("tesseract.admin.alts") }
            .then(argument<CommandSource, String>("player", word())
                .executes { ctx ->
                    val playerName = ctx.getArgument("player", String::class.java)
                    handleAlts(ctx.source, playerName)
                    1
                }
            )
    }

    private fun handleAlts(source: CommandSource, playerName: String) {
        val info = adminService.getPlayerInfo(playerName)
        if (info == null) {
            source.sendMessage(Component.text("§cAucun joueur trouvé avec ce pseudo."))
            return
        }
        val ip = info.lastIp
        if (ip.isBlank()) {
            source.sendMessage(Component.text("§7[ALTS] §fAucune IP connue pour ce joueur."))
            return
        }
        val players = adminService.getPlayersByIp(ip)
        val alts = players.filter { it.uuid != info.uuid }
        if (alts.isEmpty()) {
            source.sendMessage(Component.text("§7[ALTS] §fAucun autre compte connu sur l'IP §b$ip§f."))
            return
        }
        source.sendMessage(Component.text("§7[ALTS] §fComptes vus sur l'IP §b$ip§f :"))
        val line = alts.joinToString(", ") { it.name }
        source.sendMessage(Component.text("§8- §f$line"))
    }
}
