package commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType.greedyString
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import onl.tesseract.tesseractVelocity.globalchat.GlobalChatManager

class GlobalChatCommands(
    private val proxy: ProxyServer
) {

    fun registerAll() {
        proxy.commandManager.register(
            proxy.commandManager.metaBuilder("globalchat").aliases("gc").build(),
            BrigadierCommand(buildGlobalChat())
        )
    }

    private fun buildGlobalChat(): com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSource> {
        return literal<CommandSource>("globalchat")
            .requires { it.hasPermission("tesseract.bungee.globalchat") }
            .executes {
                val sender = it.source
                if (sender is Player) {
                    val enabled = GlobalChatManager.toggleGlobalChat(sender)
                    val txt = if (enabled) "Le chat global a été activé." else "Le chat global a été désactivé."
                    sender.sendMessage(Component.text(txt, NamedTextColor.GRAY))
                }
                Command.SINGLE_SUCCESS
            }
            .then(argument<CommandSource, String>("message", greedyString()).executes { ctx ->
                val sender = ctx.source
                val message = ctx.getArgument("message", String::class.java)
                when (sender) {
                    is Player -> GlobalChatManager.sendGlobalMessage(proxy, sender, message)
                    else -> GlobalChatManager.sendGlobalMessageFromConsole(proxy, message)
                }
                Command.SINGLE_SUCCESS
            })
    }
}