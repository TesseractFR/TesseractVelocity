package onl.tesseract.tesseractVelocity.controller.listener.vote

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.LoginEvent
import com.velocitypowered.api.proxy.Player
import com.vexsoftware.votifier.velocity.event.VotifierEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import onl.tesseract.tesseractVelocity.TesseractVelocity
import onl.tesseract.tesseractVelocity.append
import onl.tesseract.tesseractVelocity.domain.vote.Vote
import onl.tesseract.tesseractVelocity.service.vote.VoteService
import java.time.Instant

class VoteListener(private val voteService: VoteService) {
    @Subscribe
    fun onLogin(event: LoginEvent) {
        println("[VOTE] LoginEvent capté pour ${event.player.username}")
        val player = event.player
        notifyBuffered(player)
        val votes = voteService.getCanVoteNumber(player)

    }

    @Subscribe
    fun onVote(event: VotifierEvent){
        val vote = event.vote
        println("Received vote from " + vote.username + ", " + vote.serviceName)
        val playerOpt = TesseractVelocity.instance.server.getPlayer(vote.username)
        if(playerOpt.isEmpty){
            println("Player is offline, buffering 1 vote for player " + vote.username)
            voteService.registerBufferVote(vote.username,vote.serviceName)
            return
        }
        val player : Player = playerOpt.get()
        println("Adding 1 vote key to player " + vote.username)
        voteService.registerVote(
            Vote(player.uniqueId, Instant.now(),voteService.getVoteService(vote.serviceName)))
        voteService.addPoint(player.uniqueId)
        sendMessage(player,1)
    }


    private fun notifyBuffered(player: Player) {
        val votesNumber = voteService.consumeVoteBuffer(player)
        println("Found $votesNumber buffered vote for player ${player.username}.")
        if(votesNumber>0)
            sendMessage(player,votesNumber)
    }


    private fun sendMessage(player: Player, count: Int) {
        player.sendMessage(
            Component.text("[Vote] ", NamedTextColor.GOLD)
                    .append("Merci de votre vote ! Vous avez reçu ", NamedTextColor.YELLOW)
                    .append(count, NamedTextColor.GREEN)
                    .append(" point(s) de vote !",NamedTextColor.YELLOW))
    }
}