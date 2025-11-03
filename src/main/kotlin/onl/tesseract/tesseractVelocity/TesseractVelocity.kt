package onl.tesseract.tesseractVelocity;

import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import commands.BanCommands
import commands.MuteCommands
import commands.KickCommands
import commands.ListCommands
import commands.AltsCommands
import commands.ReloadCommands
import commands.StaffChatCommands
import onl.tesseract.tesseractVelocity.command.LookupCommandHandler
import onl.tesseract.tesseractVelocity.config.Config
import onl.tesseract.tesseractVelocity.controller.listener.admin.BanListener
import onl.tesseract.tesseractVelocity.controller.listener.admin.ChatListener
import onl.tesseract.tesseractVelocity.controller.listener.staffchat.StaffChatChatListener
import onl.tesseract.tesseractVelocity.repository.admin.AdminRepository
import onl.tesseract.tesseractVelocity.service.admin.AdminService
import org.slf4j.Logger
import java.nio.file.Path


@Plugin(
    id = "tesseractvelocity", name = "TesseractVelocity", version = "1.0-SNAPSHOT")
class TesseractVelocity @Inject constructor(val server: ProxyServer,val logger: Logger,@DataDirectory val dataDirectory : Path) {


    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        logger.info("Tesseract initialization")
        Config.load()
        Hibernate.init(Config.dbAdmin)

        val adminService = AdminService(AdminRepository(), server)
        BanCommands(server, adminService).registerAll()
        MuteCommands(server, adminService).registerAll()
        KickCommands(server, adminService).registerAll()
        ListCommands(server, adminService).registerAll()
        AltsCommands(server, adminService).registerAll()
        ReloadCommands(server).registerAll()
        StaffChatCommands(server).registerAll()
        server.eventManager.register(this, BanListener(adminService))
        server.eventManager.register(this, ChatListener(adminService))
        server.eventManager.register(this, StaffChatChatListener(server))
        // Enregistrement de la commande lookup
        val lookupCommand = LookupCommandHandler(adminService).createBrigadierCommand()
        server.commandManager.register(lookupCommand)
        logger.info("Commande lookup enregistrée avec permission tesseract.admin.lookup")
    }
}
