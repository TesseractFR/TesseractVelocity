package commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType.greedyString
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import com.mojang.brigadier.context.CommandContext
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import onl.tesseract.tesseractVelocity.staffchat.StaffChatManager

class StaffChatCommands(
    private val proxy: ProxyServer
) {

    private val PREFIX: Component = Component.text("[", NamedTextColor.GOLD)
        .decorate(TextDecoration.BOLD)
        .append(Component.text("Staff", NamedTextColor.YELLOW))
        .append(Component.text("]", NamedTextColor.GOLD).decorate(TextDecoration.BOLD))
        .append(Component.text(" ", NamedTextColor.GRAY))

    fun registerAll() {
        proxy.commandManager.register(
            proxy.commandManager.metaBuilder("staffchat").aliases("staffc", "stc").build(),
            BrigadierCommand(buildStaffChat())
        )
        proxy.commandManager.register(
            proxy.commandManager.metaBuilder("globalstaffchat").aliases("gstaffc", "gstc").build(),
            BrigadierCommand(buildGlobalStaffChat())
        )
    }

    private fun buildStaffChat(): LiteralArgumentBuilder<CommandSource> {
        return literal<CommandSource>("staffchat")
            .requires { it.hasPermission("tesseract.bungee.staffchat") }
            .executes { ctx ->
                val sender = ctx.source
                if (sender !is Player) return@executes Command.SINGLE_SUCCESS
                val enabled = StaffChatManager.toggleStaffChat(sender)
                val msg = if (enabled) {
                    Component.text("Le chat staff a été activé.", NamedTextColor.GRAY)
                } else {
                    Component.text("Le chat staff a été désactivé.", NamedTextColor.GRAY)
                }
                sender.sendMessage(PREFIX.append(msg))
                Command.SINGLE_SUCCESS
            }
            .then(argument<CommandSource, String>("message", greedyString())
                .executes { ctx -> executeSendStaff(ctx) })
    }

    private fun executeSendStaff(ctx: CommandContext<CommandSource>): Int {
        val sender = ctx.source
        if (sender !is Player) return Command.SINGLE_SUCCESS
        val message = ctx.getArgument("message", String::class.java)
        StaffChatManager.sendStaffMessage(proxy, sender, message)
        return Command.SINGLE_SUCCESS
    }

    private fun buildGlobalStaffChat(): LiteralArgumentBuilder<CommandSource> {
        return literal<CommandSource>("globalstaffchat")
            .requires { it.hasPermission("tesseract.bungee.staffchat") }
            .executes { ctx ->
                val sender = ctx.source
                if (sender is Player) {
                    val enabled = StaffChatManager.toggleGlobalStaffChat(sender)
                    val msg = if (enabled) {
                        Component.text("Le chat staff global a été activé.", NamedTextColor.GRAY)
                    } else {
                        Component.text("Le chat staff global a été désactivé.", NamedTextColor.GRAY)
                    }
                    sender.sendMessage(PREFIX.append(msg))
                }
                Command.SINGLE_SUCCESS
            }
            .then(argument<CommandSource, String>("message", greedyString())
                .executes { ctx -> executeSendGlobal(ctx) })
    }

    private fun executeSendGlobal(ctx: CommandContext<CommandSource>): Int {
        val sender = ctx.source
        val message = ctx.getArgument("message", String::class.java)
        when (sender) {
            is Player -> StaffChatManager.sendGlobalStaffMessage(proxy, sender, message)
            else -> StaffChatManager.sendGlobalStaffMessageFromConsole(proxy, message)
        }
        return Command.SINGLE_SUCCESS
    }
}
