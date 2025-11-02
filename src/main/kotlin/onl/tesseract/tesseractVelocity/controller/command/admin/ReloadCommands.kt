package commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.ProxyServer
import net.kyori.adventure.text.Component
import onl.tesseract.tesseractVelocity.Hibernate
import onl.tesseract.tesseractVelocity.config.Config

class ReloadCommands(
    private val proxy: ProxyServer,
) {
    fun registerAll() {
        proxy.commandManager.register(
            proxy.commandManager.metaBuilder("reload").build(),
            BrigadierCommand(buildReload())
        )
    }

    private fun buildReload(): LiteralArgumentBuilder<CommandSource> {
        return literal<CommandSource>("treload")
            .requires { it.hasPermission("tesseract.admin.reload") }
            .executes { ctx ->
                try {
                    Config.load()
                    Hibernate.init(Config.dbAdmin)
                    ctx.source.sendMessage(Component.text("§aConfig rechargée et connexion DB re-initialisée."))
                } catch (e: Exception) {
                    ctx.source.sendMessage(Component.text("§cÉchec du reload: ${e.message}"))
                }
                1
            }
    }
}
