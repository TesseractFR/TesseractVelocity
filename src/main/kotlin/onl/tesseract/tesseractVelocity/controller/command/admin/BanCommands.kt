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

class BanCommands(
    private val proxy: ProxyServer,
    private val adminService: AdminService
) {

    fun registerAll() {
        proxy.commandManager.register(
                proxy.commandManager.metaBuilder("gban").build(),
        BrigadierCommand(buildGban())
        )
        proxy.commandManager.register(
                proxy.commandManager.metaBuilder("gtempban").build(),
        BrigadierCommand(buildGtempban())
        )
        proxy.commandManager.register(
                proxy.commandManager.metaBuilder("ban").build(),
        BrigadierCommand(buildBan())
        )

        proxy.commandManager.register(
            proxy.commandManager.metaBuilder("tempban").build(),
            BrigadierCommand(buildTempban())
        )
    }

    private fun buildGban(): LiteralArgumentBuilder<CommandSource> {
        return literal<CommandSource>("gban")
                .then(argument<CommandSource, String>("target", word())
                        .then(argument<CommandSource, String>("reason", greedyString())
                                .executes { ctx ->
                                    executeBan(ctx, global = true, temporary = false)
                                }))
    }

    private fun buildGtempban(): LiteralArgumentBuilder<CommandSource> {
        return literal<CommandSource>("gtempban")
                .then(argument<CommandSource, String>("target", word())
                        .then(argument<CommandSource, String>("time", word())
                                .then(argument<CommandSource, String>("reason", greedyString())
                                        .executes { ctx ->
                                            executeBan(ctx, global = true, temporary = true)
                                        })))
    }

    private fun buildBan(): LiteralArgumentBuilder<CommandSource> {
        return literal<CommandSource>("ban")
                .then(argument<CommandSource, String>("target", word())
                        .then(argument<CommandSource, String>("reason", greedyString())
                                .executes { ctx ->
                                    executeBan(ctx, global = false, temporary = false)
                                }))
    }

    private fun buildTempban(): LiteralArgumentBuilder<CommandSource> {
        return literal<CommandSource>("tempban")
                .then(argument<CommandSource, String>("target", word())
                        .then(argument<CommandSource, String>("time", word())
                                .then(argument<CommandSource, String>("reason", greedyString())
                                        .executes { ctx ->
                                            executeBan(ctx, global = false, temporary = true)
                                        })))
    }

    private fun executeBan(
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
        val success = adminService.ban(resolveBanTarget(proxy,target), reason, sourceServer, duration, staff)

        if (!success) {
            source.sendMessage(Component.text("§cLe bannissement de $target a échoué."))
            return 0
        }

        val scope = if (global) "globalement" else "localement"
        val temp = if (duration != null) " temporairement (${formatDuration(duration)})" else ""

        source.sendMessage(Component.text("§a$target a été banni$temp $scope pour: $reason"))
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

    fun resolveBanTarget(proxy: ProxyServer, input: String): BanTarget {
        if(isIpAddress(input)){
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
