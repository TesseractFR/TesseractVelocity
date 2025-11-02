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
                sendBanList(ctx.source, null, 1)
                1
            }
            .then(argument<CommandSource, String>("server", word())
                .executes { ctx ->
                    val server = ctx.getArgument("server", String::class.java)
                    sendBanList(ctx.source, server, 1)
                    1
                }
                .then(argument<CommandSource, String>("page", word())
                    .executes { ctx ->
                        val server = ctx.getArgument("server", String::class.java)
                        val pageStr = ctx.getArgument("page", String::class.java)
                        val page = pageStr.toIntOrNull() ?: 1
                        sendBanList(ctx.source, server, if (page < 1) 1 else page)
                        1
                    }
                )
            )
            .then(argument<CommandSource, String>("page", word())
                .executes { ctx ->
                    val pageStr = ctx.getArgument("page", String::class.java)
                    val page = pageStr.toIntOrNull() ?: 1
                    sendBanList(ctx.source, null, if (page < 1) 1 else page)
                    1
                }
            )
    }

    private fun buildMuteList(): LiteralArgumentBuilder<CommandSource> {
        return literal<CommandSource>("mutelist")
            .requires { it.hasPermission("tesseract.admin.list") }
            .executes { ctx ->
                sendMuteList(ctx.source, null, 1)
                1
            }
            .then(argument<CommandSource, String>("server", word())
                .executes { ctx ->
                    val server = ctx.getArgument("server", String::class.java)
                    sendMuteList(ctx.source, server, 1)
                    1
                }
                .then(argument<CommandSource, String>("page", word())
                    .executes { ctx ->
                        val server = ctx.getArgument("server", String::class.java)
                        val pageStr = ctx.getArgument("page", String::class.java)
                        val page = pageStr.toIntOrNull() ?: 1
                        sendMuteList(ctx.source, server, if (page < 1) 1 else page)
                        1
                    }
                )
            )
            .then(argument<CommandSource, String>("page", word())
                .executes { ctx ->
                    val pageStr = ctx.getArgument("page", String::class.java)
                    val page = pageStr.toIntOrNull() ?: 1
                    sendMuteList(ctx.source, null, if (page < 1) 1 else page)
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
                    handleHistory(ctx.source, input, 1)
                    1
                }
                .then(argument<CommandSource, String>("page", word())
                    .executes { ctx ->
                        val input = ctx.getArgument("target", String::class.java)
                        val pageStr = ctx.getArgument("page", String::class.java)
                        val page = pageStr.toIntOrNull() ?: 1
                        handleHistory(ctx.source, input, if (page < 1) 1 else page)
                        1
                    }
                )
            )
    }

    private val pageSize = 10

    private fun sendBanList(source: CommandSource, server: String?, page: Int) {
        val all = adminService.listActiveBans(server)
        if (all.isEmpty()) {
            source.sendMessage(Component.text("§7[LIST] §fAucun ban actif."))
            return
        }
        val totalPages = ((all.size - 1) / pageSize) + 1
        val safePage = page.coerceIn(1, totalPages)
        val from = (safePage - 1) * pageSize
        val to = (from + pageSize).coerceAtMost(all.size)
        val bans = all.sortedByDescending { it.begin }.subList(from, to)
        source.sendMessage(Component.text("§7[LIST] §fBans actifs${server?.let { " (serveur: $it)" } ?: " (globaux+locaux)"} §7— page §f${safePage}/${totalPages}"))
        val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withLocale(Locale.FRANCE)
        bans.forEach { b ->
            val scope = if (b.server == null) "global" else b.server
            val who = b.uuid?.toString() ?: (b.ip ?: "?")
            val end = b.end?.toString() ?: "permanent"
            source.sendMessage(Component.text("§8- §f$who §7| §f$scope §7| §f${b.reason ?: "-"} §7| fin: §f$end"))
        }
    }

    private fun sendMuteList(source: CommandSource, server: String?, page: Int) {
        val all = adminService.listActiveMutes(server)
        if (all.isEmpty()) {
            source.sendMessage(Component.text("§7[LIST] §fAucun mute actif."))
            return
        }
        val totalPages = ((all.size - 1) / pageSize) + 1
        val safePage = page.coerceIn(1, totalPages)
        val from = (safePage - 1) * pageSize
        val to = (from + pageSize).coerceAtMost(all.size)
        val mutes = all.sortedByDescending { it.muteBegin }.subList(from, to)
        source.sendMessage(Component.text("§7[LIST] §fMutes actifs${server?.let { " (serveur: $it)" } ?: " (globaux+locaux)"} §7— page §f${safePage}/${totalPages}"))
        mutes.forEach { m ->
            val scope = m.muteServer ?: "global"
            val who = m.uuid ?: (m.muteIp ?: "?")
            val end = m.muteEnd?.toString() ?: "permanent"
            source.sendMessage(Component.text("§8- §f$who §7| §f$scope §7| §f${m.muteReason ?: "-"} §7| fin: §f$end"))
        }
    }

    private fun handleHistory(source: CommandSource, input: String, page: Int = 1) {
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
        val (bansAll, mutesAll) = adminService.getHistory(uuid, ip)
        if (bansAll.isEmpty() && mutesAll.isEmpty()) {
            source.sendMessage(Component.text("§7[HISTORY] §fAucune sanction trouvée."))
            return
        }
        val merged = bansAll.map { Pair(0, it.begin) to "§8- §cBAN §7| §f${if (it.server == null) "global" else it.server} §7| §f${it.reason ?: "-"} §7| du: §f${it.begin} §7au: §f${it.end ?: "permanent"}" } +
                mutesAll.map { Pair(1, it.muteBegin) to "§8- §6MUTE §7| §f${it.muteServer ?: "global"} §7| §f${it.muteReason ?: "-"} §7| du: §f${it.muteBegin} §7au: §f${it.muteEnd ?: "permanent"}" }
        val sorted = merged.sortedByDescending { it.first.second }
        val totalPages = ((sorted.size - 1) / pageSize) + 1
        val safePage = page.coerceIn(1, totalPages)
        val from = (safePage - 1) * pageSize
        val to = (from + pageSize).coerceAtMost(sorted.size)
        source.sendMessage(Component.text("§7[HISTORY] §fSanctions pour §b$input§f — page §f${safePage}/${totalPages}:"))
        for (i in from until to) {
            source.sendMessage(Component.text(sorted[i].second))
        }
    }
}
