package commands

import com.mojang.brigadier.arguments.StringArgumentType.word
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.ProxyServer
import net.kyori.adventure.text.Component
import onl.tesseract.tesseractVelocity.service.admin.AdminService
import onl.tesseract.tesseractVelocity.utils.IpUtil
import java.time.format.DateTimeFormatter
import java.util.*

class ListCommands(
    private val proxy: ProxyServer,
    private val adminService: AdminService
) {

    fun registerAll() {
        proxy.commandManager.register(
            proxy.commandManager.metaBuilder("banlist").build(),
            BrigadierCommand(buildBanList())
        )
        proxy.commandManager.register(
            proxy.commandManager.metaBuilder("mutelist").build(),
            BrigadierCommand(buildMuteList())
        )
        proxy.commandManager.register(
            proxy.commandManager.metaBuilder("history").build(),
            BrigadierCommand(buildHistory())
        )
    }

    private fun buildBanList(): LiteralArgumentBuilder<CommandSource> {
        return literal<CommandSource>("banlist")
            .requires { it.hasPermission("tesseract.admin.list") }
            .executes { ctx ->
                sendBanList(ctx.source, null)
                1
            }
            .then(argument<CommandSource, String>("server", word())
                .executes { ctx ->
                    val server = ctx.getArgument("server", String::class.java)
                    sendBanList(ctx.source, server)
                    1
                }
            )
    }

    private fun buildMuteList(): LiteralArgumentBuilder<CommandSource> {
        return literal<CommandSource>("mutelist")
            .requires { it.hasPermission("tesseract.admin.list") }
            .executes { ctx ->
                sendMuteList(ctx.source, null)
                1
            }
            .then(argument<CommandSource, String>("server", word())
                .executes { ctx ->
                    val server = ctx.getArgument("server", String::class.java)
                    sendMuteList(ctx.source, server)
                    1
                }
            )
    }

    private fun buildHistory(): LiteralArgumentBuilder<CommandSource> {
        return literal<CommandSource>("history")
            .requires { it.hasPermission("tesseract.admin.history") }
            .then(argument<CommandSource, String>("target", word())
                .executes { ctx ->
                    val input = ctx.getArgument("target", String::class.java)
                    handleHistory(ctx.source, input)
                    1
                }
            )
    }

    private fun sendBanList(source: CommandSource, server: String?) {
        val bans = adminService.listActiveBans(server).take(50)
        if (bans.isEmpty()) {
            source.sendMessage(Component.text("§7[LIST] §fAucun ban actif."))
            return
        }
        source.sendMessage(Component.text("§7[LIST] §fBans actifs${server?.let { " (serveur: $it)" } ?: " (globaux+locaux)"} :"))
        val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withLocale(Locale.FRANCE)
        bans.forEach { b ->
            val scope = if (b.server == null) "global" else b.server
            val who = b.uuid?.toString() ?: (b.ip ?: "?")
            val end = b.end?.toString() ?: "permanent"
            source.sendMessage(Component.text("§8- §f$who §7| §f$scope §7| §f${b.reason ?: "-"} §7| fin: §f$end"))
        }
    }

    private fun sendMuteList(source: CommandSource, server: String?) {
        val mutes = adminService.listActiveMutes(server).take(50)
        if (mutes.isEmpty()) {
            source.sendMessage(Component.text("§7[LIST] §fAucun mute actif."))
            return
        }
        source.sendMessage(Component.text("§7[LIST] §fMutes actifs${server?.let { " (serveur: $it)" } ?: " (globaux+locaux)"} :"))
        mutes.forEach { m ->
            val scope = m.muteServer ?: "global"
            val who = m.uuid ?: (m.muteIp ?: "?")
            val end = m.muteEnd?.toString() ?: "permanent"
            source.sendMessage(Component.text("§8- §f$who §7| §f$scope §7| §f${m.muteReason ?: "-"} §7| fin: §f$end"))
        }
    }

    private fun handleHistory(source: CommandSource, input: String) {
        val (uuid, ip) = if (IpUtil.isValidIPv4(input)) {
            null to input
        } else {
            val info = adminService.getPlayerInfo(input)
            if (info == null) {
                source.sendMessage(Component.text("§cCible inconnue. Utilisez un pseudo connu (vu en base) ou une IP valide."))
                return
            }
            info.uuid to null
        }
        val (bans, mutes) = adminService.getHistory(uuid, ip)
        if (bans.isEmpty() && mutes.isEmpty()) {
            source.sendMessage(Component.text("§7[HISTORY] §fAucune sanction trouvée."))
            return
        }
        source.sendMessage(Component.text("§7[HISTORY] §fDernières sanctions pour §b$input§f:"))
        bans.sortedByDescending { it.begin }.take(10).forEach { b ->
            val scope = if (b.server == null) "global" else b.server
            val end = b.end?.toString() ?: "permanent"
            source.sendMessage(Component.text("§8- §cBAN §7| §f$scope §7| §f${b.reason ?: "-"} §7| du: §f${b.begin} §7au: §f$end"))
        }
        mutes.sortedByDescending { it.muteBegin }.take(10).forEach { m ->
            val scope = m.muteServer ?: "global"
            val end = m.muteEnd?.toString() ?: "permanent"
            source.sendMessage(Component.text("§8- §6MUTE §7| §f$scope §7| §f${m.muteReason ?: "-"} §7| du: §f${m.muteBegin} §7au: §f$end"))
        }
    }
}
