package onl.tesseract.tesseractVelocity.domain.vote

import java.time.Instant
import java.util.UUID

data class Vote(
    var playerUuid: UUID,
    var date: Instant,
    var service: VoteSite,
)