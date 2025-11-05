package onl.tesseract.tesseractVelocity.service.vote

import com.velocitypowered.api.proxy.Player
import onl.tesseract.tesseractVelocity.domain.vote.Vote
import onl.tesseract.tesseractVelocity.domain.vote.VoteBuffer
import onl.tesseract.tesseractVelocity.domain.vote.VoteSite
import onl.tesseract.tesseractVelocity.repository.vote.VoteRepository
import onl.tesseract.tesseractVelocity.repository.vote.entity.toEntity
import java.time.Duration
import java.time.Instant
import java.util.*

class VoteService(private val voteRepository: VoteRepository) {
    fun getCanVoteNumber(player: Player): Int {
        var availableSite = 0
        voteRepository.getPlayerLastVotes(player.uniqueId).forEach {
                val voteSite = it.service.toDomain()
            if(!Duration.between(it.date.plusSeconds(voteSite.delayMinutes*60L), Instant.now()).isNegative){
                availableSite++
            }
        }
        return availableSite
    }

    fun consumeVoteBuffer(player: Player): Int {
        val buffer =  voteRepository.getPlayerBuffer(player.username)
        for (entry in buffer){
            registerVote(Vote(player.uniqueId, entry.date,entry.serviceName.toDomain()))
            addPoint(player.uniqueId)
        }
        clearBuffer(player.username)
        return buffer.size
    }

    fun registerVote(vote: Vote) {
        voteRepository.registerVote(vote.toEntity())
    }

    fun clearBuffer(playerName : String){
        voteRepository.clearBuffer(playerName)
    }

    fun addPoint(playerUUID: UUID){
        voteRepository.addPoint(playerUUID)
    }

    fun registerBufferVote(username: String, serviceName: String) {
        voteRepository.registerBufferVote(VoteBuffer(username, Instant.now(),getVoteService(serviceName)).toEntity())
    }

    fun getVoteService(serviceName: String): VoteSite {
        return voteRepository.getVoteService(serviceName).toDomain()
    }

}

