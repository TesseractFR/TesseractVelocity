package onl.tesseract.tesseractVelocity.domain.admin.cache

import onl.tesseract.tesseractVelocity.domain.admin.Mute
import java.time.Instant

data class CachedMute (
    val expiration: Instant,
    val mute: Mute?
){
    val isValid
        get() = expiration.isAfter(Instant.now())
}