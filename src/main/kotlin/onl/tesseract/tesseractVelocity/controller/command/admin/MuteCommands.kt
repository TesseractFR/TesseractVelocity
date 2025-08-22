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
import onl.tesseract.tesseractVelocity.utils.TimeParser
import java.time.Duration

class MuteCommands(
    private val proxy: ProxyServer,
    private val adminService: AdminService
) {

    fun registerAll() {
        proxy.commandManager.register(
            proxy.commandManager.metaBuilder("gmute").build(),
            BrigadierCommand(buildGmute())
        )
        proxy.commandManager.register(
            proxy.commandManager.metaBuilder("gtempmute").build(),
            BrigadierCommand(buildGtempmute())
        )
        proxy.commandManager.register(
            proxy.commandManager.metaBuilder("mute").build(),
            BrigadierCommand(buildMute())
        )
        proxy.commandManager.register(
            proxy.commandManager.metaBuilder("tempmute").build(),
            BrigadierCommand(buildTempmute())
        )
    }

    private fun buildGmute(): LiteralArgumentBuilder<CommandSource> {
        return literal<CommandSource>("gmute")
            .then(argument<CommandSource, String>("target", word())
                .then(argument<CommandSource, String>("reason", greedyString())
                    .executes { ctx ->
                        executeMute(ctx, global = true, temporary = false)
                    }
                )
            )
    }

    private fun buildGtempmute(): LiteralArgumentBuilder<CommandSource> {
        return literal<CommandSource>("gtempmute")
            .then(argument<CommandSource, String>("target", word())
                .then(argument<CommandSource, String>("time", word())
                    .then(argument<CommandSource, String>("reason", greedyString())
                        .executes { ctx ->
                            executeMute(ctx, global = true, temporary = true)
                        }
                    )
                )
            )
    }

    private fun buildMute(): LiteralArgumentBuilder<CommandSource> {
        return literal<CommandSource>("mute")
            .then(argument<CommandSource, String>("target", word())
                .then(argument<CommandSource, String>("reason", greedyString())
                    .executes { ctx ->
                        executeMute(ctx, global = false, temporary = false)
                    }
                )
            )
    }

    private fun buildTempmute(): LiteralArgumentBuilder<CommandSource> {
        return literal<CommandSource>("tempmute")
            .then(argument<CommandSource, String>("target", word())
                .then(argument<CommandSource, String>("time", word())
                    .then(argument<CommandSource, String>("reason", greedyString())
                        .executes { ctx ->
                            executeMute(ctx, global = false, temporary = true)
                        }
                    )
                )
            )
    }

    private fun executeMute(
        ctx: CommandContext<CommandSource>,
        global: Boolean,
        temporary: Boolean
    ): Int {
        val source = ctx.source
        val target = ctx.getArgument("target", String::class.java)
        val reason = ctx.getArgument("reason", String::class.java)

        val duration: Duration? = if (temporary) {
            val timeStr = ctx.getArgument("time", String::class.java)
            TimeParser.parse(timeStr).also {
                if (it == null) {
                    source.sendMessage(Component.text("§cFormat de durée invalide. Ex: 30d, 10min, 1h"))
                    return 0
                }
            }
        } else null

        val sourceServer: String? = if (global) {
            null
        } else {
            val server = (source as? Player)?.currentServer?.get()?.serverInfo?.name
            if (server == null) {
                source.sendMessage(Component.text("§cCette commande ne peut être utilisée que par un joueur connecté."))
                return 0
            }
            server
        }

        val staff = if (source is Player) source.username else null
        val success = adminService.mute(resolveMuteTarget(proxy, target), reason, sourceServer, duration, staff)

        if (!success) {
            source.sendMessage(Component.text("§cLe mute de $target a échoué."))
            return 0
        }

        val scope = if (global) "globalement" else "localement"
        val temp = if (duration != null) " temporairement (${formatDuration(duration)})" else ""

        source.sendMessage(Component.text("§a$target a été mute$temp $scope pour: $reason"))
        return Command.SINGLE_SUCCESS
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

    fun resolveMuteTarget(proxy: ProxyServer, input: String): BanTarget {
        if (isIpAddress(input)) {
            return BanTarget.Ip(input)
        }
        val player = proxy.getPlayer(input).orElse(null)
        return if (player != null) {
            BanTarget.Player(player)
        } else {
            BanTarget.Ip(input) // ip ou pseudo non connecté
        }
    }

    fun isIpAddress(input: String): Boolean {
        return Regex("""^(\d{1,3}\.){3}\d{1,3}$""").matches(input)
    }
}
