package commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType.greedyString
import com.mojang.brigadier.arguments.StringArgumentType.word
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import com.mojang.brigadier.context.CommandContext
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import net.kyori.adventure.text.Component
import onl.tesseract.tesseractVelocity.domain.admin.BanTarget
import onl.tesseract.tesseractVelocity.service.admin.AdminService
import onl.tesseract.tesseractVelocity.utils.IpUtil

class KickCommands(
    private val proxy: ProxyServer,
    private val adminService: AdminService
) {
    fun registerAll() {
        proxy.commandManager.register(
            proxy.commandManager.metaBuilder("gkick").build(),
            BrigadierCommand(buildGkick())
        )
        proxy.commandManager.register(
            proxy.commandManager.metaBuilder("kick").build(),
            BrigadierCommand(buildKick())
        )
    }

    private fun buildGkick(): LiteralArgumentBuilder<CommandSource> {
        return literal<CommandSource>("gkick")
            .requires { it.hasPermission("tesseract.admin.gkick") }
            .then(argument<CommandSource, String>("target", word())
                .then(argument<CommandSource, String>("reason", greedyString())
                    .executes { ctx ->
                        executeKick(ctx, global = true)
                    }
                )
            )
    }

    private fun buildKick(): LiteralArgumentBuilder<CommandSource> {
        return literal<CommandSource>("kick")
            .requires { it.hasPermission("tesseract.admin.kick") }
            .then(argument<CommandSource, String>("target", word())
                .then(argument<CommandSource, String>("reason", greedyString())
                    .executes { ctx ->
                        executeKick(ctx, global = false)
                    }
                )
            )
    }

    private fun executeKick(ctx: CommandContext<CommandSource>, global: Boolean): Int {
        val source = ctx.source
        val input = ctx.getArgument("target", String::class.java)
        val reason = ctx.getArgument("reason", String::class.java)

        val serverName: String? = if (global) null else {
            val s = (source as? Player)?.currentServer?.get()?.serverInfo?.name
            if (s == null) {
                source.sendMessage(Component.text("§cCette commande ne peut être utilisée que par un joueur connecté."))
                return 0
            }
            s
        }

        val target: BanTarget? = resolveTarget(input)
        if (target == null) {
            source.sendMessage(Component.text("§cCible invalide: utilisez un pseudo en ligne ou une IPv4 valide."))
            return 0
        }

        val staff = if (source is Player) source.username else null
        val ok = adminService.kick(target, reason, serverName, staff)
        if (!ok) {
            source.sendMessage(Component.text("§cAucun joueur trouvé à kicker pour la cible donnée."))
            return 0
        }
        source.sendMessage(Component.text("§aKick exécuté."))
        return Command.SINGLE_SUCCESS
    }

    private fun resolveTarget(input: String): BanTarget? {
        // If IPv4, target IP; else try online player by name
        if (IpUtil.isValidIPv4(input)) return BanTarget.Ip(input)
        val player = proxy.getPlayer(input).orElse(null)
        return if (player != null) BanTarget.Player(player) else null
    }
}
