package onl.tesseract.tesseractVelocity.domain.vote

import onl.tesseract.tesseractVelocity.repository.vote.entity.VoteBufferEntity
import toEntity
import java.time.Instant

data class VoteBuffer(
    val pseudo: String,
    val date: Instant,
    val serviceName: VoteSite,
){
    fun toEntity(): VoteBufferEntity {
        return VoteBufferEntity(pseudo = pseudo,
            date = date,
            serviceName = serviceName.toEntity())
    }
}