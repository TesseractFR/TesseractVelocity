package onl.tesseract.tesseractVelocity.domain.admin

import java.time.Instant

data class Mute(
    var id: Int? = null,

    var uuid: String? = null,

    var muteIp: String? = null,

    var muteStaff: String? = null,

    var muteReason: String? = null,

    var muteServer: String? = null,

    var muteBegin: Instant? = null,

    var muteEnd: Instant? = null,

    var muteState: Boolean? = false,

    var muteUnmutedate: Instant? = null,

    var muteUnmutestaff: String? = null,

    var muteUnmutereason: String? = null,
) {
    fun isActive(): Boolean {
        val now = Instant.now()
        return muteState == true && (muteEnd == null || now.isBefore(muteEnd))
    }
}
