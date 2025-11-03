package commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class MessageCommands(private val proxy: ProxyServer) {

    private val replyMap: MutableMap<UUID, UUID> = ConcurrentHashMap()

    fun registerAll() {
        val meta = proxy.commandManager.metaBuilder("message")
            .aliases("msg", "tell", "w", "whisper", "whisp")
            .build()
        proxy.commandManager.register(meta, BrigadierCommand(buildMessage()))

        val metaReply = proxy.commandManager.metaBuilder("r").aliases("reply").build()
        proxy.commandManager.register(metaReply, BrigadierCommand(buildReply()))
    }

    private fun buildMessage(): com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSource> {
        return literal<CommandSource>("message")
            .requires { it.hasPermission("tesseract.bungee.message") }
            .then(argument<CommandSource, String>("player", StringArgumentType.word())
                .then(argument<CommandSource, String>("message", StringArgumentType.greedyString()).executes { ctx ->
                    val src = ctx.source
                    if (src !is Player) return@executes Command.SINGLE_SUCCESS
                    val targetName = ctx.getArgument("player", String::class.java)
                    val message = ctx.getArgument("message", String::class.java)
                    val target = proxy.getPlayer(targetName).orElse(null)
                    if (target == null || !target.isActive) {
                        src.sendMessage(Component.text("Joueur introuvable", NamedTextColor.RED))
                        return@executes Command.SINGLE_SUCCESS
                    }
                    sendPrivateMessage(src, target, message)
                    Command.SINGLE_SUCCESS
                }))
    }

    private fun buildReply(): com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSource> {
        return literal<CommandSource>("r")
            .requires { it.hasPermission("tesseract.bungee.message") }
            .then(argument<CommandSource, String>("message", StringArgumentType.greedyString()).executes { ctx ->
                val src = ctx.source
                if (src !is Player) return@executes Command.SINGLE_SUCCESS
                val last = replyMap[src.uniqueId]
                if (last == null) {
                    src.sendMessage(Component.text("Aucun destinataire récent.", NamedTextColor.RED))
                    return@executes Command.SINGLE_SUCCESS
                }
                val target = proxy.getPlayer(last).orElse(null)
                if (target == null || !target.isActive) {
                    src.sendMessage(Component.text("Le joueur n'est plus connecté.", NamedTextColor.RED))
                    return@executes Command.SINGLE_SUCCESS
                }
                val message = ctx.getArgument("message", String::class.java)
                sendPrivateMessage(src, target, message)
                Command.SINGLE_SUCCESS
            })
    }

    private fun sendPrivateMessage(sender: Player, receiver: Player, message: String) {
        // update reply map both ways
        replyMap[sender.uniqueId] = receiver.uniqueId
        replyMap[receiver.uniqueId] = sender.uniqueId

        val toText = Component.text()
            .append(Component.text("Envoyé à ", NamedTextColor.GOLD))
            .append(Component.text(receiver.username, NamedTextColor.RED))
            .append(Component.text(" » ", NamedTextColor.GRAY))
            .append(Component.text(message, NamedTextColor.AQUA))
            .build()
            .hoverEvent(HoverEvent.showText(Component.text("Cliquez pour répondre à ${receiver.username}", NamedTextColor.AQUA)))
            .clickEvent(ClickEvent.suggestCommand("/msg ${receiver.username} "))

        val fromText = Component.text()
            .append(Component.text("Reçu de ", NamedTextColor.GOLD))
            .append(Component.text(sender.username, NamedTextColor.RED))
            .append(Component.text(" » ", NamedTextColor.GRAY))
            .append(Component.text(message, NamedTextColor.AQUA))
            .build()
            .hoverEvent(HoverEvent.showText(Component.text("Cliquez pour répondre à ${sender.username}", NamedTextColor.AQUA)))
            .clickEvent(ClickEvent.suggestCommand("/msg ${sender.username} "))

        sender.sendMessage(toText)
        receiver.sendMessage(fromText)
    }
}