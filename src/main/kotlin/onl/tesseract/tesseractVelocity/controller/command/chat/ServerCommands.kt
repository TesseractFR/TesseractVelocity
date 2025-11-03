package commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import java.util.*
import java.util.concurrent.TimeUnit

import onl.tesseract.tesseractVelocity.TesseractVelocity
import java.time.Duration

class ServerCommands(private val proxy: ProxyServer, private val plugin: TesseractVelocity) {

    private val teleporting: MutableSet<UUID> = HashSet()

    fun registerAll() {
        // Register one command per available server
        proxy.allServers.forEach { reg ->
            val name = reg.serverInfo.name
            val meta = proxy.commandManager.metaBuilder(name).build()
            proxy.commandManager.register(meta, BrigadierCommand(buildServerCommand(name)))
        }
    }

    private fun buildServerCommand(name: String): com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSource> {
        return literal<CommandSource>(name)
            .requires { it.hasPermission("tesseract.bungee.server.lobby") }
            .executes { ctx ->
                val src = ctx.source
                if (src !is Player) return@executes Command.SINGLE_SUCCESS

                val current = src.currentServer.map { it.serverInfo.name }.orElse("")
                if (current.equals(name, true)) {
                    src.sendMessage(Component.text("Vous êtes déjà sur $name", NamedTextColor.YELLOW))
                    return@executes Command.SINGLE_SUCCESS
                }

                if (teleporting.contains(src.uniqueId)) {
                    src.sendMessage(Component.text("Téléportation déjà en cours vers $name", NamedTextColor.RED))
                    return@executes Command.SINGLE_SUCCESS
                }

                src.sendMessage(Component.text("Téléportation vers $name...", NamedTextColor.GOLD))
                teleporting.add(src.uniqueId)

                proxy.scheduler.buildTask(plugin, Runnable {
                    val reg = proxy.getServer(name)
                    if (reg.isPresent) {
                        src.createConnectionRequest(reg.get()).connect()
                    }
                    teleporting.remove(src.uniqueId)
                }).delay(Duration.ofMillis(10)).schedule()

                Command.SINGLE_SUCCESS
            }
    }
}